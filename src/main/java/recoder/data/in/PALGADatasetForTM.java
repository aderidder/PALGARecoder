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

package recoder.data.in;

import recoder.codebook.HousekeepingCodebookManager;
import recoder.codebook.ProtocolCodebookManager;
import recoder.data.out.OutputDataWide;
import recoder.settings.RunParameters;
import recoder.transmart.TransmartManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used when data has to be prepared for a transmart import
 */
class PALGADatasetForTM extends DefaultDataset{
//    private static final Logger logger = LogManager.getLogger(PALGADatasetForTM.class.getName());

    private TransmartManager transmartManager;

    private PALGADatasetForTM(RunParameters runParameters) {
        super(runParameters);
    }

    /**
     * reads input file based on the runsettings and returns a new PALGADatasetForTM
     * @param runParameters    settings for this run
     * @return  a new Object which contains the datafile and can be used to prepare the output for transmart
     */
    static PALGADatasetForTM createDataset(RunParameters runParameters){
        String line;
        PALGADatasetForTM palgaDataset = new PALGADatasetForTM(runParameters);

        // create buffered reader
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(runParameters.getInputFileName())), "ISO-8859-1"))) {
            // read the first line of the recoder.data, which contains the header, and add it to our input recoder.data
            palgaDataset.addHeader(br.readLine());

            palgaDataset.checkPatientIdColumn();
            // add other lines
            while((line=br.readLine())!=null){
                palgaDataset.addData(line);
            }
            palgaDataset.postReadOperations();
        } catch(IOException e){
            throw new RuntimeException("A fatal exception occurred whilst reading the dataset: "+e.getMessage());
        }
        return palgaDataset;
    }

    /**
     * validates whether the patient id column exists
     */
    private void checkPatientIdColumn(){
        String tmIdLower = runParameters.getTransmartPatientId().toLowerCase();
        if(origHeaderList.stream().map(t->t.toLowerCase()).noneMatch(t->t.equalsIgnoreCase(tmIdLower))){
            throw new RuntimeException("The specified patient identier column '"+ runParameters.getTransmartPatientId()+"' was not found in the data. Please fix");
        }
    }

    /**
     * stuff to do after reading the datafile
     */
    private void postReadOperations(){
        checkRomans();
        transmartManager = new TransmartManager(runParameters, this);
    }

    /**
     * translate the header
     */
    void translateHeader() {
        HousekeepingCodebookManager housekeepingCodebookManager = HousekeepingCodebookManager.getProtocolManager(runParameters);
        ProtocolCodebookManager protocolCodebookManager = ProtocolCodebookManager.getProtocolManager(runParameters);

        // for each entry in the header
        for(int i=0; i<noRomanHeaderList.size(); i++){
            // retrieve the original header name
            String origHeaderName = origHeaderList.get(i);
            // check whether the column should be added to the output
            if(addDataToOutput(origHeaderName)){
                // if so, retrieve the max used protocol version for the concept as well as the name without the roman
                String noRomanHeaderName = noRomanHeaderList.get(i);
                String protocolVersion = maxVersionForConcept[i];

                // first check whether the header is a PALGA housekeeping column, for which there is a separate codebook
                if (housekeepingCodebookManager.containsHeaderName(noRomanHeaderName)) {
                    outputData.addHeaderValue(origHeaderList.get(i), housekeepingCodebookManager.translateConcept(noRomanHeaderName), true);
                }
                else {
                    // find all items which have the same path as our current item
                    List<String> multiMappedItems = transmartManager.getMultiMappedItems(origHeaderName);
                    // if our current item is the first item in the list, merge all items in the list
                    // this way, we basically collapse the columns into one column
                    if (multiMappedItems != null && multiMappedItems.indexOf(origHeaderName) == 0) {
                        // the multimapped items are of course based on the original column names
                        // the codebooks are based on the non-roman names
                        // so we
                        //  * find the index of the item we found in the multiMappedItem
                        //  * use the index to get the no roman version of the name
                        //  * use this name to translate the concept
                        //  * and if there are >1 items, join the translated concepts together with underscores
                        String output = multiMappedItems.stream().map(t -> protocolCodebookManager.translateConcept(noRomanHeaderList.get(origHeaderList.indexOf(t)), protocolVersion, outputFormatType)).collect(Collectors.joining("_"));

                        // maybe this should be part of the previous step? after the first add another map which checks for the romans?
                        if (!romansInHeader.get(i).equalsIgnoreCase("")) {
                            output += "_" + romansInHeader.get(i);
                        }
                        // add the new header to the outputdata
                        outputData.addHeaderValue(origHeaderList.get(i), output, false);
                    }
                }
            }
        }
    }

    /**
     * translate the data values
     */
    void translateValues(){
        lines.forEach(this::translateLine);
    }

    /**
     * translate a single line
     * @param line  the line to translate
     */
    private void translateLine(List<String> line){
        HousekeepingCodebookManager housekeepingCodebookManager = HousekeepingCodebookManager.getProtocolManager(runParameters);
        ProtocolCodebookManager protocolCodebookManager = ProtocolCodebookManager.getProtocolManager(runParameters);
        List<String> translatedLine = new ArrayList<>();

        // for each value in the line
        for(int i=0; i<line.size(); i++){
            // retrieve the original header name
            String origHeaderName = origHeaderList.get(i);
            // check whether the column should be added to the output
            if(addDataToOutput(origHeaderName)){
                String noRomanHeaderName = noRomanHeaderList.get(i);
                // check whether the concept is a housekeeping concept
                if (housekeepingCodebookManager.containsHeaderName(noRomanHeaderName)) {
                    String value = line.get(i);
                    translatedLine.add(housekeepingCodebookManager.translateValue(noRomanHeaderName, value));
                }
                else {
                    // otherwise
                    addDataValue(line, translatedLine, protocolCodebookManager, origHeaderName);
                }
            }
        }
        outputData.addDataLine(translatedLine);
    }

    /**
     * continues the work of translated line
     * @param line                      the line to translate
     * @param translatedLine            list where the translated values are stored
     * @param protocolCodebookManager   protocol manager
     * @param origHeaderName            original header name
     */
    private void addDataValue(List<String> line, List<String> translatedLine, ProtocolCodebookManager protocolCodebookManager, String origHeaderName){
        // retrieve the max used protocol version for the concept
        String version = getProtocolVersionForLine(line);

        // find all items which have the same path as our current item
        // if there is only one item with the path, the multiMappedItems list will contain only one item
        List<String> multiMappedItems = transmartManager.getMultiMappedItems(origHeaderName);

        // if our current item is the first item in the list, merge all items in the list to one column
        if (multiMappedItems != null && multiMappedItems.indexOf(origHeaderName) == 0) {
            String newValue = mergeDataValues(multiMappedItems, line, protocolCodebookManager, version);
            translatedLine.add(newValue);
        }
    }


    /**
     * merges items into one column
     * @param multiMappedItems           items which map have the same path
     * @param line                       data line
     * @param protocolCodebookManager    manager for the current protocol
     * @param version                    max version used for the concept
     * @return merged value of the column values
     */
    private String mergeDataValues(List<String> multiMappedItems, List<String> line, ProtocolCodebookManager protocolCodebookManager, String version){
        String newValue="";
        // for each column which should be mapped to a single column
        for (String headerValue : multiMappedItems) {
            // fetch the current column name and its index
            int index = origHeaderList.indexOf(headerValue);
            // use the codebook to translate the value
            String curValue = protocolCodebookManager.translateValue(noRomanHeaderList.get(index), line.get(index), version, outputFormatType).trim();
            // merge the values
            if (!curValue.equalsIgnoreCase("other") && !curValue.equalsIgnoreCase("")) {
                newValue += curValue + "&";
            }
        }
        // remove trailing "&" if it exists
        if(newValue.endsWith("&")){
            return newValue.substring(0, newValue.length()-1);
        }
        return newValue;
    }


    /**
     * write the output to file
     */
    @Override
    public void writeOutput() {
        if(runParameters.exportAsWideFormat()){
            ((OutputDataWide) outputData).expandHeader();
        }
        outputData.writeData();
        transmartManager.createTransmartFile(outputData);
    }

}
