package pharmacy_pos;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class POSWindow {

    private final User u1;
    private final MedicineDAO dao1 = new MedicineDAO();
    private final SaleDAO dao2 = new SaleDAO();

    private BorderPane bp1;

    private TableView<Medicine> t1;
    private TableView<SaleItem> t2;

    private ObservableList<Medicine> list1;
    private FilteredList<Medicine> fList1;
    private ObservableList<SaleItem> list2;

    private Label l1;
    private TextField f1;
    private TextField f2;
    private TextArea ta1;

    public POSWindow(User u1) {
        this.u1 = u1;
    }

    public BorderPane getView() {
        if (bp1 == null) {
            buildView();
        }
        return bp1;
    }

    private void buildView() {

        t1 = new TableView<>();

        TableColumn<Medicine, String> tc1 = new TableColumn<>("Name");
        tc1.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Medicine, String> tc2 = new TableColumn<>("Type");
        tc2.setCellValueFactory(c -> {
            boolean x = c.getValue().isRequiresPrescription();
            return new SimpleStringProperty(x ? "Rx" : "OTC");
        });

        TableColumn<Medicine, Double> tc3 = new TableColumn<>("Price");
        tc3.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Medicine, Integer> tc4 = new TableColumn<>("Stock");
        tc4.setCellValueFactory(new PropertyValueFactory<>("stockQty"));

        TableColumn<Medicine, LocalDate> tc5 = new TableColumn<>("Expiry");
        tc5.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));

        t1.getColumns().addAll(tc1, tc2, tc3, tc4, tc5);
        t1.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        list1 = FXCollections.observableArrayList(dao1.getAllMedicines());
        fList1 = new FilteredList<>(list1, m -> true);
        t1.setItems(fList1);

        f1 = new TextField();
        f1.setPromptText("Search by name...");
        f1.textProperty().addListener((o, a, b) -> {
            String s = (b == null ? "" : b.toLowerCase().trim());
            if (s.isEmpty()) {
                fList1.setPredicate(m -> true);
            } else {
                fList1.setPredicate(m -> m.getName() != null && m.getName().toLowerCase().contains(s));
            }
        });

        Label l2 = new Label("Medicines");
        l2.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        f2 = new TextField();
        f2.setPromptText("Qty");
        f2.setPrefWidth(60);
        f2.setText("1");

        HBox hb1 = new HBox(5);
        hb1.setAlignment(Pos.CENTER_LEFT);
        hb1.getChildren().addAll(new Label("Qty:"), f2);

        Button b1 = new Button("Add to Cart");
        b1.setOnAction(e -> addToCart());

        VBox vb1 = new VBox(10);
        vb1.setPadding(new Insets(10));
        vb1.setStyle("-fx-background-color: white; -fx-border-color: #d0d7de;");
        vb1.getChildren().addAll(l2, f1, t1, hb1, b1);

        t2 = new TableView<>();

        TableColumn<SaleItem, String> tc6 = new TableColumn<>("Name");
        tc6.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getMedicine().getName()));

        TableColumn<SaleItem, Integer> tc7 = new TableColumn<>("Qty");
        tc7.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<SaleItem, Double> tc8 = new TableColumn<>("Price");
        tc8.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<SaleItem, Double> tc9 = new TableColumn<>("Total");
        tc9.setCellValueFactory(c ->
                new SimpleDoubleProperty(c.getValue().getLineTotal()).asObject()
        );

        t2.getColumns().addAll(tc6, tc7, tc8, tc9);
        t2.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        list2 = FXCollections.observableArrayList();
        t2.setItems(list2);

        Button b2 = new Button("Remove");
        b2.setOnAction(e -> removeItem());

        Button b3 = new Button("Clear");
        b3.setOnAction(e -> clearCart());

        HBox hb2 = new HBox(10);
        hb2.setAlignment(Pos.CENTER_LEFT);
        hb2.getChildren().addAll(b2, b3);

        l1 = new Label("Total: 0.0");

        Button b4 = new Button("Checkout");
        b4.setOnAction(e -> checkout());

        Button b5 = new Button("Today's Sales");
        b5.setOnAction(e -> openSalesWindow());

        HBox hb3 = new HBox(15);
        hb3.setAlignment(Pos.CENTER_RIGHT);
        hb3.getChildren().addAll(b5, l1, b4);

        Label l3 = new Label("Receipt");
        l3.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        ta1 = new TextArea();
        ta1.setEditable(false);
        ta1.setWrapText(true);

        Button b6 = new Button("Save as PDF");
        b6.setOnAction(e -> savePdf());

        VBox vb2 = new VBox(15);
        vb2.setPadding(new Insets(10));
        vb2.setStyle("-fx-background-color: white; -fx-border-color: #d0d7de;");
        vb2.getChildren().addAll(
                new Label("Cart"),
                t2,
                hb2,
                hb3,
                new Label("------------------------------"),
                l3,
                ta1,
                b6
        );

        bp1 = new BorderPane();
        bp1.setLeft(vb1);
        bp1.setCenter(vb2);

        BorderPane.setMargin(vb1, new Insets(10));
        BorderPane.setMargin(vb2, new Insets(10));
    }

    private void addToCart() {
        Medicine m = t1.getSelectionModel().getSelectedItem();
        if (m == null) return;

        int q = parseInt(f2.getText());
        if (q <= 0) {
            alert("Quantity must be at least 1.");
            return;
        }

        if (q > m.getStockQty()) {
            alert("Not enough stock. Available: " + m.getStockQty());
            return;
        }

        SaleItem existing = null;
        for (SaleItem x : list2) {
            if (x.getMedicine().getId() == m.getId()) {
                existing = x;
                break;
            }
        }

        if (existing != null) {
            int newQty = existing.getQuantity() + q;
            if (newQty > m.getStockQty()) {
                alert("Cannot exceed stock.");
                return;
            }
            existing.setQuantity(newQty);
        } else {
            list2.add(new SaleItem(m, q, m.getPrice()));
        }

        t2.refresh();
        calcTotal();
    }

    private void removeItem() {
        SaleItem s = t2.getSelectionModel().getSelectedItem();
        if (s != null) {
            list2.remove(s);
            calcTotal();
        }
    }

    private void clearCart() {
        list2.clear();
        calcTotal();
    }

    private void calcTotal() {
        double sum = 0;
        for (SaleItem x : list2) sum += x.getLineTotal();
        l1.setText("Total: " + sum);
    }

    private void checkout() {
        if (list2.isEmpty()) return;

        Sale s = new Sale();
        s.setUserId(u1.getId());
        s.setSaleDateTime(LocalDateTime.now());

        for (SaleItem x : list2) s.addItem(x);

        boolean ok = dao2.saveSale(s);

        if (ok) {
            for (SaleItem x : list2) {
                dao1.updateStockAfterSale(x.getMedicine().getId(), x.getQuantity());
            }

            buildReceipt(s);
            list2.clear();
            calcTotal();
            list1.setAll(dao1.getAllMedicines());
        } else {
            alert("Error saving sale.");
        }
    }

    private void openSalesWindow() {
        double total = dao2.getTodaySalesTotal();
        int count = dao2.getTodaySalesCount();

        Label l4 = new Label("Today's Sales");
        l4.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        VBox vb = new VBox(10);
        vb.setPadding(new Insets(10));
        vb.getChildren().addAll(
                l4,
                new Label("Date: " + LocalDate.now()),
                new Label("Sales Count: " + count),
                new Label("Total Amount: " + total)
        );

        Stage st = new Stage();
        st.setTitle("Today's Sales");
        st.setScene(new Scene(new BorderPane(vb), 300, 180));
        st.initOwner(bp1.getScene().getWindow());
        st.show();
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); }
        catch (Exception e) { return 0; }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }

    private void buildReceipt(Sale s) {
        StringBuilder sb = new StringBuilder();

        sb.append("Pharmacy POS - Receipt\n");
        sb.append("----------------------------------------\n");
        sb.append("Date: ").append(s.getSaleDateTime()).append("\n");
        sb.append("Cashier: ").append(u1.getUsername()).append("\n\n");

        sb.append(String.format("%-20s %5s %8s %8s%n", "Item", "Qty", "Price", "Total"));
        sb.append("----------------------------------------\n");

        for (SaleItem x : s.getItems()) {
            boolean rx = x.getMedicine().isRequiresPrescription();
            String name = (rx ? "[Rx] " : "[OTC] ") + x.getMedicine().getName();

            sb.append(String.format("%-20s %5d %8.2f %8.2f%n",
                    name, x.getQuantity(), x.getUnitPrice(), x.getLineTotal()));
        }

        sb.append("----------------------------------------\n");
        sb.append(String.format("TOTAL: %.2f%n", s.getTotalAmount()));

        ta1.setText(sb.toString());
    }

    private void savePdf() {
        if (ta1.getText().isEmpty()) {
            alert("No receipt to save.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File desktop = new File(System.getProperty("user.home"), "Desktop");
        if (desktop.exists()) fc.setInitialDirectory(desktop);

        File f = fc.showSaveDialog(bp1.getScene().getWindow());
        if (f == null) return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 10);
                cs.newLineAtOffset(50, 750);

                for (String line : ta1.getText().split("\\r?\\n")) {
                    cs.showText(line);
                    cs.newLineAtOffset(0, -14);
                }

                cs.endText();
            }

            doc.save(f);
            alert("Saved:\n" + f.getAbsolutePath());

        } catch (IOException e) {
            alert("Error saving PDF.");
        }
    }
}
