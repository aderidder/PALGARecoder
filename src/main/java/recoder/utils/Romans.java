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

package recoder.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * class that helps out with the fairly horrible PALGA headernames which can contain roman numbers for repeats
 * The idea is as follows:
 * PALGA's column name may end with a roman number. Unfortunately they haven't used a splitter, so myColiii may imply
 * the column's name is myCol iii, myColi ii, myColii i or myColiii.
 * We first try to see with which Roman numbers our value may be ending
 * We then sort the matches by length, as the next step will be to check whether the column without the roman number is
 * in the codebook. Hence we first wish to try myCol, and if that fails, myColi (meaning the number would be ii), etc.
 */
public class Romans {
    private static final Map<String, String> romanToOutput = new HashMap<>();
    private static final List<String> romanNumbers = new ArrayList<>();
    private static final Comparator<String> comparator = new Romans().new ListByLengthComparator();

    static{
        // first 20 roman numbers, which is the max that PALGA uses
        romanNumbers.add("I");  romanNumbers.add("II"); romanNumbers.add("III"); romanNumbers.add("IV"); romanNumbers.add("V");
        romanNumbers.add("VI"); romanNumbers.add("VII"); romanNumbers.add("VIII"); romanNumbers.add("IX"); romanNumbers.add("X");
        romanNumbers.add("XI"); romanNumbers.add("XII"); romanNumbers.add("XIII"); romanNumbers.add("XIV"); romanNumbers.add("XV");
        romanNumbers.add("XVI"); romanNumbers.add("XVII"); romanNumbers.add("XVIII"); romanNumbers.add("XIX"); romanNumbers.add("XX");


        romanToOutput.put("I", ""); romanToOutput.put("II", "2"); romanToOutput.put("III", "3");
        romanToOutput.put("IV", "4"); romanToOutput.put("V", "5"); romanToOutput.put("VI", "6");
        romanToOutput.put("VII", "7"); romanToOutput.put("VIII", "8"); romanToOutput.put("IX", "9");
        romanToOutput.put("X", "10"); romanToOutput.put("XI", "11"); romanToOutput.put("XII", "12");
        romanToOutput.put("XIII", "13"); romanToOutput.put("XIV", "14"); romanToOutput.put("XV", "15");
        romanToOutput.put("XVI", "16"); romanToOutput.put("XVII", "17"); romanToOutput.put("XVIII", "18");
        romanToOutput.put("XIX", "19"); romanToOutput.put("XX", "20"); romanToOutput.put("", "");
    }

    /**
     * Method to check whether a value ends with a roman number
     * Maybe a nicer way to do this would be via a regular expression, but I couldn't think of a proper expression for
     * this within reasonable time, so I'm using a list with numbers instead.
     * @param value    the headername which may contain a roman number
     * @return  list of possible romans
     */
    public static List<String> romanNumberMatch(String value){
        // create a list of the roman numbers that our value ends with
        List<String> matchedRomans = romanNumbers.stream().parallel().filter(t->value.toUpperCase().endsWith(t)).sequential().collect(Collectors.toList());
        // sort this list by length
        Collections.sort(matchedRomans, comparator);
        return matchedRomans;
    }

    /**
     * transforms a roman number to a normal number
     * @param roman    the roman representation
     * @return the number representation
     */
    public static String getRomanOutputString(String roman){
        return romanToOutput.get(roman).trim();
    }

    /**
     * comparator for sorting by string length
     */
    private class ListByLengthComparator implements Comparator<String>{
        @Override
        public int compare(String o1, String o2) {
            if(o1.length()>o2.length()) return -1;
            else if (o1.length()<o2.length()) return 1;
            return 0;
        }
    }
}

