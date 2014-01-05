package AndroidDevToolbox;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import utils.AppConfig;
import utils.FileHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class AppIconResizeController extends BaseController implements Initializable {

    private static final String APP_FILE_NAME = "ic_launcher";

    public Button fileSelectBrowseBtn;
    public TextField singleFileTextField;
    public GridPane imageFileInfoPane;
    public GridPane selectImageNoticePane;
    public ImageView fileInfoImageView;
    public Label imageWidthLabel;
    public Label imageHeightLabel;
    public Label fileSizeLabel;
    public Button outputSelectBrowseBtn;
    public TextField outputFolderTextField;
    public Button imageResizeBtn;

    private ResourceBundle resourceBundle;
    private ImageResizeService imageResizeService;

    private String outputFolderPath;
    private String imageFilePath;
    private boolean imageFileSelected;
    private boolean outputFolderSelected;

    private interface AppIconSize {
        int M_DPI = 48;
        int H_DPI = 72;
        int XH_DPI = 96;
        int XXH_DPI = 144;
        int XXXH_DPI = 192;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
//        fileInfoImageView.fitWidthProperty().bind(imageFileInfoPane.widthProperty());
//        fileInfoImageView.fitHeightProperty().bind(imageFileInfoPane.heightProperty());
        updateFileInfoContent(null);
        imageResizeService = new ImageResizeService();
    }

    public void browseForImageFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resourceBundle.getString("ResizeSelectImageFile"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.*"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"),
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );

        // open file dialog
        File file = fileChooser.showOpenDialog(fileSelectBrowseBtn.getScene().getWindow());

        if (file != null) {
            singleFileTextField.setText(file.getPath());
            imageFilePath = file.getPath();
            imageFileSelected = true;
            updateStartBtnState();
            updateFileInfoContent(file);
        }
    }

    public void browseForOutputFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(resourceBundle.getString("ResizeSelectOutputFolder"));
        File outputFolder = directoryChooser.showDialog(outputSelectBrowseBtn.getScene().getWindow());

        if (outputFolder != null) {
            outputFolderTextField.setText(outputFolder.getPath());
            outputFolderSelected = true;
            outputFolderPath = outputFolder.getPath();
            updateStartBtnState();
        }
    }

    public void startResizeImage() {
        imageResizeService.restart();
    }

    private boolean isImageFileExtension(String fileName) {
        return FilenameUtils.getExtension(fileName).equalsIgnoreCase("JPG")
                || FilenameUtils.getExtension(fileName).equalsIgnoreCase("PNG");
    }

    private void updateFileInfoContent(File file) {
        if (file == null) {
            if (imageFileInfoPane != null && selectImageNoticePane != null) {
                imageFileInfoPane.setVisible(false);
                selectImageNoticePane.setVisible(true);
            }
            return;
        }

        imageFileInfoPane.setVisible(true);
        selectImageNoticePane.setVisible(false);

        Image image = new Image("file:" + file.getPath());
        fileInfoImageView.setImage(image);

        imageWidthLabel.setText(
                resourceBundle.getString("ResizeFileWidth") + " " + (int)image.getWidth() + "px");
        imageHeightLabel.setText(
                resourceBundle.getString("ResizeFileHeight") + " " + (int)image.getHeight() + "px");
        fileSizeLabel.setText(
                resourceBundle.getString("ResizeFileSize") + " " +
                        FileHelper.readableFileSize(file.length()));
    }

    private void updateStartBtnState() {
        if (imageFileSelected && outputFolderSelected) {
            imageResizeBtn.setDisable(false);
        } else {
            if (imageResizeBtn != null) {
                imageResizeBtn.setDisable(true);
            }
        }
    }

    private class ImageResizeService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            Task task =  new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage(resourceBundle.getString("ResizeStarted"));

                    // create folders
                    String rootFolder = outputFolderPath + File.separator +
                            AppConfig.FOLDER_APP_ICON_RESIZE_PREFIX + "_" + System.currentTimeMillis();
                    FileHelper.makeFolder(rootFolder);

                    String mdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_MDPI;
                    String hdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_HDPI;
                    String xhdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_XHDPI;
                    String xxhdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_XXHDPI;
                    String xxxhdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_XXXHDPI;

                    FileHelper.makeFolder(mdpiPath);
                    FileHelper.makeFolder(hdpiPath);
                    FileHelper.makeFolder(xhdpiPath);
                    FileHelper.makeFolder(xxhdpiPath);
                    FileHelper.makeFolder(xxxhdpiPath);

                    String fileExt = FilenameUtils.getExtension(new File(imageFilePath).getName());

                    // resize app icon
                    BufferedImage bufferedImage = null;
                    try {
                        bufferedImage = ImageIO.read(new File(imageFilePath));

                        int sourceWidth = bufferedImage.getWidth();
                        int sourceHeight = bufferedImage.getHeight();

                        if (sourceHeight != sourceWidth) {
                            int padding = Math.abs(sourceHeight - sourceWidth);
                            int halfPadding = (int) (padding / 2.0f);
                            bufferedImage = Scalr.pad(bufferedImage, padding, new Color(0, 0, 0, 0), null);
                            int newWidth = bufferedImage.getWidth();
                            int newHeight = bufferedImage.getHeight();

                            boolean isWidthLarger = sourceWidth > sourceHeight;
                            int maxSize = isWidthLarger ? sourceWidth : sourceHeight;
                            int cropStartX = (int) (isWidthLarger?
                                    (newWidth-sourceWidth)/2.0f:halfPadding);
                            int cropStartY = (int) (isWidthLarger?
                                    halfPadding:(newHeight-sourceHeight)/2.0f);

                            bufferedImage = Scalr.crop(
                                    bufferedImage, cropStartX, cropStartY, maxSize, maxSize, null);
                        }

                        BufferedImage resizedImage = Scalr.resize(bufferedImage
                                , Scalr.Method.QUALITY
                                , AppIconSize.M_DPI
                                , AppIconSize.M_DPI);

                        ImageIO.write(resizedImage, fileExt
                                , new File(mdpiPath + File.separator + APP_FILE_NAME + "." + fileExt));

                        resizedImage = Scalr.resize(bufferedImage
                                , Scalr.Method.QUALITY
                                , AppIconSize.H_DPI
                                , AppIconSize.H_DPI);

                        ImageIO.write(resizedImage, fileExt
                                , new File(hdpiPath + File.separator + APP_FILE_NAME + "." + fileExt));

                        resizedImage = Scalr.resize(bufferedImage
                                , Scalr.Method.QUALITY
                                , AppIconSize.XH_DPI
                                , AppIconSize.XH_DPI);

                        ImageIO.write(resizedImage, fileExt
                                , new File(xhdpiPath + File.separator + APP_FILE_NAME + "." + fileExt));

                        resizedImage = Scalr.resize(bufferedImage
                                , Scalr.Method.QUALITY
                                , AppIconSize.XXH_DPI
                                , AppIconSize.XXH_DPI);

                        ImageIO.write(resizedImage, fileExt
                                , new File(xxhdpiPath + File.separator + APP_FILE_NAME + "." + fileExt));

                        resizedImage = Scalr.resize(bufferedImage
                                , Scalr.Method.QUALITY
                                , AppIconSize.XXXH_DPI
                                , AppIconSize.XXXH_DPI);

                        ImageIO.write(resizedImage, fileExt
                                , new File(xxxhdpiPath + File.separator + APP_FILE_NAME + "." + fileExt));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    updateMessage(resourceBundle.getString("ResizeCompleted"));
                    return null;
                }
            };
            getStatusBarLabel().textProperty().bind(task.messageProperty());
            return task;
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            getStatusBarLabel().textProperty().unbind();
            if (getBackgroundTaskExecutionListener() != null) {
                getBackgroundTaskExecutionListener().taskComplete();
            }
            imageResizeBtn.setDisable(false);
            fileSelectBrowseBtn.setDisable(false);
            outputSelectBrowseBtn.setDisable(false);
        }

        @Override
        protected void running() {
            super.running();
            if (getBackgroundTaskExecutionListener() != null) {
                getBackgroundTaskExecutionListener().taskStart();
            }
            imageResizeBtn.setDisable(true);
            fileSelectBrowseBtn.setDisable(true);
            outputSelectBrowseBtn.setDisable(true);
        }

    }

}
