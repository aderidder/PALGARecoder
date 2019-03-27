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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import recoder.utils.ArtDecorCalls;
import recoder.utils.LogTracker;
import recoder.utils.enumerate.OutputFormatType;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

/**
 * DecorCodebook for a single version of a PALGA Protocol
 *
 * PALGA_COLNAME defines the column name as it is found in a PALGA recoder.data export and is a property
 * in the art-decor dataset. This is the link between the recoder.data and the recoder.codebook
 * The toplevel concepts have id which is linked to the terminologyAssociation. Using this, we can recode our
 * concept (the PALGA_COLNAME) to a code or displayName.
 * These concepts (may) have conceptLists, which contain concepts. We need these concepts to translate
 * the values. The concepts contain a code, codesystem and displayName. Furthermore it contains a designation
 * tag with type "preferred", which contains the value as found in the PALGA dataexport. So this means we can
 * translate the preferred displayName to the code or displayName.
 *
 * To summarise:
 * Concept translation = PALGA_COLNAME --> terminologyAssociation code / displayName
 * Concept Value translation = preferred displayName --> concept code / displayName
 */
class DecorCodebook {
    private Map<String, Concept> palgaColNameToConceptMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private Map<String, Concept> idToConceptMap = new HashMap<>();
    private String version;

    /**
     * new Decor codebook
     * @param languageFrom source language (at the moment always nl-NL)
     * @param datasetId    identifier which can get us the appropriate codebook online
     * @param version      version of the codebook
     */
    DecorCodebook(String languageFrom, String datasetId, String version){
        this.version = version;
        createCodebook(ArtDecorCalls.getRetrieveDatasetURI(datasetId, languageFrom));
    }

    /**
     * translate a value
     * @param outputType type of output format desired
     * @param value      value to translated
     * @param headerName the headername of the concept to which the value belongs
     * @return  translated value
     */
    String translateConceptValue(OutputFormatType outputType, String value, String headerName) {
        Concept concept;
        String translatedValue = value;
        try{
            if(palgaColNameToConceptMap.containsKey(headerName)) {
                concept = palgaColNameToConceptMap.get(headerName);
                translatedValue = concept.translateValue(value, outputType);
            }
            else {
                throw new Exception("Headername "+headerName + "doesn't exist. ");
            }
        } catch (Exception e){
            String message = e.getMessage() + " Codebook version "+version+". Value will not be translated.";
            LogTracker.logMessage(this.getClass(), message);
        }
        return translatedValue;
    }

    /**
     * translate the concept
     * @param outputType type of output format desired
     * @param headerName the headerName to translate
     * @return  translated headerName
     */
    String translateConcept(OutputFormatType outputType, String headerName) {
        Concept concept;
        String translatedHeader = headerName;
        try{
            if(palgaColNameToConceptMap.containsKey(headerName)) {
                concept = palgaColNameToConceptMap.get(headerName);
                translatedHeader = concept.translateHeaderName(outputType);
            }
            else {
                throw new Exception("Headername "+headerName + "doesn't exist. ");
            }
        } catch (Exception e){
            String message = e.getMessage() + " Codebook version "+version+". Headername will not be translated.";
            LogTracker.logMessage(this.getClass(), message);
        }
        return translatedHeader;
    }

    /**
     * checks whether the headerName exists in this codebook
     * @param headerName the headerName to check
     * @return true/false
     */
    boolean containsHeaderName(String headerName) {
        if(!palgaColNameToConceptMap.containsKey(headerName)) {
            LogTracker.logMessage(this.getClass(), "The headername " + headerName + " does not exist in the codebook (version " + version + "). Concept and values for this concept will not be translated.");
            return false;
        }
        return true;
    }

