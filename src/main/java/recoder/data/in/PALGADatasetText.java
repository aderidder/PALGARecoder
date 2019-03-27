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
import recoder.settings.RunParameters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Used when data format should be simple text
 */
class PALGADatasetText extends DefaultDataset{

    private PALGADatasetText(RunParameters runParameters){
        super(runParameters);
    }

    /**
     * reads input file based on the runsettings and returns a new PALGADatasetText
     * @param runParameters    settings for this run
     * @return  a new Object which contains the datafile and can be used to generate the text output
     */
    static PALGADatasetText createDataset(RunParameters runParameters){
        PALGADatasetText palgaDatasetText = new PALGADatasetText(runParameters);
        String line;

        // create buffered reader
        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(new File(runParameters.getInputFileName())), "ISO-8859-1"))) {
            // read the first line of the recoder.data, which contains the header, and add it to our input recoder.data
            palgaDatasetText.addHeader(br.readLine());
            // add other lines
            while((line=br.readLine())!=null){
                palgaDatasetText.addData(line);
            }
            palgaDatasetText.checkRomans();
        } catch(IOException e){
            throw new RuntimeException("A fatal exception occurred whilst reading the dataset: "+e.getMessage());
        }
        return palgaDatasetText;
    }

    /**
     * translate the header
     */
    @Override
    void translateHeader(){
        HousekeepingCodebookManager housekeepingCodebookManager = HousekeepingCodebookManager.getProtocolManager(runParameters);
        ProtocolCodebookManager protocolCodebookManager = ProtocolCodebookManager.getProtocolManager(runParameters);

        // for all elements in the norRomanHeaderList
        for(int i=0; i<noRomanHeaderList.size(); i++){
            // retrieve the original name of the element, with which we can check whether this column
            // should be added to the output
            String origHeaderName = origHeaderList.get(i);
            if(addDataToOutput(origHeaderName)){

                // retrieve the header name without roman, as well as the maximum codebook version used for the concept
                String headerName = noRomanHeaderList.get(i);
                String protocolVersion = maxVersionForConcept[i];
                String translatedHeader;

                // translate the header via the housekeepingcodebook or via the protocolcodebook
                if (housekeepingCodebookManager.containsHeaderName(headerName)) {
                    translatedHeader = housekeepingCodebookManager.translateConcept(headerName);
                    // store the translated header
                    outputData.addHeaderValue(origHeaderList.get(i), translatedHeader, true);
                }
                else {
                    translatedHeader = protocolCodebookManager.translateConcept(headerName, protocolVersion, outputFormatType);
                    // if it was a roman item, add an extension to the header
                    if (!romansInHeader.get(i).equalsIgnoreCase("")) {
                        translatedHeader += "_" + romansInHeader.get(i);
                    }
                    outputData.addHeaderValue(origHeaderList.get(i), translatedHeader, false);
                }
            }
        }
    }

    /**
     * translate the values
     */
    @Override
    void translateValues(){
        lines.forEach(this::translateLine);
    }

    /**
     * translate a single line
     * @param line    line to translate
     */
    private void translateLine(List<String> line){
        HousekeepingCodebookManager housekeepingCodebookManager = HousekeepingCodebookManager.getProtocolManager(runParameters);
        ProtocolCodebookManager protocolCodebookManager = ProtocolCodebookManager.getProtocolManager(runParameters);
        List<String> translatedLine = new ArrayList<>();

        // get the protocol version for this line
        String version = getProtocolVersionForLine(line);

        // for each item in the line
        for(int i=0; i<line.size(); i++){
            // retrieve the original header name and check whether the concept should be added to the output
            String origHeaderName = origHeaderList.get(i);
            if(addDataToOutput(origHeaderName)){
                // retrieve the no roman header name and the value that has to be translated
                String headerName = noRomanHeaderList.get(i);
                String value = line.get(i);
                String translatedValue;

                // translate value using the housekeeping codebook or the protocol codebook
                if (housekeepingCodebookManager.containsHeaderName(headerName)) {
                    translatedValue = housekeepingCodebookManager.translateValue(headerName, value);
                }
                else {
                    translatedValue = protocolCodebookManager.translateValue(headerName, value, version, outputFormatType);
                }
                // add the translated value to the translated line
                translatedLine.add(translatedValue);
            }
        }
        // store the translated line in the output
        outputData.addDataLine(translatedLine);
    }

    /**
     * write the output to file
     */
    @Override
    public void writeOutput() {
        outputData.writeData();
    }

}
