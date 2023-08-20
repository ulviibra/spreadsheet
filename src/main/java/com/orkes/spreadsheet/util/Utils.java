package com.orkes.spreadsheet.util;

import java.util.regex.Pattern;

public class Utils {
    public static boolean isInteger(String str) {
        return Pattern.matches("-?\\d+", str);
    }
}
