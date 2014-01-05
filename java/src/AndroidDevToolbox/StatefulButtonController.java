package AndroidDevToolbox;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import utils.AppConfig;
import utils.FileHelper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by AtomInvention on 14/12/13.
 */
public class StatefulButtonController extends BaseController implements Initializable {

    public interface OnStyleSetListener {
        public void onSet(int brightness, int transparency, State state);
    }

    private static final String XML_PRESSED_HOLDER = "$pressed$";
    private static final String XML_FOCUSED_HOLDER = "$focused$";
    private static final String XML_DISABLED_HOLDER = "$disabled$";
    private static final String XML_NORMAL_HOLDER = "$normal$";

    private ResourceBundle resourceBundle;
    private OnStyleSetListener onStyleSetListener;
    private GenerateStatefulFilesService generateStatefulFilesService;

    public TextField inputImageTextField;
    public Button inputSelectBrowseBtn;
    public TextField normalFilenameTextField;
    public TextField pressedFilenameTextField;
    public TextField focusedFilenameTextField;
    public TextField disabledFilenameTextField;
    public CheckBox pressedCheckBox;
    public CheckBox focusedCheckBox;
    public CheckBox disabledCheckBox;
    public Button pressedStyleBtn;
    public Button focusedStyleBtn;
    public Button disabledStyleBtn;
    public ImageView normalPreviewImageView;
    public ImageView pressedPreviewImageView;
    public ImageView focusedPreviewImageView;
    public ImageView disabledPreviewImageView;
    public TextField outputFolderTextField;
    public Button outputSelectBrowseBtn;
    public Button generateFilesbtn;
    public Label normalFilenameLabel;
    public Label pressedFilenameLabel;
    public Label focusedFilenameLabel;
    public Label disabledFilenameLabel;
    public Label normalExtLabel;
    public Label pressedExtLabel;
    public Label focusedExtLabel;
    public Label disabledExtLabel;
    public HBox normalHbox;
    public HBox pressedHbox;
    public HBox focusedHbox;
    public HBox disabledHbox;

    public enum State {
        NORMAL,
        PRESSED,
        FOCUSED,
        DISABLED
    }

    private interface DefaultStateFileName {
        String NORMAL = "normal";
        String PRESSED = "pressed";
        String FOCUSED = "focused";
        String DISABLED = "disabled";
    }

    private interface PressedDefaultImgValue {
        float BRIGHTNESS = -30.0f;
        float TRANSPARENCY = 0.0f;
    }

    private interface FocusedDefaultImgValue {
        float BRIGHTNESS = -15.0f;
        float TRANSPARENCY = 0.0f;
    }

    private interface DisabledDefaultImgValue {
        float BRIGHTNESS = 25.0f;
        float TRANSPARENCY = 5.0f;
    }

    private String imageFilePath;
    private String outputFolderPath;
    private Label[] fileNameLabels;
    private Label[] fileExtLabels;
    private String[] defaultStateFileNames;
    private TextField[] fileNameTextFields;
    private ImageStyle pressedImgStyle;
    private ImageStyle focusedImgStyle;
    private ImageStyle disabledImgStyle;

    private boolean imageFileSelected;
    private boolean outputFolderSelected;
    private boolean pressedStateEnabled;
    private boolean focusedStateEnabled;
    private boolean disabledStateEnabled;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        fileNameLabels = new Label[4];
        fileNameLabels[0] = normalFilenameLabel;
        fileNameLabels[1] = pressedFilenameLabel;
        fileNameLabels[2] = focusedFilenameLabel;
        fileNameLabels[3] = disabledFilenameLabel;

        fileExtLabels = new Label[4];
        fileExtLabels[0] = normalExtLabel;
        fileExtLabels[1] = pressedExtLabel;
        fileExtLabels[2] = focusedExtLabel;
        fileExtLabels[3] = disabledExtLabel;

