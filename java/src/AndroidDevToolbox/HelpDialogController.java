package AndroidDevToolbox;

import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class HelpDialogController implements Initializable {

    public Label helpDialogMsgLabel;
    public Button helpDialogOkBtn;

    private ResourceBundle resourceBundle;
    private String msg;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public void setDialogMessage(String msg) {
        this.msg = msg;
        helpDialogMsgLabel.setText(msg);
    }

    public void dismissHelpDialog() {
        ((Stage)helpDialogOkBtn.getScene().getWindow()).close();
    }

}
