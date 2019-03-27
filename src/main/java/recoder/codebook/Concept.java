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

import recoder.utils.enumerate.OutputFormatType;

import java.util.HashMap;
import java.util.Map;

/**
 * Concepts are the main building blocks for the codebook
 * Each PALGA Concept in the codebook is stored in one Concept object
 * Concepts can contain valueLists (stores values and their mappings to e.g. SNOMED codes) and terminology
 * (stores the e.g. SNOMED code for this concept)
 */
class Concept {
    private Map<String, ConceptListItem> valuesMap = new HashMap<>();
    private ConceptTerminology conceptTerminology;
    private String conceptId;
    private boolean hasConceptListItems=false;
    private String palgaColName;

    /**
     * create a new Concept
     * @param conceptId    the identifier in art-decor
     * @param palgaColName column name in the PALGA dataset for this concept
     */
    Concept(String conceptId, String palgaColName) {
        this.conceptId = conceptId;
        this.palgaColName = palgaColName;
    }

    /**
     * returns the identifier art-decor gave to the concept
     * @return the identifier art-decor gave to the concept
     */
    String getConceptId() {
        return conceptId;
    }

    /**
     * attempts to translate a value, returning it in the desired output format
     * @param value the value to translate
     * @param type  the outputformat type
     * @return  a translated value or the original value
     * @throws Exception
     */
    String translateValue(String value, OutputFormatType type) throws Exception{
        if(!hasConceptListItems || value.equalsIgnoreCase("")) {
            return value;
        }
        if(!valuesMap.containsKey(value)){
            throw new Exception("value \""+value+"\" ("+palgaColName+") doesn't seem to exist.");
        }
        return valuesMap.get(value).getTranslation(type);
    }

    /**
     * attempts to translate the header
     * @param outputType the output format type
     * @return  translated header
     * @throws Exception
     */
    String translateHeaderName(OutputFormatType outputType) throws Exception {
        return conceptTerminology.getTranslation(outputType);
    }

    /**
     * add an item to the concept list items. This basically means this concept has one or more option values
     * and we're now adding one of these option values and its translation
     * @param valueCode        code of the value (e.g. some SNOMED code)
     * @param valueCodeSystem  codesystem the code belongs to (e.g. SNOMED)
     * @param valueDisplayName textual representation of the code
     * @param value            the value as it appears in the PALGA exports
     */
    void addConceptListItem(String valueCode, String valueCodeSystem, String valueDisplayName, String value) {
        hasConceptListItems = true;
        valuesMap.put(value, new ConceptListItem(valueCode, valueCodeSystem, valueDisplayName));
    }

    /**
     * add a translation for the concept itself
     * @param conceptCode    code of the concept (e.g. some SNOMED code)
     * @param codeSystemName codesystem the code belongs to (e.g. SNOMED)
     * @param displayName    textual representation of the code
     */
    void addConceptTerminology(String conceptCode, String codeSystemName, String displayName) {
        conceptTerminology = new ConceptTerminology(conceptCode, codeSystemName, displayName);
    }

    /**
     * class which defines a single concept list item (one option's code, description and codesystem)
     */
    private class ConceptListItem {
        private String valueCode;
        private String valueCodeSystem;
        private String valueDisplayName;

        /**
         * new conceptlistitem
         * @param valueCode        code of the value
         * @param valueCodeSystem  codesystem of the value
         * @param valueDisplayName textual representation of the value
         */
        private ConceptListItem(String valueCode, String valueCodeSystem, String valueDisplayName) {
            this.valueCode = valueCode;
            this.valueCodeSystem = valueCodeSystem;
            this.valueDisplayName = valueDisplayName;
        }

        /**
         * returns the translation, based on the output format type
         * @param type output format type
         * @return  translated value
         * @throws Exception
         */
        private String getTranslation(OutputFormatType type) throws Exception{
            switch(type){
                case CODES:
                    return valueCode;
                case CODES_AND_DESCRIPTIONS:
                    return valueCode+":"+valueDisplayName;
                case CODESYSTEM_AND_CODES:
                    return valueCodeSystem+":"+valueCode;
                case DESCRIPTIONS:
                    return valueDisplayName;
                case CODESYSTEM_AND_CODES_AND_DESCRIPTIONS:
                    return valueCodeSystem+":"+valueCode+":"+valueDisplayName;
                default:
                    throw new Exception("type "+type+" does not exist");
            }
        }

    }

    /**
     * class which stores the terminology for the concept
     * it basically stores the translation of the concept
     */
    private class ConceptTerminology {
        private String conceptCode;
        private String conceptCodeSystem;
        private String conceptDisplayName;

        /**
         * new terminology
         * @param conceptCode       code of the concept
         * @param conceptCodeSystem codesystem of the concept
         * @param displayName       textual representation of the concept
         */
        private ConceptTerminology(String conceptCode, String conceptCodeSystem, String displayName) {
            this.conceptCode = conceptCode;
            this.conceptCodeSystem = conceptCodeSystem;
            this.conceptDisplayName = displayName;
        }

        /**
         * returns the translation, based on the output format type
         * @param type output format type
         * @return  translated value
         * @throws Exception
         */
        private String getTranslation(OutputFormatType type) throws Exception{
            switch(type){
                case CODES:
                    return conceptCode;
                case CODES_AND_DESCRIPTIONS:
                    return conceptCode+":"+conceptDisplayName;
                case CODESYSTEM_AND_CODES:
                    return conceptCodeSystem+":"+conceptCode;
                case DESCRIPTIONS:
                    return conceptDisplayName;
                case CODESYSTEM_AND_CODES_AND_DESCRIPTIONS:
                    return conceptCodeSystem+":"+conceptCode+":"+conceptDisplayName;
                default:
                    throw new Exception("type "+type+" does not exist");
            }
        }
    }
}
