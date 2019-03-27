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

package recoder.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import recoder.utils.enumerate.OutputFormatType;
import recoder.utils.enumerate.OutputFileType;

import java.io.File;

/**
 * contains all the user parameters the ui generates
 * could create separate runsettings for transmart and text?
 */
public class RunParameters {
    private static final Logger logger = LogManager.getLogger(RunParameters.class.getName());
    private String fromLanguage;

    private OutputFormatType outputFormatType;
    private OutputFileType outputFileType;
    private String protocolPrefix;
    private String inputFileName;
    private String studyName;
    private String protocolName;
    private String transmartTreeFile;
    private String transmartPatientId;
    private boolean exportAsWideFormat;

    public RunParameters(){
        this("", GlobalSettings.getDefaultProtocolName(), OutputFormatType.DESCRIPTIONS, OutputFileType.TEXT, "", "", "", true, "");
    }

    public RunParameters(String inputFileName, String protocolName, OutputFormatType outputFormatType, OutputFileType outputFileType, String fromLanguage){
        this(inputFileName, protocolName, outputFormatType, outputFileType, fromLanguage, "", "", false, "");
    }

    public RunParameters(String inputFileName, String protocolName, OutputFormatType outputFormatType, OutputFileType outputFileType, String fromLanguage, String transmartTreeFile, String studyName, Boolean exportAsWideFormat, String transmartPatientId){
        this.inputFileName =  inputFileName;
        this.protocolName = protocolName;
        this.protocolPrefix = GlobalSettings.getProtocolPrefix(protocolName);
        this.outputFormatType = outputFormatType;
        this.outputFileType = outputFileType;
        this.transmartTreeFile = transmartTreeFile;
        this.studyName = studyName;
        this.exportAsWideFormat = exportAsWideFormat;
        this.transmartPatientId = transmartPatientId;
        this.fromLanguage = fromLanguage;
    }

    public String getSummaryString(){
        String summaryText=
                "data file: "+inputFileName+"\n" +
                "protocol: "+protocolName+"\n" +
                "filetype: "+outputFileType.getPrettyString()+"\n" +
                "containing: "+outputFormatType.getPrettyString()+"\n" +
                "source language: "+fromLanguage;
        if(outputFileType.equals(OutputFileType.TRANSMART)){
            summaryText +=
                "transmart study name: " + studyName + "\n" +
                "transmart tree template: " + transmartTreeFile + "\n" +
                "transmart id column: " + transmartPatientId + "\n" +
                "transmart wideformat: " + exportAsWideFormat;
        }
        return summaryText;
    }

    /**
     * returns the output format type
     * @return the output format type
     */
    public OutputFormatType getOutputFormatType() {
        return outputFormatType;
    }

    /**
     * returns the output file type
     * @return the output file type
     */
    public OutputFileType getOutputFileType() {
        return outputFileType;
    }

    /**
     * returns the protocol prefix
     * @return the protocol prefix
     */
    public String getProtocolPrefix() {
        return protocolPrefix;
    }

    /**
     * returns the from language
     * @return the from language
     */
    public String getFromLanguage() {
        return fromLanguage;
    }

    /**
     * returns the name of the input file
     * @return the name of the input file
     */
    public String getInputFileName(){
        return inputFileName;
    }

    /**
     * returns the study name
     * @return the study name
     */
    public String getStudyName(){
        return studyName;
    }

    /**
     * returns the protocol name
     * @return the protocol name
     */
    public String getProtocolName() {
        return protocolName;
    }

    /**
     * returns the filename without the directory
     * @return the filename without the directory
     */
    public String getShortDataOutFileName(){
        String outFileName = getDataOutFileName();
        return outFileName.substring(outFileName.lastIndexOf(File.separator)+1);
    }

    /**
     * returns the name of the data output file
     * @return the name of the data output file
     */
    public String getDataOutFileName(){
        String outFileName = inputFileName.substring(0, inputFileName.lastIndexOf("."));
        outFileName += "_out.txt";
        return outFileName;
    }

    /**
     * returns the name of the transmart tree output file
     * @return the name of the transmart tree output file
     */
    public String getTranSMARTTreeOutFileName(){
        String outFileName = inputFileName.substring(0, inputFileName.lastIndexOf("."));
        outFileName += "_treeOut.xlsx";
        return outFileName;
    }

    /**
     * returns the name of the patient id column
     * @return the name of the patient id column
     */
    public String getTransmartPatientId(){
        return transmartPatientId;
    }

    /**
     * returns the transmart tree template file
     * @return the transmart tree template file
     */
    public String getTransmartTreeFile(){
        return transmartTreeFile;
    }

    public boolean exportAsWideFormat(){
        return exportAsWideFormat;
    }

    /**
     * returns whether the settings are valid
     * @return true/false
     */
    public boolean validSettings(){
        boolean valid = true;
        if (!isValidInputFile(inputFileName)) {
            logger.error("Please select a valid datafile before running");
            valid = false;
        }
        if(outputFileType.equals(OutputFileType.TRANSMART)){
            valid = validateTranSMART() && valid;
        }
        return valid;
    }

    /**
     * some specific validations for the transmart files
     * @return true / false
     */
    private boolean validateTranSMART(){
        boolean valid = true;
        if(transmartPatientId.equalsIgnoreCase("")){
            logger.error("When using tranSMART as output, please set a patient id column name");
            valid = false;
        }
        if(studyName.equalsIgnoreCase("")){
            logger.error("When using tranSMART as output, please set a Studyname");
            valid = false;
        }
        if(!isValidInputFile(transmartTreeFile)){
            logger.error("Please select a valid tranSMART tree file before running");
            valid = false;
        }
        return valid;
    }

    /**
     * returns whether a file is a valid file
     * @param fileName    file to validate
     * @return true/false
     */
    private boolean isValidInputFile(String fileName){
        File file = new File(fileName);
        return !fileName.equalsIgnoreCase("") && file.exists() && !file.isDirectory();
    }
}
