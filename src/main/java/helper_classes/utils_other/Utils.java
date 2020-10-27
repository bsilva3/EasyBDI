package helper_classes.utils_other;

import java.io.File;

public class Utils {

    private Utils(){}

    /**
     * Returns true if the string is numeric or a boolean (true or false), and flase if it is any string
     * @param strNum
     * @return
     */
    public static boolean stringIsNumericOrBoolean(String strNum) {
        if (strNum == null) {
            return false;
        }
        else if (strNum.equalsIgnoreCase("true") || strNum.equalsIgnoreCase("false"))
            return true;
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String getFileNameNoExtension(String filePath) {
        String[] split = filePath.split(File.pathSeparator);
        String filename = split[split.length-1];
        return filename.split("\\.")[0];
    }

    public static String getExtension(String filePath) {
        String[] split = filePath.split(File.pathSeparator);
        String filename = split[split.length-1];
        return filename.split("\\.")[1];
    }

    public static boolean hasExtension(String filePath){
        if (!filePath.contains("\\."))
            return false;
        String extension = filePath.substring(filePath.lastIndexOf(".") + 1);
        //Checks if there is any extension after the last . in the input
        if (!extension.isEmpty()) {
           return true;
        }
        return false;
    }
}