        defaultStateFileNames = new String[4];
        defaultStateFileNames[0] = DefaultStateFileName.NORMAL;
        defaultStateFileNames[1] = DefaultStateFileName.PRESSED;
        defaultStateFileNames[2] = DefaultStateFileName.FOCUSED;
        defaultStateFileNames[3] = DefaultStateFileName.DISABLED;

        fileNameTextFields = new TextField[4];
        fileNameTextFields[0] = normalFilenameTextField;
        fileNameTextFields[1] = pressedFilenameTextField;
        fileNameTextFields[2] = focusedFilenameTextField;
        fileNameTextFields[3] = disabledFilenameTextField;

        pressedImgStyle = new ImageStyle(PressedDefaultImgValue.BRIGHTNESS
                , PressedDefaultImgValue.TRANSPARENCY);
        focusedImgStyle = new ImageStyle(FocusedDefaultImgValue.BRIGHTNESS
                , FocusedDefaultImgValue.TRANSPARENCY);
        disabledImgStyle = new ImageStyle(DisabledDefaultImgValue.BRIGHTNESS
                , DisabledDefaultImgValue.TRANSPARENCY);

        onStyleSetListener = new OnStyleSetListener() {
            @Override
            public void onSet(int brightness, int transparency, State state) {
                System.out.println("onSet brightness: " + brightness
                        + " transparency: " + transparency + " state: " + state);
                switch (state) {
                    case PRESSED:
                        pressedImgStyle.setBrightness(brightness);
                        pressedImgStyle.setTransparency(transparency);
                        break;
                    case DISABLED:
                        disabledImgStyle.setBrightness(brightness);
                        disabledImgStyle.setTransparency(transparency);
                        break;
                    case FOCUSED:
                        focusedImgStyle.setBrightness(brightness);
                        focusedImgStyle.setTransparency(transparency);
                        break;
                }
                updatePreviewImage(state);
            }
        };

