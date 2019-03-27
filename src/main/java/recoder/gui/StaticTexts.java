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

package recoder.gui;

class StaticTexts {

    static String getAboutTitle(){
        return "About the PALGA Data Recoder...";
    }

    /**
     * returns the welcome text
     * @return the welcome text
     */
    static String getWelcomeText(){
        return "Welcome to the PALGA Data Recoder!\n\n" + getHelpText();
    }

    /**
     * returns the help text
     * @return the help text
     */
    static String getHelpText(){
        return  "Select a datafile, the codebook/protocol you wish to use, the type of coding you wish to translate to and the " +
                "type of output file you wish to generate.\n" +
                "In case of a TranSMART output file, please also select a transmart tree template and set the study name.\n" +
                "When all parameters are filled, press the run button.\n" +
                "Output is written to the directory that contains your datafile.\n\n";
    }

    /**
     * returns the about text
     * @return the about text
     */
    static String getAboutText(){
        return  "The PALGA Data Recoder was designed and created by:" +
                "\n\tSander de Ridder (NKI 2017; VUmc 2018/2019)\n"+
                "Testers & Consultants:" +
                "\n\tJeroen Belien (VUmc)\n" +
                "This project was sponsored by MLDS project OPSLAG and KWF project TraIT2Health-RI (WP: Registry-in-a-Box)\n\n" +
                "---------------------------------------------------------------------------------------------------------------------------------------------------------\n\n"+
                "Copyright 2017 NKI / AvL; 2018/2019 VUmc\n" +
                "\n" +
                "PALGARecoder is free software: you can redistribute it and/or modify\n" +
                "it under the terms of the GNU General Public License as published by\n" +
                "the Free Software Foundation, either version 3 of the License, or\n" +
                "(at your option) any later version.\n" +
                "\n" +
                "PALGARecoder is distributed in the hope that it will be useful,\n" +
                "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                "GNU General Public License for more details.\n" +
                "\n" +
                "You should have received a copy of the GNU General Public License\n" +
                "along with PALGARecoder. If not, see <http://www.gnu.org/licenses/>\n";
    }
}
