package AndroidDevToolbox;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import utils.AppConfig;
import utils.FileHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ImageResizeController extends BaseController implements Initializable {

    public Tab singleResizeTab;
    public Tab batchResizeTab;
    public Button fileSelectBrowseBtn;
    public TextField singleFileTextField;
    public GridPane imageFileInfoPane;
    public GridPane selectImageNoticePane;
    public ImageView fileInfoImageView;
    public Label imageWidthLabel;
    public Label imageHeightLabel;
    public Label fileSizeLabel;
    public ToggleButton densityBtnMdpi;
    public ToggleButton densityBtnHdpi;
    public ToggleButton densityBtnXHdpi;
    public ToggleButton densityBtnXXHdpi;
    public Button outputSelectBrowseBtn;
    public TextField outputFolderTextField;
    public Button imageResizeBtn;
    public Button inputFolderSelectBrowseBtn;
    public TextField batchFileTextField;
    public GridPane folderInfoPane;
    public GridPane selectFolderNoticePane;
    public Label folderNumOfImageLabel;

    private final String FIXED_SPERATOR = "#!#";
    
    private enum RESIZE_MODE {
        SINGLE,
        BATCH
    }

    private enum DENSITY {
        M_DPI,
        H_DPI,
        XH_DPI,
        XXH_DPI
    }

    // mdpi hdpi xhdpi xxhdpi
    private static final float[] DENSITY_FACTORS = {
        1.0f, 1.5f, 2.0f, 3.0f
    } ;

    private DENSITY density;
    private RESIZE_MODE resizeMode;
    private ResourceBundle resourceBundle;
    private ImageResizeService imageResizeService;

    private String outputFolderPath;
    private String inputFolderPath;
    private String imageFilePath;
    private boolean imageFileSelected;
    private boolean outputFolderSelected;
    private boolean inputFolderSelected;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        resizeMode = RESIZE_MODE.SINGLE;
        density = DENSITY.M_DPI;
//        fileInfoImageView.fitWidthProperty().bind(imageFileInfoPane.widthProperty());
//        fileInfoImageView.fitHeightProperty().bind(imageFileInfoPane.heightProperty());
        updateFileInfoContent(null);
        updateFolderInfoContent(null);
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

    public void setSourceDensity(MouseEvent event) {
        if (event.getSource() == densityBtnMdpi) {
            densityBtnMdpi.setSelected(true);
            density = DENSITY.M_DPI;
        } else if (event.getSource() == densityBtnHdpi) {
            densityBtnHdpi.setSelected(true);
            density = DENSITY.H_DPI;
        } else if (event.getSource() == densityBtnXHdpi) {
            densityBtnXHdpi.setSelected(true);
            density = DENSITY.XH_DPI;
        } else if (event.getSource() == densityBtnXXHdpi) {
            densityBtnXXHdpi.setSelected(true);
            density = DENSITY.XXH_DPI;
        }
    }

    public void browseForInputFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(resourceBundle.getString("ResizeSelectInputFolder"));
        File inputFolder = directoryChooser.showDialog(inputFolderSelectBrowseBtn.getScene().getWindow());

        if (inputFolder != null) {
            batchFileTextField.setText(inputFolder.getPath());
            inputFolderSelected = true;
            inputFolderPath = inputFolder.getPath();
            updateStartBtnState();
            updateFolderInfoContent(inputFolder);
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

    public void tabOnSelectionChanged() {
        if (singleResizeTab.isSelected()) {
            resizeMode = RESIZE_MODE.SINGLE;
        } else {
            resizeMode = RESIZE_MODE.BATCH;
        }
        updateStartBtnState();
    }

    private void updateFolderInfoContent(File file) {
        if (file == null) {
            if (folderInfoPane != null && selectFolderNoticePane != null) {
                folderInfoPane.setVisible(false);
                selectFolderNoticePane.setVisible(true);
            }
            return;
        }

        folderInfoPane.setVisible(true);
        selectFolderNoticePane.setVisible(false);

        int numOfImageFiles = 0;
        File[] files = file.listFiles();
        for (File folderFile : files) {
            if (isImageFileExtension(folderFile.getName())) {
                numOfImageFiles++;
            }
        }

        folderNumOfImageLabel.setText(
                resourceBundle.getString("ResizeFolderImageCount") + " " + numOfImageFiles);
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
        if (resizeMode == RESIZE_MODE.SINGLE) {
            if (imageFileSelected && outputFolderSelected) {
                imageResizeBtn.setDisable(false);
            } else {
                if (imageResizeBtn != null) {
                    imageResizeBtn.setDisable(true);
                }
            }
        } else if (resizeMode == RESIZE_MODE.BATCH) {
            if (inputFolderSelected && outputFolderSelected) {
                imageResizeBtn.setDisable(false);
            } else {
                if (imageResizeBtn != null) {
                    imageResizeBtn.setDisable(true);
                }
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
                            AppConfig.FOLDER_RESIZE_PREFIX + "_" + System.currentTimeMillis();
                    FileHelper.makeFolder(rootFolder);

                    String mdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_MDPI;
                    String hdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_HDPI;
                    String xhdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_XHDPI;
                    String xxhdpiPath = rootFolder + File.separator + AppConfig.FOLDER_DRAWABLE_XXHDPI;

                    FileHelper.makeFolder(mdpiPath);
                    FileHelper.makeFolder(hdpiPath);
                    FileHelper.makeFolder(xhdpiPath);
                    FileHelper.makeFolder(xxhdpiPath);

                    ArrayList<String> targetFolders = new ArrayList<String>();
                    targetFolders.add(mdpiPath + FIXED_SPERATOR + DENSITY_FACTORS[DENSITY.M_DPI.ordinal()]);
                    targetFolders.add(hdpiPath + FIXED_SPERATOR + DENSITY_FACTORS[DENSITY.H_DPI.ordinal()]);
                    targetFolders.add(xhdpiPath + FIXED_SPERATOR + DENSITY_FACTORS[DENSITY.XH_DPI.ordinal()]);
                    targetFolders.add(xxhdpiPath + FIXED_SPERATOR + DENSITY_FACTORS[DENSITY.XXH_DPI.ordinal()]);

                    float sourceDensityFactor = DENSITY_FACTORS[density.ordinal()];

                    String sourceDensityFolder = "";
                    if (density == DENSITY.M_DPI) {
                        sourceDensityFolder = mdpiPath;
                    } else if (density == DENSITY.H_DPI) {
                        sourceDensityFolder = hdpiPath;
                    } else if (density == DENSITY.XH_DPI) {
                        sourceDensityFolder = xhdpiPath;
                    } else if (density == DENSITY.XXH_DPI) {
                        sourceDensityFolder = xxhdpiPath;
                    }

                    targetFolders.remove(sourceDensityFolder + FIXED_SPERATOR + sourceDensityFactor);

                    // copy original image to its density folder
                    if (resizeMode == RESIZE_MODE.SINGLE) {
                        System.out.println(imageFilePath);
                        String fileName = new File(imageFilePath).getName();

                        FileHelper.copyFile(imageFilePath
                                , sourceDensityFolder + File.separator
                                + fileName.replace(" ", "_").toLowerCase());
                    } else if (resizeMode == RESIZE_MODE.BATCH) {
                        File[] Files = new File(inputFolderPath).listFiles();
                        for (File folderFile : Files) {
                            if (isImageFileExtension(folderFile.getName())) {
                                FileHelper.copyFile(folderFile.getPath()
                                        , sourceDensityFolder + File.separator
                                        + folderFile.getName().replace(" ", "_").toLowerCase());
                            }
                        }
                    }

                    // resize into different densities
                    File[] sourceDirFiles = new File(sourceDensityFolder).listFiles();
                    int numOfFiles = sourceDirFiles.length;
                    int count = 1;
                    for (File file : sourceDirFiles) {
                        String fileName = file.getName();
                        String fileExt = FilenameUtils.getExtension(fileName);
                        String sourceFilePath = sourceDensityFolder + File.separator + fileName;

                        updateMessage(
                                resourceBundle.getString("ResizeProgress") + " " + fileName
                                + " (" + count + " / " + numOfFiles + ")");

                        BufferedImage bufferedImage = null;
                        try {
                            bufferedImage = ImageIO.read(new File(sourceFilePath));
                        } catch (Exception e) {
                            e.printStackTrace();
                            continue;
                        }

                        if (bufferedImage == null) {
                            continue;
                        }

                        float sourceWidth = bufferedImage.getWidth();
                        float sourceHeight = bufferedImage.getHeight();

                        for (String targetFolderAndDensity : targetFolders) {
                            String[] targetFolderDensity = targetFolderAndDensity.split(FIXED_SPERATOR);
                            String folder = targetFolderDensity[0];
                            String targetDensityFactor = targetFolderDensity[1];

                            int targetWidth = (int) Math.ceil(sourceWidth / sourceDensityFactor
                                    * Float.parseFloat(targetDensityFactor));
                            int targetHeight = (int) Math.ceil(sourceHeight / sourceDensityFactor
                                    * Float.parseFloat(targetDensityFactor));

                            BufferedImage resizedImage = Scalr.resize(bufferedImage
                                    , Scalr.Method.QUALITY
                                    , targetWidth
                                    , targetHeight);

                            ImageIO.write(resizedImage, fileExt
                                    , new File(folder + File.separator
                                    + fileName.replace(" ", "_").toLowerCase()));
                        }
                        count++;
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
            inputFolderSelectBrowseBtn.setDisable(false);
            fileSelectBrowseBtn.setDisable(false);
            outputSelectBrowseBtn.setDisable(false);
            densityBtnMdpi.setDisable(false);
            densityBtnHdpi.setDisable(false);
            densityBtnXHdpi.setDisable(false);
            densityBtnXXHdpi.setDisable(false);
            singleResizeTab.setDisable(false);
            batchResizeTab.setDisable(false);
        }

        @Override
        protected void running() {
            super.running();
            if (getBackgroundTaskExecutionListener() != null) {
                getBackgroundTaskExecutionListener().taskStart();
            }
            imageResizeBtn.setDisable(true);
            inputFolderSelectBrowseBtn.setDisable(true);
            fileSelectBrowseBtn.setDisable(true);
            outputSelectBrowseBtn.setDisable(true);
            densityBtnMdpi.setDisable(true);
            densityBtnHdpi.setDisable(true);
            densityBtnXHdpi.setDisable(true);
            densityBtnXXHdpi.setDisable(true);
            singleResizeTab.setDisable(true);
            batchResizeTab.setDisable(true);
        }
    }

}