        generateStatefulFilesService = new GenerateStatefulFilesService();
    }

    private void updatePreviewImage(State state) {
        switch (state) {
            case PRESSED:
                if (pressedStateEnabled) {
                    setImageAndStyle(pressedImgStyle.getBrightness()
                            , pressedImgStyle.getTransparency(), pressedPreviewImageView);
                } else {
                    pressedPreviewImageView.setImage(null);
                }
                break;
            case DISABLED:
                if (disabledStateEnabled) {
                    setImageAndStyle(disabledImgStyle.getBrightness()
                            , disabledImgStyle.getTransparency(), disabledPreviewImageView);
                } else {
                    disabledPreviewImageView.setImage(null);
                }
                break;
            case FOCUSED:
                if (focusedStateEnabled) {
                    setImageAndStyle(focusedImgStyle.getBrightness()
                            , focusedImgStyle.getTransparency(), focusedPreviewImageView);
                } else {
                    focusedPreviewImageView.setImage(null);
                }
                break;
        }
    }

    private void setImageAndStyle(float brightness, float transparency, ImageView imageView) {
        imageView.setImage(SwingFXUtils.toFXImage(getStyledImage(brightness, transparency, 200), null));
    }

    private BufferedImage getStyledImage(float brightness, float transparency, int size) {
        BufferedImage outputImage = null;
        try {
            BufferedImage readBufferedImage = ImageIO.read(new File(imageFilePath));
            BufferedImage inputBufferedImage = convertToArgb(readBufferedImage);
            outputImage = inputBufferedImage;
            if (size > 0) {
                outputImage = Scalr.resize(inputBufferedImage
                        , Method.BALANCED
                        , size
                        , size);
            }

            float brightnessFactor = 1.0f + brightness/100.0f;
            float transparencyFactor = Math.abs(transparency/100.0f - 1.0f);
            RescaleOp rescale = new RescaleOp(
                    new float[]{brightnessFactor, brightnessFactor, brightnessFactor, transparencyFactor},
                    new float[]{0f, 0f, 0f, 0f}, null);
            rescale.filter(outputImage, outputImage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputImage;
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
        File file = fileChooser.showOpenDialog(inputSelectBrowseBtn.getScene().getWindow());

        if (file != null) {
            inputImageTextField.setText(file.getPath());
            imageFilePath = file.getPath();
            imageFileSelected = true;
            updateStartBtnState();
            updateStateSettingsBox();
            updateNormalImageView(new File(imageFilePath));
            updatePreviewImage(State.PRESSED);
            updatePreviewImage(State.FOCUSED);
            updatePreviewImage(State.DISABLED);
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

    public void updateNormalImageView(File file) {
        Image image = new Image("file:" + file.getPath());
        normalPreviewImageView.setImage(image);
    }

    public void updateStateSettingsBox() {
        normalHbox.setVisible(true);
        pressedHbox.setVisible(true);
        focusedHbox.setVisible(true);
        disabledHbox.setVisible(true);

        normalHbox.setDisable(false);

        pressedCheckBox.setDisable(false);
        focusedCheckBox.setDisable(false);
        disabledCheckBox.setDisable(false);

        String fileName = new File(imageFilePath).getName();
        // force png output
        String fileExt = "png";
        String originalExt = FilenameUtils.getExtension(fileName);
        fileName = fileName.replace("."+originalExt, "").replace(" ", "_").toLowerCase();

        for (int i=0; i<fileNameLabels.length; i++) {
            fileNameLabels[i].setText(fileName + "_");
            fileExtLabels[i].setText("." + fileExt);
            fileNameTextFields[i].setText(defaultStateFileNames[i]);
        }
    }

    public void onPressedStateChecked() {
        pressedStateEnabled = !pressedStateEnabled;
        pressedStyleBtn.setDisable(!pressedStateEnabled);
        pressedHbox.setDisable(!pressedStateEnabled);
        updatePreviewImage(State.PRESSED);
        updateStartBtnState();
    }

    public void onFocusedStateChecked() {
        focusedStateEnabled = !focusedStateEnabled;
        focusedStyleBtn.setDisable(!focusedStateEnabled);
        focusedHbox.setDisable(!focusedStateEnabled);
        updatePreviewImage(State.FOCUSED);
        updateStartBtnState();
    }

    public void onDisabledStateChecked() {
        disabledStateEnabled = !disabledStateEnabled;
        disabledStyleBtn.setDisable(!disabledStateEnabled);
        disabledHbox.setDisable(!disabledStateEnabled);
        updatePreviewImage(State.DISABLED);
        updateStartBtnState();
    }

    public void setStyle(MouseEvent event) {
        float defaultBrightness = 0.0f;
        float defaultTransparency = 0.0f;
        float currentBrightness = 0.0f;
        float currentTransparency = 0.0f;

        State state = State.PRESSED;

        Button button = (Button) event.getSource();
        if (button == pressedStyleBtn) {
            defaultBrightness = PressedDefaultImgValue.BRIGHTNESS;
            defaultTransparency = PressedDefaultImgValue.TRANSPARENCY;
            currentBrightness = pressedImgStyle.getBrightness();
            currentTransparency = pressedImgStyle.getTransparency();
            state = State.PRESSED;
        } else if (button == focusedStyleBtn) {
            defaultBrightness = FocusedDefaultImgValue.BRIGHTNESS;
            defaultTransparency = FocusedDefaultImgValue.TRANSPARENCY;
            currentBrightness = focusedImgStyle.getBrightness();
            currentTransparency = focusedImgStyle.getTransparency();
            state = State.FOCUSED;
        } else if (button == disabledStyleBtn) {
            defaultBrightness = DisabledDefaultImgValue.BRIGHTNESS;
            defaultTransparency = DisabledDefaultImgValue.TRANSPARENCY;
            currentBrightness = disabledImgStyle.getBrightness();
            currentTransparency = disabledImgStyle.getTransparency();
            state = State.DISABLED;
        }

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FileHelper.getFxmlUrl(
                    getClass(), "EditStyleDialog.fxml"), resourceBundle);

            Parent root = (Parent) fxmlLoader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            String cssURL = getClass().getClassLoader().getResource(AppConfig.APP_CSS_PATH).toExternalForm();
            scene.getStylesheets().add(cssURL);
            dialogStage.setScene(scene);

            EditStyleDialogController controller = fxmlLoader.getController();

            BufferedImage readBufferedImage = ImageIO.read(new File(imageFilePath));
            BufferedImage inputBufferedImage = convertToArgb(readBufferedImage);
            BufferedImage resizedImage = Scalr.resize(inputBufferedImage
                    , Method.BALANCED
                    , 200
                    , 200);

            controller.initImage(state, resizedImage, defaultBrightness, defaultTransparency
                    , currentBrightness, currentTransparency);
            controller.setOnStyleSetListener(onStyleSetListener);

            dialogStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStartBtnState() {
        boolean anyExtraStateSelected = pressedStateEnabled || focusedStateEnabled
                || disabledStateEnabled;
        if (imageFileSelected && outputFolderSelected && anyExtraStateSelected) {
            generateFilesbtn.setDisable(false);
        } else {
            if (generateFilesbtn != null) {
                generateFilesbtn.setDisable(true);
            }
        }
    }

    public static class ImageStyle {

        private float brightness;
        private float transparency;

        public ImageStyle(float brightness, float transparency) {
            this.brightness = brightness;
            this.transparency = transparency;
        }

        public float getBrightness() {
            return brightness;
        }

        public void setBrightness(float brightness) {
            this.brightness = brightness;
        }

        public float getTransparency() {
            return transparency;
        }

        public void setTransparency(float transparency) {
            this.transparency = transparency;
        }

    }

    public void startGenerateFiles() {
        generateStatefulFilesService.restart();
    }

    private BufferedImage convertToArgb(java.awt.Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null)
                , BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = bufferedImage.createGraphics();
        graphics2D.drawImage(image, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()
                , new Color(0, 0, 0, 0), null);
        return bufferedImage;
    }

    private class GenerateStatefulFilesService extends Service<Void> {

        @Override
        protected Task<Void> createTask() {
            Task task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage(resourceBundle.getString("StateGenerationStarted"));

                    // create folder
                    String rootFolder = outputFolderPath + File.separator +
                            AppConfig.FOLDER_STATEFUL_PREFIX + "_" + System.currentTimeMillis();
                    FileHelper.makeFolder(rootFolder);

                    String fileName = new File(imageFilePath).getName().replace(" ", "_").toLowerCase();
                    // force png output
                    String fileExt = "png";
                    String originalExt = FilenameUtils.getExtension(fileName);
                    String dotFileExt = "." + fileExt;
                    String fileNameNoExt = fileName.replace("."+originalExt, "");

                    String normalFileName = fileNameNoExt
                            + "_" + normalFilenameTextField.getText() + dotFileExt;
                    String pressedFileName = fileNameNoExt
                            + "_" + pressedFilenameTextField.getText() + dotFileExt;
                    String focusedFileName = fileNameNoExt
                            + "_" + focusedFilenameTextField.getText() + dotFileExt;
                    String disabledFileName = fileNameNoExt
                            + "_" + disabledFilenameTextField.getText() + dotFileExt;

                    String normalFilePath = rootFolder + File.separator + normalFileName;
                    String pressedFilePath = rootFolder + File.separator + pressedFileName;
                    String focusedFilePath = rootFolder + File.separator + focusedFileName;
                    String disabledFilePath = rootFolder + File.separator + disabledFileName;

                    // copy original(normal) file first
                    FileHelper.copyFile(imageFilePath, normalFilePath);

                    // output images of other states
                    BufferedImage outputBufferedImage = null;
                    if (pressedStateEnabled) {
                        outputBufferedImage = getStyledImage(
                                pressedImgStyle.getBrightness(), pressedImgStyle.getTransparency(), 0);
                        ImageIO.write(outputBufferedImage, fileExt, new File(pressedFilePath));
                    }

                    if (focusedStateEnabled) {
                        outputBufferedImage = getStyledImage(
                                focusedImgStyle.getBrightness(), focusedImgStyle.getTransparency(), 0);
                        ImageIO.write(outputBufferedImage, fileExt, new File(focusedFilePath));
                    }

                    if (disabledStateEnabled) {
                        outputBufferedImage = getStyledImage(
                                disabledImgStyle.getBrightness(), disabledImgStyle.getTransparency(), 0);
                        ImageIO.write(outputBufferedImage, fileExt, new File(disabledFilePath));
                    }

                    // output xml
                    String xmlPath = getClass().getClassLoader()
                            .getResource(AppConfig.STATEFUL_TEMP_PATH).getPath();
                    String fileContent = FileHelper.readFileAsString(xmlPath);

                    normalFileName = normalFileName.replace("."+fileExt, "");
                    pressedFileName = pressedFileName.replace("."+fileExt, "");
                    focusedFileName = focusedFileName.replace("."+fileExt, "");
                    disabledFileName = disabledFileName.replace("."+fileExt, "");
                    fileContent = fileContent
                            .replace(XML_PRESSED_HOLDER
                                    , pressedStateEnabled?pressedFileName:normalFileName)
                            .replace(XML_FOCUSED_HOLDER
                                    , focusedStateEnabled?focusedFileName:normalFileName)
                            .replace(XML_DISABLED_HOLDER
                                    , disabledStateEnabled?disabledFileName:normalFileName)
                            .replace(XML_NORMAL_HOLDER, normalFileName);
                    FileHelper.saveToFile(fileContent
                            , rootFolder + File.separator + fileNameNoExt + ".xml");


                    updateMessage(resourceBundle.getString("StateGenerationCompleted"));
                    return null;
                }
            };
            getStatusBarLabel().textProperty().bind(task.messageProperty());
            return task;
        }

        @Override
        protected void running() {
            super.running();
            if (getBackgroundTaskExecutionListener() != null) {
                getBackgroundTaskExecutionListener().taskStart();
            }
            inputSelectBrowseBtn.setDisable(true);
            normalFilenameTextField.setDisable(true);
            pressedFilenameTextField.setDisable(true);
            focusedFilenameTextField.setDisable(true);
            disabledFilenameTextField.setDisable(true);
            pressedCheckBox.setDisable(true);
            focusedCheckBox.setDisable(true);
            disabledCheckBox.setDisable(true);
            pressedStyleBtn.setDisable(true);
            focusedStyleBtn.setDisable(true);
            disabledStyleBtn.setDisable(true);
            outputSelectBrowseBtn.setDisable(true);
            generateFilesbtn.setDisable(true);
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            getStatusBarLabel().textProperty().unbind();
            if (getBackgroundTaskExecutionListener() != null) {
                getBackgroundTaskExecutionListener().taskComplete();
            }
            inputSelectBrowseBtn.setDisable(false);
            normalFilenameTextField.setDisable(false);
            pressedFilenameTextField.setDisable(false);
            focusedFilenameTextField.setDisable(false);
            disabledFilenameTextField.setDisable(false);
            pressedCheckBox.setDisable(false);
            focusedCheckBox.setDisable(false);
            disabledCheckBox.setDisable(false);
            pressedStyleBtn.setDisable(false);
            focusedStyleBtn.setDisable(false);
            disabledStyleBtn.setDisable(false);
            outputSelectBrowseBtn.setDisable(false);
            generateFilesbtn.setDisable(false);
        }
    }

}
