package com.upfinder.voicetodo.util;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: ywj
 * @date: 2019/2/23 16:55
 */
public class Utils {
    public static boolean isEmpty(Map<String, String> param) {
        return param == null || param.size() == 0;
    }

    public static boolean isEmpty(Object object) {
        return object == null ;
    }
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNumber(String calendarId) {
        try {
            Integer.parseInt(calendarId);
            return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Calendar parseStrToCld(String startDate) {
        Calendar instance = null;
        try {
            instance = Calendar.getInstance();
            instance.setTime(new Date(Long.parseLong(startDate)));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public static long getFormatCld(Calendar calendar) {
        return calendar.getTimeInMillis();
    }

    public static boolean isEmpty(List<Map<String, Object>> list) {
        return list == null || list.isEmpty();
    }
}
