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

import recoder.settings.RunParameters;
import recoder.utils.enumerate.OutputFileType;

/**
 * Factory for creating input data
 */
public class InputDataFactory {

    /**
     * get the appropriate object, depending on the runsettings
     * @param runParameters    the settings for this run
     * @return a dataset for text or a datset for transmart
     */
    public static InputData getInputData(RunParameters runParameters){
        OutputFileType outputFileType = runParameters.getOutputFileType();
        if(outputFileType.equals(OutputFileType.TEXT)){
            return PALGADatasetText.createDataset(runParameters);
        }
        else if(outputFileType.equals(OutputFileType.TRANSMART)){
            return PALGADatasetForTM.createDataset(runParameters);
        }
        return null;
    }
}
