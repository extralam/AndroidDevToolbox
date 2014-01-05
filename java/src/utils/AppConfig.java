package utils;

import java.io.File;

/**
 * Created by AtomInvention on 18/11/13.
 */
public class AppConfig {

    public static final String FXML_PACKAGE = "fxmls";
    public static final String RES_PACKAGE = "res";
    public static final String APP_CSS_PATH = RES_PACKAGE + File.separator + "AppTheme.css";
    public static final String APP_ICON_PATH = RES_PACKAGE + File.separator + "app_icon.png";
    public static final String STATEFUL_TEMP_PATH = RES_PACKAGE + File.separator + "template_stateful.xml";

    public static final String FOLDER_RESIZE_PREFIX = "resize";
    public static final String FOLDER_APP_ICON_RESIZE_PREFIX = "app_icon";
    public static final String FOLDER_STATEFUL_PREFIX = "stateful";
    public static final String FOLDER_DIMEN_SCALE_PREFIX = "scale";

    public static final String FOLDER_DRAWABLE_MDPI = "drawable-mdpi";
    public static final String FOLDER_DRAWABLE_HDPI = "drawable-hdpi";
    public static final String FOLDER_DRAWABLE_XHDPI = "drawable-xhdpi";
    public static final String FOLDER_DRAWABLE_XXHDPI = "drawable-xxhdpi";
    public static final String FOLDER_DRAWABLE_XXXHDPI = "drawable-xxxhdpi";

}
