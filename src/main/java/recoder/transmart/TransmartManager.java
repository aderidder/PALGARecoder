/*
 * Copyright 2017 NKI/AvL
 *
 * This file is part of PALGARecoder.
 *
 * PALGARecoder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PALGARecoder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PALGARecoder. If not, see <http://www.gnu.org/licenses/>
 */

package recoder.transmart;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import recoder.codebook.ProtocolCodebookManager;
import recoder.data.in.InputData;
import recoder.data.out.OutputData;
import recoder.data.out.OutputHeaderItem;
import recoder.settings.RunParameters;
import recoder.utils.ExcelUtils;
import recoder.utils.LogTracker;
import recoder.utils.Romans;
import recoder.utils.enumerate.OutputFormatType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * used to create a transmart tree based on a transmart tree template and the data
 */
public class TransmartManager {
    private static final Logger logger = LogManager.getLogger(TransmartManager.class.getName());

    private Map<String, String> fullColToPathMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, List<String>> pathToColsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    private RunParameters runParameters;

    private int maxPathLength=-1;

    /**
     * constructor for a transmart manager
     * @param runParameters settings for this run
     * @param inputData   input data from the data file
     */
    public TransmartManager(RunParameters runParameters, InputData inputData){
        this.runParameters = runParameters;
        setup(inputData);
    }

    /**
     * setup, reads the template and matches the template to the inputdata
     * @param inputData    input data from the data file
     */
    private void setup(InputData inputData){
        Map<String, String> colToPathMap = readTransmartTreeTemplate();
        dataMatch(inputData, colToPathMap);
    }

    /**
     * reads a transmart tree template, which will be used to construct transmart output tree
     * @return a map which contains all column to transmart path mappings
     */
    private Map<String, String> readTransmartTreeTemplate(){
        Map<String, String> colToPathMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String transmartTreeFile = runParameters.getTransmartTreeFile();

        try {
            // open the template for the protocol
            Workbook workbook = WorkbookFactory.create(new File(transmartTreeFile));
            // information we need is in the first sheet. Open and read the data
            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNr = sheet.getLastRowNum();
            for(int i=0; i<=lastRowNr; i++) {
                Row row = sheet.getRow(i);
                if(row!=null) {
                    // handle a single row
                    handleTemplateRow(colToPathMap, row);
                }
            }
            workbook.close();
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
        }
        return colToPathMap;
    }

    /**
     * add information from a single row to the Map
     * @param colToPathMap    the column to transmart path map
     * @param row             the current template row
     */
    private void handleTemplateRow(Map<String, String> colToPathMap, Row row){
        // the column name is in the first column and the path for it is in the second
        String headerName = ExcelUtils.getCellValue(row, 0).toLowerCase().trim();
        String path = ExcelUtils.getCellValue(row, 1);

        // check whether the entry has a column name and a path
        if (!headerName.equalsIgnoreCase("") && !path.equalsIgnoreCase("")) {
            if (!path.endsWith("/")) {
                path += "/";
            }
            colToPathMap.put(headerName, path);
        }
        else if(!headerName.startsWith("#") && !headerName.equalsIgnoreCase("")){
            logger.warn("Warning: item {} not properly defined in tree", headerName);
        }
    }

    /**
     * match the input data and the column to transmart path map
     * @param inputData       the input data
     * @param colToPathMap    the column to transmart path map
     */
    private void dataMatch(InputData inputData, Map<String, String> colToPathMap){
        ProtocolCodebookManager protocolCodebookManager = ProtocolCodebookManager.getProtocolManager(runParameters);
        // retrieve the data header lists
        List<String> dataNoRomanHeadernames = inputData.getNoRomanHeaderList();
        List<String> dataOrigHeaderNames = inputData.getOrigHeaderList();
        List<String> dataRomans = inputData.getRomanList();

        // for all items in the no roman headers list
        for(int i=0; i<dataNoRomanHeadernames.size(); i++){
            // retrieve the original header name and check whether it will be part of the output
            String origHeaderName = dataOrigHeaderNames.get(i);
            if (inputData.addDataToOutput(origHeaderName)) {
                // retrieve the header name without roman number, the max used version for the concept and translate the header
                String maxVersion = inputData.getMaxVersionForConcept(origHeaderName);
                String noRomanName = dataNoRomanHeadernames.get(i);
                String translatedHeaderName = protocolCodebookManager.translateConcept(noRomanName, maxVersion, OutputFormatType.DESCRIPTIONS);

                // if the transmart template did not contain the noRomanName, we add a fake path for it
                if (!colToPathMap.containsKey(noRomanName)) {
                    addFakePath(noRomanName, colToPathMap);
                }

                // get the path and attach the translated header name, which is the last part of the standardised name for transmart
                String path = colToPathMap.get(noRomanName);
                path += translatedHeaderName;

                // replace the {ROMAN} part in the path with the actual roman number for this item
                if (path.contains("{ROMAN}")) {
                    path = replaceRoman(path, dataRomans.get(i));
                }

                // add to the map for later use
                fullColToPathMap.put(origHeaderName, path);

                // add to the path to columns map as well, which contains a path and a list of all original header names with this same path
                if (!pathToColsMap.containsKey(path)) {
                    pathToColsMap.put(path, new ArrayList<>());
                }
                pathToColsMap.get(path).add(origHeaderName);

                // check whether there is a new maximum path length
                checkMaxPath(path);
            }
        }
    }

