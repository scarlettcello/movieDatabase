package hyon;

import java.util.Optional;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * This class starts/ends the Movie DB program
 * 
 * @author Hyon Lee
 */
public class MovieDatabase extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("FXMLDocument.fxml")
        );
        Pane root = (Pane) loader.load();
        FXMLDocumentController controller 
            = loader.<FXMLDocumentController>getController();
        Scene scene = new Scene(root);
        stage.setTitle("Movie DB");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent we) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Exit Program");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you wish to exit?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    controller.getJdbc().disconnect();
                } else {
                    we.consume();
                }
            }
        });        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);       
    }
}
