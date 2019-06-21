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

package recoder.settings;

import java.util.*;

/**
 * stores some global settings
 */
public class GlobalSettings {
    public static String server = "http://decor.nictiz.nl/services/";
    private static Map<String, String> protocolNameToPrefixMap = new TreeMap<>();
    private static List<String> languageList = Arrays.asList("nl-NL", "en-US");

    // maybe read from file instead?
    static{
        protocolNameToPrefixMap.put("colonbiopt", "ppcolbio-");
        protocolNameToPrefixMap.put("colonrectum carcinoom", "ppcolcar-");
        protocolNameToPrefixMap.put("inherit_test", "s2nki-");
    }

    public static String getDefaultProtocolName(){
        return "colonbiopt";
    }

    public static List<String> getLanguageList(){
        return languageList;
    }

    /**
     * get all the protocol available
     * @return a set with all the protocols
     */
    public static Set<String> getProtocols(){
        return protocolNameToPrefixMap.keySet();
    }

    /**
     * get a prefix of a protocol, which is necessary for the art-decor calls
     * @param protocolName    name of the protocol
     * @return  prefix of the protocol
     */
    public static String getProtocolPrefix(String protocolName){
        return protocolNameToPrefixMap.get(protocolName);
    }

}
