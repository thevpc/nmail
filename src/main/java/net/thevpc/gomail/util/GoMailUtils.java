package net.thevpc.gomail.util;

/**
 * Created by vpc on 7/5/16.
 */
public class GoMailUtils {
    public static boolean isTextPlainContentType(String contentType) {
        if (contentType != null) {
            for (String s : contentType.split(";")) {
                if (s.trim().equals("text/plain")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTextHtmlContentType(String contentType) {
        if (contentType != null) {
            for (String s : contentType.split(";")) {
                if (s.trim().equals("text/html")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isTextContentType(String contentType) {
        if (contentType != null) {
            for (String s : contentType.split(";")) {
                if (s.trim().startsWith("text/")) {
                    return true;
                }
            }
        }
        return false;
    }
}
