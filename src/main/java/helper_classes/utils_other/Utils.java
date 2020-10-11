package helper_classes.utils_other;

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
}
