package AndroidDevToolbox;

import AndroidDevToolbox.StatefulButtonController.OnStyleSetListener;
import AndroidDevToolbox.StatefulButtonController.State;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.net.URL;
import java.util.ResourceBundle;

public class EditStyleDialogController implements Initializable {

    public Button styleDialogOkBtn;
    public ImageView previewImageView;
    public Slider brightnessSlider;
    public Slider transparencySlider;
    public TextField brightnessTextField;
    public TextField transparencyTextField;

    private static final int SLIDER_MID_VAL = 100;

    private ResourceBundle resourceBundle;
    private OnStyleSetListener onStyleSetListener;
    private State state;
    private BufferedImage sourceBufferedImage;

    private float defaultBrightness;
    private float defaultTransparency;
    private float initBrightness;
    private float initTransparency;
    private boolean updateSkipped;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        brightnessSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                onBrightnessDragged();
                updateSkipped = !updateSkipped;
                if (updateSkipped || Math.abs(old_val.intValue() - new_val.intValue()) > 3) {
                    updateStyle();
                }
            }
        });

        transparencySlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
                onTransparencysDragged();
                updateSkipped = !updateSkipped;
                if (updateSkipped || Math.abs(old_val.intValue() - new_val.intValue()) > 3) {
                    updateStyle();
                }
            }
        });

    }

    public void initImage(State state, BufferedImage sourceBufferedImage, float defaultBrightness
            , float defaultTransparency , float initBrightness, float initTransparency) {
        this.defaultBrightness = defaultBrightness;
        this.defaultTransparency = defaultTransparency;
        this.initBrightness = initBrightness;
        this.initTransparency = initTransparency;
        this.sourceBufferedImage = sourceBufferedImage;
        this.state = state;

        setBrightness(initBrightness);
        setTransparency(initTransparency);
        setupImageView();
    }

    public void setOnStyleSetListener(OnStyleSetListener listener) {
        onStyleSetListener = listener;
    }

    public void resetToDefaultValue() {
        setBrightness(defaultBrightness);
        setTransparency(defaultTransparency);
        updateStyle();
    }

    private void setBrightness(float value) {
        float sliderValue = convertToBrightnessSliderValue(value);
        brightnessSlider.setValue(sliderValue);
        brightnessTextField.setText(getStringFromBrightnessSliderValue(sliderValue));
    }

    private void setTransparency(float value) {
        int roundedValue = (int) Math.round(value);
        transparencySlider.setValue(roundedValue);
        transparencyTextField.setText(String.valueOf(roundedValue));
    }

    private void setupImageView() {
        updateStyle();
    }

    private void updateStyle() {
        float brightnessFactor = (float) convertToBrightnessExactValue(brightnessSlider.getValue())/100.0f + 1;
        float transparencyFactor = (float) Math.abs(transparencySlider.getValue()/100.0f - 1.0f);
        RescaleOp rescale = new RescaleOp(
                new float[]{brightnessFactor, brightnessFactor, brightnessFactor, transparencyFactor},
                new float[]{0f, 0f, 0f, 0f}, null);
        previewImageView.setImage(SwingFXUtils.toFXImage(rescale.filter(sourceBufferedImage, null), null));
    }

    public void dismissStyleDialog() {
        if (onStyleSetListener != null) {
            int brightnessValue = (int) Math.round(brightnessSlider.getValue()) - SLIDER_MID_VAL;
            onStyleSetListener.onSet(brightnessValue, (int) Math.round(transparencySlider.getValue())
                    , state);
        }
        ((Stage)styleDialogOkBtn.getScene().getWindow()).close();
    }

    public void onBrightnessDragged() {
        brightnessTextField.setText(getStringFromBrightnessSliderValue(brightnessSlider.getValue()));
    }

    public void onTransparencysDragged() {
        int value = (int) Math.round(transparencySlider.getValue());
        transparencyTextField.setText(String.valueOf(value));
    }

    private String getStringFromBrightnessSliderValue(double value) {
        double exactValue = convertToBrightnessExactValue(value);
        int roundedValue = (int) Math.round(exactValue);
        return (roundedValue >= 0 ? "+" : "") + String.valueOf(roundedValue);
    }

    private float convertToBrightnessSliderValue(float value) {
        return value + SLIDER_MID_VAL;
    }

    private double convertToBrightnessExactValue(double value) {
        return value - SLIDER_MID_VAL;
    }

}
