package pharmacy_pos;

import java.time.LocalDate;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class MedicinesWindow {

    private BorderPane bp1;
    private TableView<Medicine> t1;

    private ObservableList<Medicine> list1;
    private FilteredList<Medicine> fList1;

    private final MedicineDAO dao = new MedicineDAO();

    public MedicinesWindow() {}

    public BorderPane getView() {
        if (bp1 == null) {
            buildView();
        }
        return bp1;
    }

    private void buildView() {

        t1 = new TableView<>();

        TableColumn<Medicine, Integer> tc1 = new TableColumn<>("ID");
        tc1.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Medicine, String> tc2 = new TableColumn<>("Name");
        tc2.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Medicine, String> tc3 = new TableColumn<>("Type");
        tc3.setCellValueFactory(c -> {
            boolean x = c.getValue().isRequiresPrescription();
            return new SimpleStringProperty(x ? "Rx" : "OTC");
        });

        TableColumn<Medicine, Double> tc4 = new TableColumn<>("Price");
        tc4.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Medicine, Integer> tc5 = new TableColumn<>("Stock");
        tc5.setCellValueFactory(new PropertyValueFactory<>("stockQty"));

        TableColumn<Medicine, LocalDate> tc6 = new TableColumn<>("Expiry");
        tc6.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        t1.getColumns().addAll(tc1, tc2, tc3, tc4, tc5, tc6);
        t1.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t1.setStyle("-fx-background-color: #ffffff; -fx-border-color: #c0c4cc;");

        list1 = FXCollections.observableArrayList(dao.getAllMedicines());
        fList1 = new FilteredList<>(list1, m -> true);

        t1.setItems(fList1);

        TextField f1 = new TextField();
        f1.setPromptText("Search by name...");
        f1.textProperty().addListener((o, a, b) -> {
            String s = (b == null ? "" : b.toLowerCase().trim());
            if (s.isEmpty()) {
                fList1.setPredicate(m -> true);
            } else {
                fList1.setPredicate(m -> m.getName() != null && m.getName().toLowerCase().contains(s));
            }
        });

        Label l1 = new Label("Medicines & Inventory");
        l1.setStyle(
            "-fx-font-size: 19px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #162a60;"
        );

        TextField f2 = new TextField();
        f2.setPromptText("Name");

        TextField f3 = new TextField();
        f3.setPromptText("Price");

        TextField f4 = new TextField();
        f4.setPromptText("Stock");

        DatePicker d1 = new DatePicker();
        d1.setPromptText("Expiry");

        CheckBox c1 = new CheckBox("Requires prescription");

        Button b1 = new Button("Add");
        Button b2 = new Button("Update");
        Button b3 = new Button("Delete");
        Button b4 = new Button("Clear");

        b1.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        b2.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        b3.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        b4.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        HBox hb1 = new HBox(10);
        hb1.setAlignment(Pos.CENTER_LEFT);
        hb1.setPadding(new Insets(10, 0, 0, 0));
        hb1.getChildren().addAll(f2, f3, f4, d1, c1, b1, b2, b3, b4);

        b1.setOnAction(e -> {
            if (f2.getText().isEmpty()) return;

            Medicine m = new Medicine();
            m.setName(f2.getText());
            m.setPrice(pDouble(f3.getText()));
            m.setStockQty(pInt(f4.getText()));
            m.setExpiryDate(d1.getValue());
            m.setRequiresPrescription(c1.isSelected());

            dao.insertMedicine(m);
            list1.setAll(dao.getAllMedicines());
            clearForm(f2, f3, f4, d1, c1);
        });

        b2.setOnAction(e -> {
            Medicine m = t1.getSelectionModel().getSelectedItem();
            if (m == null) return;

            m.setName(f2.getText());
            m.setPrice(pDouble(f3.getText()));
            m.setStockQty(pInt(f4.getText()));
            m.setExpiryDate(d1.getValue());
            m.setRequiresPrescription(c1.isSelected());

            dao.updateMedicine(m);
            list1.setAll(dao.getAllMedicines());
            clearForm(f2, f3, f4, d1, c1);
        });

        b3.setOnAction(e -> {
            Medicine m = t1.getSelectionModel().getSelectedItem();
            if (m == null) return;

            dao.deleteMedicine(m.getId());
            list1.setAll(dao.getAllMedicines());
            clearForm(f2, f3, f4, d1, c1);
        });

        b4.setOnAction(e -> {
            clearForm(f2, f3, f4, d1, c1);
            t1.getSelectionModel().clearSelection();
        });

        t1.setOnMouseClicked(e -> {
            Medicine m = t1.getSelectionModel().getSelectedItem();
            if (m != null) {
                f2.setText(m.getName());
                f3.setText(String.valueOf(m.getPrice()));
                f4.setText(String.valueOf(m.getStockQty()));
                d1.setValue(m.getExpiryDate());
                c1.setSelected(m.isRequiresPrescription());
            }
        });

        VBox vb1 = new VBox(10);
        vb1.setPadding(new Insets(10));
        vb1.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-border-color: #d0d0d0;" +
            "-fx-border-radius: 6;" +
            "-fx-background-radius: 6;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);" +
            "-fx-padding: 15;"
        );

        vb1.getChildren().addAll(l1, f1, t1, new Label("------------------------------"), hb1);

        bp1 = new BorderPane();
        bp1.setCenter(vb1);
        BorderPane.setMargin(vb1, new Insets(10));
    }

    private void clearForm(TextField f2, TextField f3, TextField f4,
                           DatePicker d1, CheckBox c1) {
        f2.clear();
        f3.clear();
        f4.clear();
        d1.setValue(null);
        c1.setSelected(false);
    }

    private double pDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0.0; }
    }

    private int pInt(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }
}