    /**
     * replaces {ROMAN} with an actual roman as stored in the data
     * @param path     the path which contains the {ROMAN}
     * @param roman    the roman to replace it with
     * @return  a String with the replaced value
     */
    private String replaceRoman(String path, String roman){
        if(roman.equalsIgnoreCase("")){
            LogTracker.logMessage(this.getClass(), "The tranSMART tree suggests the data should have a Roman extension (path: "+path+"). This is not the case... The resulting path will not be correct.");
            return path;
        }
        else {
            return path.replace("{ROMAN}", Romans.getRomanOutputString(roman));
        }
    }

    /**
     * replaces {REPNR} with the report number
     * @param path     the path which contains the {ROMAN}
     * @param repeat   the repeat to replace it with
     * @return  a String with the replaced value
     */
    private String replaceRepNr(String path, String repeat){
        return path.replace("{REPNR}", "Report "+repeat);
    }

    /**
     * set the maximum path depth, which affects the number of levels in the header that will appear in the transmart output tree
     * @param path    the path to check
     */
    private void checkMaxPath(String path){
        int curPathLength;
        curPathLength = (path.split("/").length)+1;
        if(curPathLength>maxPathLength){
            maxPathLength = curPathLength;
        }
    }

    /**
     * adds a fake path for missing template tree items
     * @param headerName      header name which has no entry in the template
     * @param colToPathMap    map which contains the column to path mapping
     */
    private void addFakePath(String headerName, Map<String, String> colToPathMap){
        if(!runParameters.getTransmartPatientId().equalsIgnoreCase(headerName)) {
            LogTracker.logMessage(this.getClass(), "A fake path will be created for " + headerName + " as it has no entry in the tranSMART tree template");
        }
        String fakePath = "NoPathFound/";
        colToPathMap.put(headerName, fakePath);
    }

    /**
     * return a list of columns that are mapped to the same map as our headerValue
     * this is used to merge the data to one column
     * @param headerValue the concept name for which to retrieve the other headers with the same path
     * @return list with all header names with the same path as the original header name
     */
    public List<String> getMultiMappedItems(String headerValue){
        String path = fullColToPathMap.get(headerValue);
        List<String> multiMappedList = pathToColsMap.get(path);
        if(multiMappedList.size()>1){
            LogTracker.logMessage(this.getClass(), "A merged column will appear in the output, containing: "+multiMappedList.stream().collect(Collectors.joining("; ")));
        }
        return multiMappedList;
    }

    /**
     * creates a transmart tree file, based on the output data
     * We need to know which items appear in the output data file, as this will decide which items
     * appear in our tree. Hence, we fetch the original headers from the output file
     * We need the output data for this, as this way, we'll get the correct column numbering.
     * @param outputData    the output data
     */
    public void createTransmartFile(OutputData outputData){
        TmTreeWorkbook tmTreeWorkbook = new TmTreeWorkbook(maxPathLength);

        // get the header information
        List<OutputHeaderItem> headerList = outputData.getHeaderList();

        // get the short filename, as that's apparently used in the tranSMART tree file
        String shortDataOutFileName = runParameters.getShortDataOutFileName();

        // loop over the translated data's original headerlist
        for(int i=0; i<headerList.size(); i++){
            List<String> dataList;
            OutputHeaderItem outputHeaderItem = headerList.get(i);
            String origHeader = outputHeaderItem.getOrigHeaderName();
            if(origHeader.equalsIgnoreCase(runParameters.getTransmartPatientId())){
                dataList = getIdPathList(i + 1, shortDataOutFileName);
            }
            else {
                List<String> pathList = getPathList(origHeader, outputHeaderItem.getRepeat());

                // add some standard stuff to our datalist and add the pathlist we just created
                dataList = setupGeneralList(i + 1, shortDataOutFileName);
                dataList.addAll(pathList);
            }
            // write the datalist to our excel workbook
            tmTreeWorkbook.writeTreeValues(dataList);
        }

        // save the workbook
        tmTreeWorkbook.writeBook(runParameters.getTranSMARTTreeOutFileName());
    }


