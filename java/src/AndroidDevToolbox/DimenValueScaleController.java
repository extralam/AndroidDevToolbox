package AndroidDevToolbox;

import com.google.gson.annotations.SerializedName;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;
import com.stanfy.gsonxml.XmlParserCreator;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import utils.AppConfig;
import utils.FileHelper;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DimenValueScaleController extends BaseController implements Initializable {

    private static final String LINE_BREAK = "\n";
    private static final String TAB = "    ";
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    private String[] dimenUnits = {
        "dp", "dip", "sp", "pt", "px", "mm", "in"
    };

    public Button fileSelectBrowseBtn;
    public TextField singleFileTextField;
    public Button outputSelectBrowseBtn;
    public TextField outputFolderTextField;
    public Button dimenScaleBtn;
    public TextField scaleFactorTextField;

    private ResourceBundle resourceBundle;
    private DimenScaleService dimenScaleService;

    private String outputFolderPath;
    private String inputFilePath;
    private boolean inputFileSelected;
    private boolean outputFolderSelected;

    public static final XmlParserCreator PARSER_CREATOR = new XmlParserCreator() {
        @Override
        public XmlPullParser createParser() {
            try {
                final XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                return parser;
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    };

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        dimenScaleService = new DimenScaleService();
    }

    public void browseForDimenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(resourceBundle.getString("ResizeSelectImageFile"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("xml", "*.xml")
        );

        // open file dialog
        File file = fileChooser.showOpenDialog(fileSelectBrowseBtn.getScene().getWindow());

        if (file != null) {
            singleFileTextField.setText(file.getPath());
            inputFilePath = file.getPath();
            inputFileSelected = true;
            updateStartBtnState();
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

    public void startDimenScale() {
        dimenScaleService.restart();
    }

    private void updateStartBtnState() {
        if (inputFileSelected && outputFolderSelected) {
            dimenScaleBtn.setDisable(false);
        } else {
            if (dimenScaleBtn != null) {
                dimenScaleBtn.setDisable(true);
            }
        }
    }

    private class DimenResources {

        public List<Dimen> dimen;

    }

    private class Dimen {

        @SerializedName("@name")
        public String name;

        @SerializedName("$")
        public String value;

    }

    private class DimenScaleService extends Service<Void> {

        private String getDimenUnit(String dimenValue) {
            String unit = "";
            for (int i=0; i<dimenUnits.length; i++) {
                if (dimenValue.endsWith(dimenUnits[i])) {
                    unit = dimenUnits[i];
                    break;
                }
            }
            return unit;
        }

        @Override
        protected Task<Void> createTask() {
            Task task =  new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    updateMessage(resourceBundle.getString("DimenScaling"));

                    double scaleFactor = 1.0;
                    try {
                        scaleFactor = Double.parseDouble(scaleFactorTextField.getText());
                    } catch (Exception e) {
                        e.printStackTrace();
                        updateMessage(resourceBundle.getString("DimenScaleInvalidFactor"));
                        return null;
                    }

                    try {
                        GsonXml gsonXml = new GsonXmlBuilder()
                                .setXmlParserCreator(PARSER_CREATOR)
                                .setSameNameLists(true).create();

                        String xml = FileHelper.readFileAsString(inputFilePath);
                        xml = xml.replace(XML_HEADER, "");
                        DimenResources model = gsonXml.fromXml(xml, DimenResources.class);

                        StringBuilder outXmlBuilder = new StringBuilder("<resources>")
                                .append(LINE_BREAK);

                        String scaledValue = "";
                        String dimenUnit = "";
                        double originalValue = 0;
                        for (Dimen dimen : model.dimen) {
                            scaledValue = dimen.value;

                            dimenUnit = getDimenUnit(dimen.value);

                            if (dimenUnit != null && dimenUnit.length() > 0) {
                                originalValue = Double.parseDouble(dimen.value.replace(dimenUnit, ""));
                                scaledValue = (int)(Math.round(originalValue * scaleFactor)) + dimenUnit;
                            }

                            // construct the value scaled xml
                            outXmlBuilder.append(TAB)
                                    .append("<dimen name=\"")
                                    .append(dimen.name)
                                    .append("\">")
                                    .append(scaledValue)
                                    .append("</dimen>")
                                    .append(LINE_BREAK);

                        }
                        outXmlBuilder.append("</resources>");

                        // create output folders
                        String outputFolder = outputFolderPath + File.separator +
                                AppConfig.FOLDER_DIMEN_SCALE_PREFIX + "_" + System.currentTimeMillis();
                        FileHelper.makeFolder(outputFolder);

                        FileHelper.saveToFile(outXmlBuilder.toString(), outputFolder
                                + File.separator + "dimens.xml");

                    } catch (Exception e) {
                        e.printStackTrace();
                        updateMessage(resourceBundle.getString("DimenScaleInvalidFile"));
                        return null;
                    }

                    updateMessage(resourceBundle.getString("DimenScaleCompleted"));
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
            fileSelectBrowseBtn.setDisable(false);
            outputSelectBrowseBtn.setDisable(false);
            dimenScaleBtn.setDisable(false);
            scaleFactorTextField.setDisable(false);
        }

        @Override
        protected void running() {
            super.running();
            if (getBackgroundTaskExecutionListener() != null) {
                getBackgroundTaskExecutionListener().taskStart();
            }
            fileSelectBrowseBtn.setDisable(true);
            outputSelectBrowseBtn.setDisable(true);
            dimenScaleBtn.setDisable(true);
            scaleFactorTextField.setDisable(true);
        }

    }

}
