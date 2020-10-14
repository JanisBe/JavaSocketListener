package streamingdatafx;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.maven.shared.utils.StringUtils;

import java.io.IOException;

public class DisplayController {
    @FXML
    private Label speedLabel;

    @FXML
    private TextArea output;

    @FXML
    private Button start;
    @FXML
    private NumberTextField portNo;

    private DataStreamParser dataStream;
    private boolean isListening = false;

    public void setSpeed(double speed) {
        speedLabel.setText(String.format("%.1f", speed));
    }

    public void setText(String text) {
        output.appendText(text + "\n");
    }

    public void start(ActionEvent actionEvent) throws IOException {
        if (!isListening) {
            if (StringUtils.isEmpty(portNo.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("No port specified");
                alert.show();
                return;
            }
            isListening = true;
            start.setText("Stop listening");
            dataStream = new DataStreamParser(Integer.parseInt(portNo.getText()));
            dataStream.speedProperty().addListener((obs, oldSpeed, newSpeed) -> {
                // update controller on FX Application Thread:
                Platform.runLater(() -> setSpeed(newSpeed.doubleValue()));
            });
            dataStream.messageProperty().addListener((observable, oldValue, newValue) -> {
                Platform.runLater(() -> setText(newValue));
            });
        } else {
            dataStream.getServerSocket().close();
            dataStream.shutdown();
            isListening = false;
            start.setText("Start listening");
        }
    }
}