    /**
     * attempts to create a codebook which is stored at a specific uri
     * @param uri contains the source of the codebook
     */
    private void createCodebook(String uri){
        LogTracker.logMessage(this.getClass(), "Retrieving a codebook using "+uri);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document dom = db.parse(uri);
            //get the root element
            Element rootElement = dom.getDocumentElement();
            createBook(rootElement);
        } catch (Exception e){
            String message = "There was an issue retrieving data using the following uri: "+uri+"\nPerhaps it doesn't exist yet?";
            LogTracker.logMessage(this.getClass(), message);
        }
    }

    /**
     * parses the codebook starting from the root element
     * @param rootElement the rootelement
     * @throws Exception
     */
    private void createBook(Element rootElement) throws Exception {
        List<Element> conceptList = getChildElementsWithName(rootElement, "concept");

        for (Element conceptElement : conceptList) {
            if(isGroupElement(conceptElement)){
                createBook(conceptElement);
            }

            if (hasValidStatusCode(conceptElement)) {
                handleConceptElement(conceptElement);
            }
        }
    }

    /**
     * Handles a concept element, checking for the palga column name, creating the concept, etc.
     * @param conceptElement the concept element from the xml file
     * @throws Exception
     */
    private void handleConceptElement(Element conceptElement) throws Exception{
        String palgaColName = getPalgaColNameAttributeValue(conceptElement);
        if(!palgaColName.equalsIgnoreCase("")){
            Concept concept = createConcept(conceptElement, palgaColName);
            palgaColNameToConceptMap.put(palgaColName, concept);
            idToConceptMap.put(concept.getConceptId(), concept);
            addTerminology(conceptElement);
        }
    }

    /**
     * adds terminology associations to concepts
     * @param rootElement the root element
     * @throws Exception when something goes wrong
     */
    private void addTerminology(Element rootElement) throws Exception{
        List<Element> terminologyAssociationList = getChildElementsWithName(rootElement, "terminologyAssociation");
        for(Element terminologyAssociationElement:terminologyAssociationList){
            String conceptId = getAttributeValue(terminologyAssociationElement, "conceptId");
            if(idToConceptMap.containsKey(conceptId)){
                Concept concept = idToConceptMap.get(conceptId);
                String conceptCode = terminologyAssociationElement.getAttribute("code");
                String codeSystemName = terminologyAssociationElement.getAttribute("codeSystemName");
                String displayName = terminologyAssociationElement.getAttribute("displayName");
                concept.addConceptTerminology(conceptCode, codeSystemName, displayName);
            }
        }
    }

    /**
     * creates a new concept from a conceptElement
     * @param conceptElement a concept element
     * @param palgaColName   the name of the item in PALGA
     * @return the newly created concept
     */
    private Concept createConcept(Element conceptElement, String palgaColName){
        String conceptId = getAttributeValue(conceptElement, "id");
        Concept concept = new Concept(conceptId, palgaColName);

        addValueSet(concept, conceptElement);

        return concept;
    }

    /**
     * adds a value set to a concept, if one exists
     * @param concept        the concept to which to add the value set
     * @param conceptElement the xml concept element in which to look for the valueset
     */
    private void addValueSet(Concept concept, Element conceptElement){
        Element valueSetElement = getChildElementWithName(conceptElement, "valueSet");
        if(valueSetElement!=null) {
            Element conceptListElement = getChildElementWithName(valueSetElement, "conceptList");
            // elements are in the concept tag, but may also be in the exception tag (NULLFlavors)
            addValueSet(concept, getChildElementsWithName(conceptListElement, "concept"));
            addValueSet(concept, getChildElementsWithName(conceptListElement, "exception"));
        }
    }

    /**
     * adds elements from a list to the concept list
     * @param concept   the concept to which to add the value set
     * @param elements  list with elements
     */
    private void addValueSet(Concept concept, List<Element> elements){
        for (Element entryElement : elements) {
            String valueCode = getAttributeValue(entryElement, "code");
            String valueCodeSystem = getAttributeValue(entryElement, "codeSystemName");
            String valueDisplayName = getAttributeValue(entryElement, "displayName");

            String textInLanguage = getTextInLanguage(entryElement);
            concept.addConceptListItem(valueCode, valueCodeSystem, valueDisplayName, textInLanguage);
        }
    }

    /**
     * returns the preferred name of the value. This is used in art-decor and is the value as it appears in PALGA
     * @param entryElement xml element of a value set entry
     * @return the value as it appears in PALGA
     */
    private static String getTextInLanguage(Element entryElement){
        String textInLanguage="";
        List<Element> designationElements = getChildElementsWithName(entryElement, "designation");
        for(Element designationElement:designationElements) {
            String designationType = getAttributeValue(designationElement, "type");
            if(designationType.equalsIgnoreCase("preferred")){
                textInLanguage = designationElement.getAttribute("displayName");
            }
        }
        return textInLanguage;
    }

    /**
     * returns the value of the COL_NAME property for the concept element
     * @param conceptElement the xml of the concept, in which to look for the property
     * @return  the PALGA_COLNAME value or empty string
     */
    private static String getPalgaColNameAttributeValue(Element conceptElement){
        List<Element> propertyElementsList = getChildElementsWithName(conceptElement, "property");
        for(Element propertyElement:propertyElementsList){
            String attributeValue = getAttributeValue(propertyElement, "name");
            if(attributeValue.equalsIgnoreCase("PALGA_COLNAME")){
                return propertyElement.getTextContent().trim();
            }
        }
        return "";
    }

    /**
     * returns the value of an xml attribute
     * @param element       element which contains the attribute
     * @param attributeName name of the attribute
     * @return the value of an xml attribute
     */
    private static String getAttributeValue(Element element, String attributeName){
        return element.getAttribute(attributeName);
    }

    /**
     * checks whether this element is of interest for us in our codebook, which depends on the statuscode of the element;
     * we're only interested in items which are "draft" or "final"
     * @param element the element to check
     * @return true/false
     */
    private static boolean hasValidStatusCode(Element element){
        String statusCode = element.getAttribute("statusCode");
        return statusCode.equalsIgnoreCase("draft") || statusCode.equalsIgnoreCase("final");
    }

    /**
     * returns the first child element with a certain name
     * @param element the parent element
     * @param name    name of the element we're looking for
     * @return the child element
     */
    private static Element getChildElementWithName(Element element, String name){
        NodeList nodeList = element.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && ((Element) node).getTagName().equals(name)) {
                return (Element) node;
            }
        }
        return null;
    }

    /**
     * returns a list of all child elements with a certain name
     * @param element parent element
     * @param name    name to look for
     * @return list of children
     */
    private static List<Element> getChildElementsWithName(Element element, String name){
        List<Element> elementsWithName = new ArrayList<>();
        NodeList nodeList = element.getChildNodes();
        for (int i=0; i<nodeList.getLength(); i++){
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE
                    && ((Element) node).getTagName().equals(name)) {
                elementsWithName.add((Element) node);
            }
        }
        return elementsWithName;
    }

    /**
     * checks whether an element is a group element
     * @param conceptElement the element to check
     * @return true/false
     */
    private static boolean isGroupElement(Element conceptElement){
        String elementType = getAttributeValue(conceptElement, "type");
        return elementType.equalsIgnoreCase("group");
    }
}

