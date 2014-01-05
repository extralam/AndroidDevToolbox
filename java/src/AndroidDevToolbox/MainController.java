package AndroidDevToolbox;

import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AppConfig;
import utils.FileHelper;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    public interface BackgroundTaskExecutionListener {
        public void taskStart();
        public void taskComplete();
    }

    private static String[] SCREEN_TITLE_KEY = {
            "LeftMenuAppIconResize",
            "LeftMenuResize",
            "LeftMenuStatefulBtn",
            "LeftMenuDimenScale"
    };

    private static String[] SCREEN_HELP_KEY = {
            "helpAppIconResize",
            "helpResize",
            "helpStatefulBtn",
            "helpDimenScale"
    };

    private interface FxmlName {
        String APP_ICON_RESIZE_SCREEN = "AppIconResizeScreen.fxml";
        String RESIZE_SCREEN = "ImageResizeScreen.fxml";
        String STATEFUL_BTN_SCREEN = "StatefulButtonScreen.fxml";
        String DIMEN_SCALE_SCREEN = "DimenValueScaleScreen.fxml";
    }

    public ToggleButton leftMenuImageResizeBtn;
    public ToggleButton leftMenuStatefulBtn;
    public ToggleButton leftMenuIconResizeBtn;
    public ToggleButton leftMenuDimenScaleBtn;
    public GridPane contentPane;
    public Label titleBarLabel;
    public Label statusBarLabel;
    public VBox contentHeaderBox;
    public VBox leftMenuBox;

    private ResourceBundle resourceBundle;
    private Stage dialogStage;

    private BackgroundTaskExecutionListener taskExecutionListener = new BackgroundTaskExecutionListener() {
        @Override
        public void taskStart() {
            setLeftMenuEnable(false);
        }

        @Override
        public void taskComplete() {
            setLeftMenuEnable(true);
        }
    };

    private enum Screen {
        APP_ICON_RESIZE,
        IMAGE_RESIZE,
        STATEFUL_BUTTON,
        DIMEN_SCALE
    }

    private Screen currentScreen;
    private ToggleButton lastSelectedButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        contentHeaderBox.setVisible(false);
    }

    private void setScreen(Screen screen) {
        contentHeaderBox.setVisible(true);
        this.currentScreen = screen;
        titleBarLabel.setText(resourceBundle.getString(SCREEN_TITLE_KEY[screen.ordinal()]));
    }

    public void showHelpDialog() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(FileHelper.getFxmlUrl(
                    getClass(), "HelpDialog.fxml"), resourceBundle);

            Parent root = (Parent) fxmlLoader.load();

            dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root);
            String cssURL = getClass().getClassLoader().getResource(AppConfig.APP_CSS_PATH).toExternalForm();
            scene.getStylesheets().add(cssURL);
            dialogStage.setScene(scene);

            HelpDialogController controller = fxmlLoader.getController();
            controller.setDialogMessage(
                    resourceBundle.getString(SCREEN_HELP_KEY[currentScreen.ordinal()]));

            dialogStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateContentScreen(MouseEvent mouseEvent) {
        statusBarLabel.setText("");
        ToggleButton button = (ToggleButton) mouseEvent.getSource();
        Screen screen = getScreenForButton(button);

        selectButton(button);
        if (this.currentScreen == screen) {
            return;
        }

        setScreen(screen);
        this.contentPane.getChildren().clear();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    FileHelper.getFxmlUrl(getClass(), getFxmlNameForButton(button)), resourceBundle);
            this.contentPane.getChildren().add((Node)fxmlLoader.load());

            BaseController controller = fxmlLoader.getController();
            controller.setStatusBarLabel(statusBarLabel);
            controller.setBackgroundTaskExecutionListener(taskExecutionListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deselectLastButton() {
        if (lastSelectedButton != null) {
            lastSelectedButton.setSelected(false);
        }
    }

    private void selectButton(ToggleButton selectedButton) {
        deselectLastButton();
        lastSelectedButton = selectedButton;
        selectedButton.setSelected(true);
    }

    private Screen getScreenForButton(ToggleButton button) {
        Screen screen = Screen.IMAGE_RESIZE;

        if (button == leftMenuImageResizeBtn) {
            screen = Screen.IMAGE_RESIZE;
        } else if (button == leftMenuStatefulBtn) {
            screen = Screen.STATEFUL_BUTTON;
        } else if (button == leftMenuIconResizeBtn) {
            screen = Screen.APP_ICON_RESIZE;
        } else if (button == leftMenuDimenScaleBtn) {
            screen = Screen.DIMEN_SCALE;
        }

        return screen;
    }

    private String getFxmlNameForButton(ToggleButton button) {
        String fxmlName = "";

        if (button == leftMenuImageResizeBtn) {
            fxmlName = FxmlName.RESIZE_SCREEN;
        } else if (button == leftMenuStatefulBtn) {
            fxmlName = FxmlName.STATEFUL_BTN_SCREEN;
        } else if (button == leftMenuIconResizeBtn) {
            fxmlName = FxmlName.APP_ICON_RESIZE_SCREEN;
        } else if (button == leftMenuDimenScaleBtn) {
            fxmlName = FxmlName.DIMEN_SCALE_SCREEN;
        }

        return fxmlName;
    }

    private void setLeftMenuEnable(boolean enable) {
        ObservableList<Node> childList = leftMenuBox.getChildren();
        int numOfChild = childList.size();
        for (int i=0; i<numOfChild; i++) {
            Node btn = childList.get(i);
            btn.setDisable(!enable);
        }
    }

}
