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

import java.util.ArrayList;
import java.util.List;

/**
 * Defaults for OutputData
 */
abstract class OutputDataDefault implements OutputData{
    List<OutputHeaderItem> headerList = new ArrayList<>();
    RunParameters runParameters;

    OutputDataDefault(RunParameters runParameters){
        this.runParameters = runParameters;
    }

    /**
     * returns list with OutputHeaderItems, providing us with a complete list of all the concepts in the output
     * this information we can then use in the transmart manager to find the indices
     * @return the original header as list
     */
    public List<OutputHeaderItem> getHeaderList(){
        return headerList;
    }
}
