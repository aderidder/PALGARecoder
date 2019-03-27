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

package recoder.utils.enumerate;

/**
 * types we can produce as output
 * text --> PALGADatasetText
 * transmart --> PALGADatasetForTM
 */
public enum OutputFileType {
    TEXT ("Text file"),
    TRANSMART ("TranSMART");

    private final String prettyString;

    OutputFileType(String prettyString){
        this.prettyString = prettyString;
    }

    public String getPrettyString(){
        return prettyString;
    }

    public static OutputFileType getEnum(String prettyString){
        for(OutputFileType outputFileType: OutputFileType.values()){
            if(outputFileType.prettyString.equalsIgnoreCase(prettyString)){
                return outputFileType;
            }
        }
        return null;
    }
}
