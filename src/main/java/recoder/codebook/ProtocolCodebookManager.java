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

package recoder.codebook;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import recoder.settings.RunParameters;
import recoder.utils.ArtDecorCalls;
import recoder.utils.LogTracker;
import recoder.utils.enumerate.OutputFormatType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;


/**
 * each protocol has a manager; each manager manages one or more versions of the codebook
 *
 * each language (if a non-Dutch one were to be used for some reason as input...)
 * should have the same mappings to the ontologies / standardised values
 * so we should always be able to map to one of these from the origin language
 */
public class ProtocolCodebookManager {
    private static final Logger logger = LogManager.getLogger(ProtocolCodebookManager.class.getName());
    private static Map<String, ProtocolCodebookManager> protocolCodebookManagerMap = new HashMap<>();
    private static Map<String, CodebookInfo> protocolInfoMap = new HashMap<>();

    // create a ordered map, which will allow us to easily find the newest codebook version
    private Map<String, NavigableMap<String, DecorCodebook>> codebookMap = new HashMap<>();

    // datasetId is the same for the multiple languages, so we do not need a language key here
//    private Map<String, String> datasetIdMap = new HashMap<>();
    private String protocolPrefix;
    private String fromLanguage; //e.g. nl-NL

    /**
     * create a new protcol codebook manager
     * @param runParameters the settings used for the translation run
     */
    private ProtocolCodebookManager(RunParameters runParameters){
        this.protocolPrefix = runParameters.getProtocolPrefix();
        this.fromLanguage = runParameters.getFromLanguage();
//        setProtocolVersionToIdMap();
    }

    /**
     * returns an existing protocol manager or creates a new one, based on the runsettings
     * @param runParameters settings for the run
     * @return protocol codebook manager for the protocol
     */
    public static ProtocolCodebookManager getProtocolManager(RunParameters runParameters){
        String protocolPrefix = runParameters.getProtocolPrefix();
        String fromLanguage = runParameters.getFromLanguage();
        String key = protocolPrefix+fromLanguage;
        if(!protocolCodebookManagerMap.containsKey(key)){
            protocolCodebookManagerMap.put(key, new ProtocolCodebookManager(runParameters));
        }
        return protocolCodebookManagerMap.get(key);
    }

    /**
     * attempts to create a new codebook for de codebookVersionsMap
     * @param codebookVersionMap versionMap for some language
     * @param version            the version that should be added
     */
    private void addCodebook(Map<String, DecorCodebook> codebookVersionMap, String version){
        String datasetId = protocolInfoMap.get(protocolPrefix).getId(version);
//        String datasetId = datasetIdMap.get(version);
        if(datasetId==null) {
            String message = "version "+version+" of the protocol doesn't seem to exist online. Data using that version will not be translated.";
            LogTracker.logMessage(this.getClass(), message);
            return;
        }
        DecorCodebook decorCodebook = new DecorCodebook(fromLanguage, datasetId, version);
        codebookVersionMap.put(version, decorCodebook);
    }

    /**
     * translate a value
     * @param headerName headerName for the value to be translated
     * @param value      value to be translated
     * @param version    version of the required codebook
     * @param outputType the format type to use for the output
     * @return translated value
     */
    public String translateValue(String headerName, String value, String version, OutputFormatType outputType){
        String translatedValue = value;
        DecorCodebook decorCodebook = getCodebook(version);
        if(decorCodebook!=null && !value.equalsIgnoreCase("") && decorCodebook.containsHeaderName(headerName)){
            translatedValue = decorCodebook.translateConceptValue(outputType, value, headerName);
        }
        return translatedValue;
    }

    /**
     * translate a concept / headername
     * @param headerName the name to translate
     * @param version    the version of the codebook to use
     * @param outputType the format type to use for the output
     * @return translated headerName
     */
    public String translateConcept(String headerName, String version, OutputFormatType outputType){
        DecorCodebook decorCodebook = getCodebook(version);
        if(decorCodebook==null || !decorCodebook.containsHeaderName(headerName)){
            return headerName;
        }
        return decorCodebook.translateConcept(outputType, headerName);
    }

    /**
     * returns whether codebook contains headerName
     * @param headerName the name to check for
     * @param version    version of the codebook
     * @return true/false
     */
    public boolean containsHeaderName(String headerName, String version){
        DecorCodebook decorCodebook = getCodebook(version);
        if(decorCodebook==null){
            return false;
        }
        return decorCodebook.containsHeaderName(headerName);
    }


