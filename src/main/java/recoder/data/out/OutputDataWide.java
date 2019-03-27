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

package recoder.data.out;

import recoder.settings.RunParameters;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * class used for output data
 * this class aims to write the data in the wide format, implying there is only one row per patient
 * this is necessary as tranSMART cannot handle multiple samples per patient (or in our case multiple reports);
 * it requires the id to be unique. Hence this:
 *
 * ID           Localization
 * MOC_A_0003   coecum
 * MOC_A_0003   rectum
 *
 * needs to become
 * ID           1_Localization  2_Localization
 * MOC_A_0003   coecum          rectum
 */
public class OutputDataWide extends OutputDataDefault{

    private OutputHeaderItem idItem;
    private List<OutputHeaderItem> repeatingHeaderList = new ArrayList<>();

    // for each id, store a DataRepeats object, which contains all the data for the id
    private Map<String, DataRepeats> dataRepeatMap = new HashMap<>();

    // stores which data entries are used by a repeat
    // so, if e.g. localization_II does not contain data for any patient for the second report
    // this will have a false and will not appear in the output, nor in the tM tree
    private Map<Integer, List<Boolean>> repeatHasDataMap = new HashMap<>();

    // store the index of the id
    private int idIndex=-1;
    // store the maximum number of repeats (which basically is the number of reports a single id has)
    private int maxRepeat=-1;

    public OutputDataWide(RunParameters runParameters, int idIndex){
        super(runParameters);
        this.idIndex = idIndex;
    }

    /**
     * store the original header and the translated header
     * at the moment we're not using the housekeeping boolean, but I think I will use this for maybe meta data,
     * so I'm keeping it for now
     * @param origHeaderName    original headerName
     * @param translatedName    translated headerName
     * @param housekeeping      whether the entry is a housekeeping entry
     */
    public void addHeaderValue(String origHeaderName, String translatedName, boolean housekeeping){
        // check whether the header is the id column
        String idCol = runParameters.getTransmartPatientId();
        if(origHeaderName.equalsIgnoreCase(idCol) && idItem==null){
            // if it is, store it in the nonrepeating list
//            nonRepeatingHeaderList.add(new OutputHeaderItem(origHeaderName, translatedName));
            idItem = new OutputHeaderItem(origHeaderName, translatedName);
        }
        else{
            repeatingHeaderList.add(new OutputHeaderItem(origHeaderName, translatedName));
        }
    }

    /**
     * add a line to our lines
     * @param line    the line to add
     */
    public void addDataLine(List<String> line) {
        // Find the identifier of the line
        String id = line.get(idIndex);

        // check whether we need to add a datarepeats object for this id
        if(!dataRepeatMap.containsKey(id)){
            dataRepeatMap.put(id, new DataRepeats());
        }

        // retrieve the datarepeats object
        DataRepeats dataRepeats = dataRepeatMap.get(id);
        // add the data line to the object and receive a List which contains which positions of the data contain values
        // so if col1 has data, col2 does not have data, col3 has data, this will return true, false, true
        List<Boolean> hasDataForPatientRepeat = dataRepeats.addLine(line, idIndex);
        // retrieve the current repeat of this data and check whether it is bigger than our current maximum repeat
        int patientMaxRepeat = dataRepeats.getMaxRepeat();
        if(patientMaxRepeat>maxRepeat){
            maxRepeat = patientMaxRepeat;
        }

        // check whether a boolean list already exists for the repeat
        if(!repeatHasDataMap.containsKey(patientMaxRepeat)){
            repeatHasDataMap.put(patientMaxRepeat, hasDataForPatientRepeat);
        }

        // retrieve the information about stored information for this repeat and
        // perform an "OR" with the hasDataForPatientRepeat
        // basically this means that if some other patient had
        // col1 data, col2 no data, col3 no data, col4 data (so true, false, false, true)
        // and our current patient has
        // true, true, false, false
        // the new situation will be true, true, false, true
        // if that is the final situation, the false will not appear in the output as there is no data for it in
        // any of the patients
        List<Boolean> hasDataForRepeat = repeatHasDataMap.get(patientMaxRepeat);
        for(int i=0; i<hasDataForRepeat.size(); i++){
            hasDataForRepeat.set(i, hasDataForRepeat.get(i)||hasDataForPatientRepeat.get(i));
        }
    }

