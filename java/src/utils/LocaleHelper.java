package utils;

import java.util.Locale;

/**
 * Created by AtomInvention on 18/11/13.
 */
public class LocaleHelper {

    public static Locale getLocale() {
        Locale returnLocale = Locale.getDefault();
        if (returnLocale != Locale.TRADITIONAL_CHINESE) {
            return Locale.ENGLISH;
        } else {
            return Locale.TRADITIONAL_CHINESE;
        }
    }

}