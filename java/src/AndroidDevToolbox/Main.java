package AndroidDevToolbox;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import utils.AppConfig;
import utils.FileHelper;
import utils.LocaleHelper;

import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "bundles.Bundle", LocaleHelper.getLocale());

        Parent root = fxmlLoader.load(
                FileHelper.getFxmlUrl(getClass(), "MainScreen.fxml"), resourceBundle);
        Scene scene = new Scene(root);
        String cssURL = getClass().getClassLoader().getResource(AppConfig.APP_CSS_PATH).toExternalForm();
        scene.getStylesheets().add(cssURL);

        String appURL = getClass().getClassLoader().getResource(AppConfig.APP_ICON_PATH).toExternalForm();
        primaryStage.setTitle(resourceBundle.getString("AppName"));
        primaryStage.getIcons().add(new Image(appURL));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setMinWidth(850);
        primaryStage.setMinHeight(750);
        primaryStage.show();
    }

    public static void main(String[] args) {
        System.setProperty("javafx.macosx.embedded", "true");
        java.awt.Toolkit.getDefaultToolkit();
        launch(args);
    }
}
