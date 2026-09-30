package edu.labs;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public final class Ui {
    private Ui() {}

    public static Label label(String text, String style) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }

    public static Button button(String text, Runnable action) {
        Button button = new Button(text);
        button.setOnAction(e -> guard(action));
        return button;
    }

    public static Button primary(String text, Runnable action) {
        return button(text, action);
    }

    public static VBox card(Node... nodes) {
        return new VBox(8, nodes);
    }

    public static VBox field(String title, Node input) {
        return new VBox(4, new Label(title), input);
    }

    public static void guard(Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            error(ex);
        }
    }

    public static void error(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(ex.getMessage() == null ? ex.toString() : ex.getMessage());
        alert.showAndWait();
    }

    public static void show(Stage stage, String title, String subtitle, Node center, Label footer) {
        Label heading = new Label(title + (subtitle == null || subtitle.isBlank() ? "" : "\n" + subtitle));
        VBox top = new VBox(heading);
        top.setSpacing(2);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(center);
        root.setBottom(footer);

        Scene scene = new Scene(root, 1160, 800);
        stage.setTitle(title);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }
}
