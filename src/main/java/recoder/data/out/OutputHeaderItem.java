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

/**
 * class which stores the original headername its translation and a repeat number
 * when the data is not written in wide-format, the repeat will always be 1; otherwise it will be >=1
 */
public class OutputHeaderItem {
    private String origHeaderName;
    private String translatedName;
    private int repeat = 1;

    OutputHeaderItem(String origHeaderName, String translatedName){
        this(origHeaderName, translatedName, 1);
    }

    OutputHeaderItem(String origHeaderName, String translatedName, int repeat){
        this.origHeaderName = origHeaderName;
        this.translatedName = translatedName;
        this.repeat = repeat;
    }

    /**
     * returns the original header name
     * @return the original header name
     */
    public String getOrigHeaderName() {
        return origHeaderName;
    }

    /**
     * returns this header entry's repeat number
     * @return the repeat number
     */
    public int getRepeat() {
        return repeat;
    }

    /**
     * returns the translated name
     * @return the translated name
     */
    String getTranslatedName() {
        return translatedName;
    }

}