    /**
     * returns a list representation of the path belonging to the header
     * @param origHeader header for which a list representation of its path should be returned
     * @return  List representation of the path
     */
    private List<String> getPathList(String origHeader, int repNr){
        String path = fullColToPathMap.get(origHeader);
        path = replaceRepNr(path, String.valueOf(repNr));
        String [] splitPath = path.split("/");
        return pathToTmList(splitPath);
    }

    /**
     * adapt to the new transmart tree format, which expect a level and then two fields for the metadata
     * @param splitPath    the normal path
     * @return path with the extra fields
     */
    private List<String> pathToTmList(String [] splitPath){
        List<String> pathList = new ArrayList<>();
        for(String item:splitPath){
            pathList.add(item);
            pathList.add("");
            pathList.add("");
        }
        return pathList;
    }

    /**
     * adds the id entry to the transmart tree
     * @param index                 the index of the column in the output file
     * @param shortDataOutFileName  name of the data file
     * @return a list containing the standard list as well as extra info for the id
     */
    private List<String> getIdPathList(int index, String shortDataOutFileName){
        List<String> dataList = setupGeneralList(index, shortDataOutFileName);
        dataList.add("SUBJ_ID");
        dataList.add("");
        dataList.add("");
        return dataList;
    }

    /**
     * add general items to a list for the transmart output file
     * @param index                   the index of the column in the output file
     * @param shortDataOutFileName    name of the data file
     * @return a list containing the necessary information for the first couple of columns for the transmart output file
     */
    private List<String> setupGeneralList(int index, String shortDataOutFileName){
        List<String> dataList = new ArrayList<>();
        dataList.add("Low-dimensional");
        dataList.add(shortDataOutFileName);
        dataList.add(String.valueOf(index));
        dataList.add(runParameters.getStudyName());
        dataList.add("");
        dataList.add("");
        return dataList;
    }

    /**
     * helper class for the tranSMART tree excel file
     */
    private class TmTreeWorkbook {
        private final String treeStructureSheetName = "Tree structure template";
        private final List<String> treeStructureHeader = new ArrayList<>();
        private final String valueSubstitutionSheetName = "Value substitution";
        private final List<String> valueSubstitutionHeader = Arrays.asList("Sheet name/File name", "Column number", "From value", "To value");

        private Workbook workbook;
        private CellStyle cellStyle;

        /**
         * constructor for a new transmart tree workbook
         * @param maxPathLength    the maximum path length encountered, necessary to create the proper header length
         */
        TmTreeWorkbook(int maxPathLength){
            createTreeStructureHeader(maxPathLength);
            setupWorkbook();
        }

        /**
         * create the workbook and do some setup things, such as creating the header style etc.
         */
        private void setupWorkbook() {
            workbook = ExcelUtils.createXLSXWorkbook();
            cellStyle = ExcelUtils.createHeaderStyle(workbook, IndexedColors.AQUA);
            setupTreeStrucureSheet();
            setupValueSubstitutionSheet();
        }

        /**
         * write the workbook to a file
         * @param fileOut    filename to write to
         */
        private void writeBook(String fileOut){
            ExcelUtils.writeXLSXWorkBook(workbook, fileOut);
        }

        /**
         * setup for the main sheet
         */
        private void setupTreeStrucureSheet(){
            ExcelUtils.createSheetWithHeader(workbook, treeStructureSheetName, treeStructureHeader, cellStyle);
        }

        /**
         * setup for the value substitution sheet
         */
        private void setupValueSubstitutionSheet(){
            ExcelUtils.createSheetWithHeader(workbook, valueSubstitutionSheetName, valueSubstitutionHeader, cellStyle);
        }

        /**
         * write a list of values to the tree structure sheet
         * @param values values to write
         */
        private void writeTreeValues(List<String> values){
            ExcelUtils.writeValues(workbook.getSheet(treeStructureSheetName), values);
        }

        /**
         * generates the header for tree structure sheet
         * @param maxLevel    decides on how many level headernames should appear, which depends on the max length of the paths
         */
        private void createTreeStructureHeader(int maxLevel){
            treeStructureHeader.add("tranSMART data type");
            treeStructureHeader.add("Sheet name/File name");
            treeStructureHeader.add("Column number");
            // add levels depending on the number of levels found in the data
            for(int i=1; i<=maxLevel; i++) {
                treeStructureHeader.add("Level " + i);
                treeStructureHeader.add("Level " + i + " metadata tag");
                treeStructureHeader.add("Level " + i + " metadata value");
            }
        }
    }
}