    /**
     * returns codebook of the specified version
     * @param version version of the codebook
     * @return codebook
     */
    private DecorCodebook getCodebook(String version){
        // otherwise, check whether we have the protocol available for this language
        if(!codebookMap.containsKey(fromLanguage)){
            codebookMap.put(fromLanguage, new TreeMap<>());
        }
        Map <String, DecorCodebook> codebookVersionMap = codebookMap.get(fromLanguage);

        // next, check whether we have the version available
        // if not, create a recoder.codebook for this version
        if(!codebookVersionMap.containsKey(version)){
            addCodebook(codebookVersionMap, version);
        }
        return codebookVersionMap.get(version);
    }

//    /**
//     * attempts to retrieve version and identifier information from a uri for the protocol
//     */
//    private void setProtocolVersionToIdMap(){
//        String uri = ArtDecorCalls.getProjectIndexURI(protocolPrefix);
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//
//        logger.log(Level.INFO, "Attempting to retrieve which version of the codebook are available using {}", uri);
//
//        try {
//            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//            Document dom = documentBuilder.parse(uri);
//            //get the root element
//            Element documentElement = dom.getDocumentElement();
//
//            //get a nodelist of elements
//            NodeList nodeList = documentElement.getElementsByTagName("dataset");
//
//            logger.log(Level.INFO, "Found {} versions", nodeList.getLength());
//
//            if(nodeList != null) {
//                for(int i=0; i<nodeList.getLength(); i++) {
//                    Element element = (Element) nodeList.item(i);
//                    datasetIdMap.put(element.getAttribute("versionLabel"), element.getAttribute("id"));
//
//                    logger.log(Level.INFO, "versionlabel found: {} id found: {}", element.getAttribute("versionLabel"),element.getAttribute("id"));
//                    findLanguages(element);
//                }
//            }
//        } catch (Exception e){
//            throw new RuntimeException("Exception occurred while attempting to retrieve which version are available for the codebook: "+e.getMessage());
//        }
//    }

    public static List<String> getProtocolLanguages(String protocolPrefix){
        return protocolInfoMap.get(protocolPrefix).getUniqueLanguages();
    }

    public static void createProtocolInfo(String protocolPrefix){
        if(!protocolInfoMap.containsKey(protocolPrefix)){
            setProtocolVersionToIdMap(protocolPrefix);
        }
    }

    /**
     * attempts to retrieve version and identifier information from a uri for the protocol
     */
    private static void setProtocolVersionToIdMap(String protocolPrefix){
        CodebookInfo codebookInfo = new CodebookInfo();
        protocolInfoMap.put(protocolPrefix, codebookInfo);

        String uri = ArtDecorCalls.getProjectIndexURI(protocolPrefix);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        logger.log(Level.INFO, "Attempting to retrieve which version of the codebook are available using {}", uri);

        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document dom = documentBuilder.parse(uri);
            //get the root element
            Element documentElement = dom.getDocumentElement();

            //get a nodelist of elements
            NodeList nodeList = documentElement.getElementsByTagName("dataset");

            logger.log(Level.INFO, "Found {} versions", nodeList.getLength());

            if(nodeList != null) {
                for(int i=0; i<nodeList.getLength(); i++) {
                    Element element = (Element) nodeList.item(i);
                    String version = element.getAttribute("versionLabel");
                    String id = element.getAttribute("id");
                    codebookInfo.addVersionId(version, id);
                    codebookInfo.addLanguages(version, findLanguages(element));

//                    datasetIdMap.put(element.getAttribute("versionLabel"), element.getAttribute("id"));

                    logger.log(Level.INFO, "versionlabel found: {} id found: {}", element.getAttribute("versionLabel"),element.getAttribute("id"));
                    findLanguages(element);
                }
            }
        } catch (Exception e){
            throw new RuntimeException("Exception occurred while attempting to retrieve which version are available for the codebook: "+e.getMessage());
        }
    }

    private static List<String> findLanguages(Element element){
        List<String> languages = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName("desc");
        if(nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element descElement = (Element) nodeList.item(i);
                String language = descElement.getAttribute("language");
                languages.add(language);
            }
        }
        return languages;
    }
}

class CodebookInfo{
    Map<String, List<String>> versionLanguageMap = new HashMap<>();
    List<String> uniqueLanguagesList = new ArrayList<>();
    Map<String, String> versionIdMap = new HashMap<>();

    void addVersionId(String version, String id){
        versionIdMap.put(version, id);
    }

    void addLanguages(String version, List<String> languages){
        versionLanguageMap.put(version, languages);
        for(String language:languages){
            if(!uniqueLanguagesList.contains(language)){
                uniqueLanguagesList.add(language);
            }
        }
    }

    String getId(String version){
        return versionIdMap.get(version);
    }

    List<String> getUniqueLanguages(){
        return uniqueLanguagesList;
    }
}