    /**
     * turns the header into the wide format
     */
    public void expandHeader(){
        // first add the idItem
        headerList.add(idItem);
        // if the maxRepeat is one, there's nothing special going on, so just add all the items, as they
        // were already pre-filtered
        if(maxRepeat==1){
            headerList.addAll(repeatingHeaderList);
        }
        else{
            // otherwise, for all repeats
            for(int i=1; i<=maxRepeat; i++){
                // fetch which positions have data for the repeat
                List<Boolean> repeatHasData = repeatHasDataMap.get(i);

                for(int j=0; j<repeatingHeaderList.size(); j++){
                    // if the entry has data, add the entry's header to the headerList, also storing the appropriate repeat number
                    if(repeatHasData.get(j)){
                        OutputHeaderItem outputHeaderItem = repeatingHeaderList.get(j);
                        headerList.add(new OutputHeaderItem(outputHeaderItem.getOrigHeaderName(), outputHeaderItem.getTranslatedName(), i));
                    }
                }
            }
        }

    }

    /**
     * write the data to a file
     */
    public void writeData(){
        String outFileName = runParameters.getDataOutFileName();
        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName), "ISO-8859-1"))){

            // write the header; headerlist contains OutputHeaderItems
            bufferedWriter.write(headerList.stream().map(f-> f.getRepeat()+"_"+f.getTranslatedName()).collect(Collectors.joining("\t"))+System.lineSeparator());

            // write the lines
            for(DataRepeats dataRepeats:dataRepeatMap.values()) {
                List<String> data = new ArrayList<>();
                for (int i = 1; i <= maxRepeat; i++) {
                    // retrieve which entries have data and, therefore, should exist in the final output
                    List<Boolean> repeatHasDataList = repeatHasDataMap.get(i);
                    // retrieve the dataline, providing the repeat number as well as the filter
                    String dataLine = dataRepeats.getDataString(i, repeatHasDataList);

                    // for the first entry, add the identifier
                    if(i==1){
                        dataLine = dataRepeats.getId()+"\t"+dataLine;
                    }
                    // add the data line to a temporary list
                    data.add(dataLine);
                }
                // write the list
                bufferedWriter.write(data.stream().collect(Collectors.joining("\t"))+System.lineSeparator());
            }
        } catch (Exception e){
            throw new RuntimeException("A severe error occurred while writing the output file: "+e.getMessage());
        }
    }

}

class DataRepeats{
    private Map<Integer, List<String>> dataRepeatMap = new HashMap<>();
    private int maxRepeat = 0;
    private String id;

    DataRepeats(){

    }

    /**
     * returns the number of repeats this id has
     * @return the number of repeats this id has
     */
    int getMaxRepeat(){
        return maxRepeat;
    }

    /**
     * add a line to this id
     * @param line       the line to add
     * @param idIndex    index of the column which contains the id
     * @return list which contains information whether or not the position contains data
     */
    List<Boolean> addLine(List<String> line, int idIndex){
        maxRepeat++;
        // store the identifier
        if(maxRepeat==1){
            id = line.get(idIndex);
        }

        // remove the identifier from the line, as we don't want to repeat it (as that may confuse the datateam's scripts...)
        line.remove(idIndex);

        // store the line for the repeat
        dataRepeatMap.put(maxRepeat, line);

        // return which positions contain data
        return line.stream().map(t->!t.equalsIgnoreCase("")).collect(Collectors.toList());
    }

    /**
     * returns the identifier
     * @return the identifier
     */
    String getId(){
        return id;
    }

    /**
     * get string representation of the filtered line
     * @param repeat        repeat for which we want the data
     * @param dataFilter    filter for the repeat
     * @return filtered string with only the entries which need to appear in the output
     */
    String getDataString(int repeat, List<Boolean> dataFilter){
        List<String> dataList = new ArrayList<>();
        if(dataRepeatMap.containsKey(repeat)){
            List<String> line = dataRepeatMap.get(repeat);
            for(int i=0; i<line.size(); i++){
                if(dataFilter.get(i)){
                    dataList.add(line.get(i));
                }
            }
            return dataList.stream().collect(Collectors.joining("\t"));
        }
        else{
            // if no data for the repeat exists for this patient, create empty entries for all the entries for which
            // data should exist in the output
            return dataFilter.stream().filter(t-> t).map(t->"").collect(Collectors.joining("\t"));
        }
    }
}