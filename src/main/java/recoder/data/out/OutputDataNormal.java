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
import java.util.List;
import java.util.stream.Collectors;

/**
 * class used for output data
 */
public class OutputDataNormal extends OutputDataDefault{

    private List<List<String>> lines = new ArrayList<>();

    public OutputDataNormal(RunParameters runParameters){
        super(runParameters);
    }

    /**
     * store the original header and the translated header
     * @param origHeaderName    original headerName
     * @param translatedName    translated headerName
     */
    public void addHeaderValue(String origHeaderName, String translatedName, boolean housekeeping){
        headerList.add(new OutputHeaderItem(origHeaderName, translatedName));
    }

    /**
     * add a line to our lines
     * @param line    the line to add
     */
    public void addDataLine(List<String> line) {
        lines.add(line);
    }

    /**
     * write the data to a file
     */
    public void writeData(){
        String outFileName = runParameters.getDataOutFileName();
        try(BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName), "ISO-8859-1"))){
            // write the header; headerlist contains OutputHeaderItems
            bufferedWriter.write(headerList.stream().map(f-> f.getTranslatedName()).collect(Collectors.joining("\t"))+System.lineSeparator());

            // write the lines
            for(List<String> line:lines){
                bufferedWriter.write(line.stream().collect(Collectors.joining("\t"))+System.lineSeparator());
            }

        } catch (Exception e){
            throw new RuntimeException("A severe error occurred while writing the output file: "+e.getMessage());
        }
    }
}
