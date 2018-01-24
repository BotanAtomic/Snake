package snake;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Pane root = new Pane();

        ImageView imageView = new ImageView();

        imageView.setFitHeight(500);
        imageView.setFitWidth(500);

        imageView.setLayoutX(0);
        imageView.setLayoutY(0);

        root.getChildren().add(imageView);

        Scene scene = new Scene(root);

        primaryStage.setTitle("Snake");
        primaryStage.setScene(scene);
        primaryStage.show();
        new SnakeGame(imageView, primaryStage);

    }
}
