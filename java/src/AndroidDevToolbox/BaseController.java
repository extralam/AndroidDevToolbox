package AndroidDevToolbox;

import AndroidDevToolbox.MainController.BackgroundTaskExecutionListener;
import javafx.scene.control.Label;

public abstract class BaseController {

    private Label statusBarLabel;
    private BackgroundTaskExecutionListener backgroundTaskExecutionListener;

    public void setStatusBarLabel(Label label) {
        statusBarLabel = label;
    }

    protected Label getStatusBarLabel() {
        return statusBarLabel;
    }

    public void setBackgroundTaskExecutionListener(BackgroundTaskExecutionListener listener) {
        backgroundTaskExecutionListener = listener;
    }

    protected BackgroundTaskExecutionListener getBackgroundTaskExecutionListener() {
        return backgroundTaskExecutionListener;
    }

}
