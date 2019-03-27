#
SdR... ToDo / check: there are currently PALGA things in here, because when I built it, it was built for translating PALGA data. Maybe we can change some of the things? E.g. palgaColNameToConceptMap? And add some dropdown that asks: what is the source data? PALGA/Other; when PALGA it searches for the PALGA_COLNAME property and when Other user has to specify?

# The PALGA Recoder
The PALGA Recoder aims to translate PALGA Protocol Data to a different language using codebooks stored in Art-Decor. 

## Creating an executable jar
You can use maven to create an executable jar file, using mvn package. The jar is placed in the target directory and can be run using java -jar <generated_jar_file>

## Translating a file
The program currently supports two different modes: 1) create a translated text file, and 2) create a translated TranSMART ready file
In the first case, the aim is to simply create a translated version of the provided protocol data. 
In the second case, the aim is to provide a translated file which is ready for tranSMART. This also includes a tranSMART tree. The tree is based on the tree template for the specific protocol, which will ensure that every tree is standardised. 

### Usage parameters
Parameters in italic are only appicable if "transmart file" is selected.

| parameter | explanation | 
| --- | --- | 
| Protocol file | directory and name of the data file |
| Protocol | the palga protocol that was used to collect the data |
| Translate to | tekst file or transmart file |
| Output format | determines what the output file will show, e.g. tekst only / codes only / combinations |
| *studyName* | the name the study should have in transmart |
| *tranSMART tree template* | file which holds the tree template |
| *wide format* | whether the data should be exported in wide format |
| *id column* | column in the datafile which has the identifier |

## How does it work for PALGA
When the codebooks are created, each concept in the codebook is given a property called "PALGA_COLNAME". The value of this property matches the actual column name as found in the PALGA Protocol's data. This basically links the data file to the codebook. The Recoder uses the protocol selected by the user to fetch which codebooks are available online. It then retrieves the codebook versions when necessary. When the user selected tranSMART as output, the program also requires the user to select a tranSMART tree template. This template is applied to the translated data, ensuring the translated concept names are used in the tree. Furthermore, the program recognises the Roman numbers used by PALGA and can use them to expand the tree. The output of the program is a translated file and a tree file.   

## Remarks
* The program tries to find a version of the protocol that is specified in the datafile. If that version doesn't exist, the progam can't translate using the version and hence fails to translate the concept and values for these entries. 
* The program tries to translate the concepts and values, but to be able to do so, the entries have to be identical. So if the datafile contains "Yes, but" and the codebook contains "Yes but", the values are not identical and translation is not possible. In such a case the program reports the problem and writes the original value(s) to the output file.
* If a concept does not exist in a transmart tree template, the program gives ??????? in the output
* Obviously, quality of the translated data depends on the quallity of the codebooks. For example, if a codebook is mapped to e.g. SNOMED, the SNOMED codes can be retrieved; if an internal codesystem is used, only that id can be retrieved. 
* Although originally written for translating PALGA Protocol data, not much is preventing the tool from being usable for translating other datasets using other codebooks (assuming they are compatible). Basically all that is required is:
    * a link between the codebook and the datafile 
    * an entry in the list which links the codebook name to its Art-Decor identifier
    * the codebook to have a PALGA_COLNAME property. This name is currently still hardcoded in the code, but this could be turned into a textual property if desired 

--> SdR: check the second bullet ??????? 

## About
"The PALGA Data Recoder was designed and created by **Sander de Ridder** (NKI 2017; VUmc 2018/2019)<br>
Testers & Consultants:	Jeroen Belien (VUmc)<br>
This project was sponsored by MLDS project OPSLAG and KWF project TraIT2Health-RI (WP: Registry-in-a-Box)<br>

The PALGA Recoder is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

The PALGA Recoder is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
