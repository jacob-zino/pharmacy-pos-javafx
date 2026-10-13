package pharmacy_pos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    private BorderPane root;
    private User loggedUser;

    @Override
    public void start(Stage stage) {

        Label l1 = new Label("Username:");
        TextField f1 = new TextField();

        Label l2 = new Label("Password:");
        PasswordField pf1 = new PasswordField();

        Label l3 = new Label();

        Button b1 = new Button("Login");

        GridPane gp = new GridPane();
        gp.setAlignment(Pos.CENTER);
        gp.setHgap(10);
        gp.setVgap(10);
        gp.setPadding(new Insets(25, 25, 25, 25));

        gp.add(l1, 0, 0);
        gp.add(f1, 1, 0);
        gp.add(l2, 0, 1);
        gp.add(pf1, 1, 1);
        gp.add(b1, 1, 2);
        gp.add(l3, 1, 3);

        l1.setStyle("-fx-font-size: 14px;");
        l2.setStyle("-fx-font-size: 14px;");
        l3.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        b1.setStyle(
                "-fx-background-color: #2c3e8f; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-padding: 6px 14px;"
        );

        gp.setStyle("-fx-background-color: #e4e9ff;");
        Scene scene1 = new Scene(gp, 800, 500);

        stage.setTitle("Pharmacy POS - Login");
        stage.setScene(scene1);
        stage.show();

        b1.setOnAction(e -> {
            String user = f1.getText();
            String pass = pf1.getText();

            if (user.isEmpty() || pass.isEmpty()) {
                l3.setText("Please enter username and password");
                return;
            }

            UserDAO dao = new UserDAO();
            User u = dao.verifyLogin(user, pass);

            if (u != null) {
                loggedUser = u;
                showDashboard(stage);
            } else {
                l3.setText("Invalid login");
            }
        });
    }

    private void showDashboard(Stage stage) {

        root = new BorderPane();
        root.setStyle("-fx-background-color: #f5f7ff;");

        Label l1 = new Label("Welcome, " + loggedUser.getUsername() +
                " (" + loggedUser.getRole() + ")");

        HBox hb1 = new HBox(l1);
        hb1.setPadding(new Insets(10));
        hb1.setAlignment(Pos.CENTER_LEFT);

        hb1.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-border-color: #dcdcdc;" +
                "-fx-border-width: 0 0 1 0;" +
                "-fx-padding: 12;"
        );

        Button b1 = new Button("POS");
        Button b2 = new Button("Medicines & Inventory");
        Button b3 = new Button("Sales");
        Button b4 = new Button("Logout");

        b1.setStyle("-fx-background-color: #2c3e8f; -fx-text-fill: white; -fx-padding: 6 14;");
        b2.setStyle("-fx-background-color: #2c3e8f; -fx-text-fill: white; -fx-padding: 6 14;");
        b3.setStyle("-fx-background-color: #2c3e8f; -fx-text-fill: white; -fx-padding: 6 14;");
        b4.setStyle("-fx-background-color: #ef5350; -fx-text-fill: white; -fx-padding: 6 14;");

        HBox hb2 = new HBox(10);
        hb2.setPadding(new Insets(10));
        hb2.getChildren().add(b1);
        hb2.getChildren().add(b3);

        String r = loggedUser.getRole();

        if (r.equalsIgnoreCase("ADMIN")) {
            hb2.getChildren().add(b2);
        }

        hb2.getChildren().add(b4);

        VBox vb = new VBox(hb1, hb2);
        root.setTop(vb);

        POSWindow pw = new POSWindow(loggedUser);
        root.setCenter(pw.getView());

        Scene scene2 = new Scene(root, 1000, 600);
        stage.setScene(scene2);

        b1.setOnAction(e -> {
            POSWindow w = new POSWindow(loggedUser);
            root.setCenter(w.getView());
        });

        b2.setOnAction(e -> {
            if (r.equalsIgnoreCase("ADMIN")) {
                MedicinesWindow mw = new MedicinesWindow();
                root.setCenter(mw.getView());
            }
        });

        b3.setOnAction(e -> {
            TodaysSalesWindow ts = new TodaysSalesWindow(loggedUser);
            root.setCenter(ts.getView());
        });

        b4.setOnAction(e -> {
            loggedUser = null;
            start(stage);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
