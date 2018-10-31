package de.k3b.io;

/**
 * Created by k3b on 17.02.2015.
 */
public class FileNameUtil {

    /** converts baseName to a valid filename. If it has no file-extension then defaultExtension is added */
    public static String createFileName(String baseName, String defaultExtension) {
        StringBuilder result = new StringBuilder(baseName);

        // remove trailing "."
        int len = result.length();
        while ((len > 0) && (result.charAt(len - 1) == '.')) {
            result.deleteCharAt(len - 1);
            len--;
        }

        // remove leading "."
        while ((len > 0) && (result.charAt(0) == '.')) {
            result.deleteCharAt(0);
            len--;
        }

        // add extension if there is none
        if ((defaultExtension != null) && (result.indexOf(".") < 0)) {
            result.append(".").append(defaultExtension);
        }

        // replace illegal chars with "_"
        replace(result, "_", "/", "\\", ":", " ", "?", "*", "&", ">", "<" , "|", "'", "\"", "__");
        return result.toString();
    }

    /** replaces all occurences of illegalValues in result by replacement */
    private static void replace(StringBuilder result, String replacement, String ... illegalValues) {
        for (String illegalValue : illegalValues) {
            int found = result.indexOf(illegalValue);
            while (found >= 0) {
                result.replace(found, found+illegalValue.length(), replacement);
                found = result.indexOf(illegalValue);
            }
        }
    }
}
