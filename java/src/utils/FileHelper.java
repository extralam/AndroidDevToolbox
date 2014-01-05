package utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Created by AtomInvention on 18/11/13.
 */
public class FileHelper {

    public static URL getFxmlUrl(Class callingClass, String fxmlFileName) {
        return  callingClass.getClassLoader().getResource(
                AppConfig.FXML_PACKAGE + File.separator + fxmlFileName);
    }

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat(
                "#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean isFileExist(String filePath) {
        return new File(filePath).exists();
    }

    public static void makeFolder(String folderPath) {
        File dir = new File(folderPath);
        dir.mkdirs();
    }

    public static void makeFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyFile(String originalPath, String destPath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(originalPath);
            outputStream = new FileOutputStream(destPath);
            copyFile(inputStream, outputStream);
            inputStream.close();
            inputStream = null;
            outputStream.flush();
            outputStream.close();
            outputStream = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static String readFileAsString(String filePath) {
        String fileContent = "";
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            fileContent = IOUtils.toString(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return fileContent;
    }

    public static void saveToFile(String fileContent, String outputPath) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(outputPath);
            IOUtils.write(fileContent, fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
