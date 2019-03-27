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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * stores which messages were logged by a class, to prevent some messages from appearing multiple times
 */
public class LogTracker {
    private static Map<Class, List<String>> alreadyLoggedMap = new HashMap<>();

    /**
     * returns whether a messages may ben shown. If it has been shown before, returns false
     * @param aClass     class which wants to show the message
     * @param message    the message which a class wants to show
     * @return  true/false
     */
    private static boolean mayLogMessage(Class aClass, String message) {
        if (!alreadyLoggedMap.containsKey(aClass)) {
            alreadyLoggedMap.put(aClass, new ArrayList<>());
        }
        List<String> loggedMessages = alreadyLoggedMap.get(aClass);
        if (loggedMessages.contains(message)) {
            return false;
        }
        else {
            loggedMessages.add(message);
            return true;
        }
    }

    /**
     * uniquely log a message to prevent an overkill of repetitive messages
     * @param aClass    class which wishes to log a message
     * @param message   the message the class wishes to log
     */
    public static void logMessage(Class aClass, String message) {
        Logger logger = LogManager.getLogger(aClass.getName());
        if(mayLogMessage(aClass, message)){
            logger.log(Level.ERROR, message);
        }
    }

    public static void clearLog(){
        alreadyLoggedMap.clear();
    }
}