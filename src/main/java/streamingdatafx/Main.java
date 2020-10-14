package streamingdatafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class Main extends Application {

    private DataStreamParser dataStream ;

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Display.fxml"));
            GridPane root = loader.load();
            DisplayController controller = loader.getController();


            Scene scene = new Scene(root,900,500);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (dataStream != null) {
            dataStream.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
