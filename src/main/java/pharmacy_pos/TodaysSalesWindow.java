package pharmacy_pos;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class TodaysSalesWindow {

    private final BorderPane root = new BorderPane();

    private final TableView<SaleRow> t1 = new TableView<>();
    private final TableView<SaleItemRow> t2 = new TableView<>();

    private final ObservableList<SaleRow> salesData = FXCollections.observableArrayList();
    private final ObservableList<SaleItemRow> itemsData = FXCollections.observableArrayList();

    private final Label l1 = new Label("Order Total: 0.00");
    private final Label l2 = new Label("Total for range: 0.00");

    private final DatePicker d1 = new DatePicker();
    private final DatePicker d2 = new DatePicker();

    private final DateTimeFormatter dtf =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final User loggedInUser;

    public TodaysSalesWindow(User user) {
        this.loggedInUser = user;

        buildUI();

        LocalDate today = LocalDate.now();
        d1.setValue(today);
        d2.setValue(today);
        loadSales(today, today);
    }

    public Pane getView() {
        return root;
    }

    private void buildUI() {

        Label l3 = new Label("Sales");
        l3.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label l4 = new Label("From:");
        Label l5 = new Label("To:");

        Button b1 = new Button("Today");
        b1.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            d1.setValue(today);
            d2.setValue(today);
            loadSales(today, today);
        });

        Button b2 = new Button("Apply");
        b2.setOnAction(e -> applyDateFilter());

        HBox filterBox = new HBox(10, l4, d1, l5, d2, b1, b2);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        
        TableColumn<SaleRow, Integer> tc1 = new TableColumn<>("Order #");
        tc1.setCellValueFactory(new PropertyValueFactory<>("id"));
        tc1.setPrefWidth(80);

        TableColumn<SaleRow, String> tc2 = new TableColumn<>("Date/Time");
        tc2.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getDateTime() == null
                                ? ""
                                : dtf.format(cell.getValue().getDateTime())
                )
        );
        tc2.setPrefWidth(160);

        TableColumn<SaleRow, Double> tc3 = new TableColumn<>("Total");
        tc3.setCellValueFactory(new PropertyValueFactory<>("total"));
        tc3.setPrefWidth(90);

        TableColumn<SaleRow, Integer> tc4 = new TableColumn<>("User");
        tc4.setCellValueFactory(new PropertyValueFactory<>("userId"));
        tc4.setPrefWidth(70);

        t1.getColumns().addAll(tc1, tc2, tc3, tc4);
        t1.setItems(salesData);
        t1.setPrefHeight(260);
        t1.setPlaceholder(new Label("No sales."));

        t1.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadItems(newVal.getId());
                l1.setText(String.format("Order Total: %.2f", newVal.getTotal()));
            } else {
                itemsData.clear();
                l1.setText("Order Total: 0.00");
            }
        });

        VBox leftBox = new VBox(10, new Label("Orders"), t1);
        leftBox.setPrefWidth(380);

        TableColumn<SaleItemRow, String> tc5 = new TableColumn<>("Medicine");
        tc5.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        tc5.setPrefWidth(170);

        TableColumn<SaleItemRow, Integer> tc6 = new TableColumn<>("Qty");
        tc6.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        tc6.setPrefWidth(60);

        TableColumn<SaleItemRow, Double> tc7 = new TableColumn<>("Price");
        tc7.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        tc7.setPrefWidth(80);

        TableColumn<SaleItemRow, Double> tc8 = new TableColumn<>("Total");
        tc8.setCellValueFactory(new PropertyValueFactory<>("lineTotal"));
        tc8.setPrefWidth(80);

        t2.getColumns().addAll(tc5, tc6, tc7, tc8);
        t2.setItems(itemsData);
        t2.setPrefHeight(260);
        t2.setPlaceholder(new Label("Select an order…"));

        VBox rightBox = new VBox(10, new Label("Order Details"), t2);
        rightBox.setPrefWidth(380);

        HBox centerTables = new HBox(20, leftBox, rightBox);


        Button b3 = new Button("Print Invoice");
        b3.setOnAction(e -> printInvoice());

        Button b4 = new Button("Delete Order");

        // Hide delete button for CASHIER
        if (loggedInUser != null && 
            "CASHIER".equalsIgnoreCase(loggedInUser.getRole())) {

            b4.setVisible(false);
            b4.setManaged(false);
        }

        b4.setOnAction(e -> deleteSelectedOrder());

        HBox actionsBox = new HBox(10, b3, b4);
        actionsBox.setAlignment(Pos.CENTER_LEFT);

        HBox totalsBox = new HBox(20, l1, l2);
        totalsBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bottomBar = new HBox(10, actionsBox, spacer, totalsBox);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(5, 0, 0, 0));


        VBox mainBox = new VBox(10, l3, filterBox, centerTables, new Separator(), bottomBar);
        mainBox.setPadding(new Insets(10));
        mainBox.setStyle(
                "-fx-background-color: #ffffff;" +
                "-fx-border-color: #d0d0d0;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);" +
                "-fx-padding: 15;"
        );

        root.setCenter(mainBox);
        BorderPane.setMargin(mainBox, new Insets(10));
    }

    private void applyDateFilter() {
        LocalDate from = d1.getValue();
        LocalDate to = d2.getValue();

        if (from == null || to == null) {
            showAlert("Please select both From and To dates.");
            return;
        }

        if (to.isBefore(from)) {
            showAlert("To date cannot be before From date.");
            return;
        }

        loadSales(from, to);
    }

    private void loadSales(LocalDate from, LocalDate to) {
        salesData.clear();
        itemsData.clear();
        l1.setText("Order Total: 0.00");

        String sql = "SELECT id, sale_datetime, total_amount, user_id " +
                     "FROM sales WHERE DATE(sale_datetime) BETWEEN ? AND ? " +
                     "ORDER BY sale_datetime DESC";

        double totalForRange = 0.0;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    int id = rs.getInt("id");
                    Timestamp ts = rs.getTimestamp("sale_datetime");
                    LocalDateTime dt = ts != null ? ts.toLocalDateTime() : null;

                    double total = rs.getDouble("total_amount");
                    int userId = rs.getInt("user_id");

                    salesData.add(new SaleRow(id, dt, total, userId));
                    totalForRange += total;
                }
            }

        } catch (SQLException e) {
            showAlert("Error loading sales: " + e.getMessage());
        }

        l2.setText(String.format("Total for range: %.2f", totalForRange));
    }

    private void loadItems(int saleId) {
        itemsData.clear();

        String sql = """
            SELECT si.quantity, si.unit_price, si.subtotal, m.name AS medName
            FROM sale_items si
            JOIN medicines m ON si.medicine_id = m.id
            WHERE si.sale_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    itemsData.add(new SaleItemRow(
                            rs.getString("medName"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("subtotal")
                    ));
                }
            }

        } catch (SQLException e) {
            showAlert("Error loading order details: " + e.getMessage());
        }
    }

    private void deleteSelectedOrder() {
        SaleRow selected = t1.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Please select an order to delete.");
            return;
        }

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Delete order #" + selected.getId() + " ?",
                ButtonType.YES, ButtonType.NO
        );

        confirm.setHeaderText("Confirm delete");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                doDeleteOrder(selected);
            }
        });
    }

    private void doDeleteOrder(SaleRow order) {
        String sqlItems = "DELETE FROM sale_items WHERE sale_id = ?";
        String sqlSale = "DELETE FROM sales WHERE id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement psItems = conn.prepareStatement(sqlItems);
                 PreparedStatement psSale = conn.prepareStatement(sqlSale)) {

                psItems.setInt(1, order.getId());
                psItems.executeUpdate();

                psSale.setInt(1, order.getId());
                psSale.executeUpdate();

                conn.commit();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

            salesData.remove(order);
            itemsData.clear();
            recalcRangeTotal();
            l1.setText("Order Total: 0.00");

        } catch (SQLException e) {
            showAlert("Error deleting order: " + e.getMessage());
        }
    }

    private void recalcRangeTotal() {
        double sum = 0.0;
        for (SaleRow s : salesData) {
            sum += s.getTotal();
        }
        l2.setText(String.format("Total for range: %.2f", sum));
    }

    private void printInvoice() {
        SaleRow order = t1.getSelectionModel().getSelectedItem();
        if (order == null) {
            showAlert("Please select an order to print.");
            return;
        }

        if (itemsData.isEmpty()) {
            loadItems(order.getId());
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Invoice PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        chooser.setInitialFileName("invoice-" + order.getId() + ".pdf");

        File file = chooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                PDType1Font font = new PDType1Font(Standard14Fonts.FontName.COURIER);
                cs.setFont(font, 11);

                cs.beginText();
                cs.newLineAtOffset(50, 750);

                cs.showText("PHARMACY POS - INVOICE");
                cs.newLineAtOffset(0, -16);
                cs.showText("Order #: " + order.getId());
                cs.newLineAtOffset(0, -14);
                cs.showText("Date: " + (order.getDateTime() == null ? "" : dtf.format(order.getDateTime())));
                cs.newLineAtOffset(0, -20);

                cs.showText(String.format("%-20s %5s %8s %10s",
                        "Item", "Qty", "Price", "Total"));
                cs.newLineAtOffset(0, -14);
                cs.showText("-----------------------------------------------");

                for (SaleItemRow item : itemsData) {
                    cs.newLineAtOffset(0, -14);

                    String name = item.getMedicineName();
                    if (name.length() > 18) name = name.substring(0, 18);

                    String line = String.format(
                            "%-20s %5d %8.2f %10.2f",
                            name, item.getQuantity(),
                            item.getUnitPrice(),
                            item.getLineTotal()
                    );

                    cs.showText(line);
                }

                cs.newLineAtOffset(0, -20);
                cs.showText(String.format("Order Total: %.2f", order.getTotal()));

                cs.endText();
            }

            doc.save(file);
            showAlert("Invoice saved:\n" + file.getAbsolutePath());

        } catch (IOException e) {
            showAlert("Error saving PDF file.");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static class SaleRow {
        private final IntegerProperty id = new SimpleIntegerProperty();
        private final ObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<>();
        private final DoubleProperty total = new SimpleDoubleProperty();
        private final IntegerProperty userId = new SimpleIntegerProperty();

        public SaleRow(int id, LocalDateTime dt, double total, int user) {
            this.id.set(id);
            this.dateTime.set(dt);
            this.total.set(total);
            this.userId.set(user);
        }

        public int getId() { return id.get(); }
        public LocalDateTime getDateTime() { return dateTime.get(); }
        public double getTotal() { return total.get(); }
        public int getUserId() { return userId.get(); }
    }

    public static class SaleItemRow {
        private final StringProperty medicineName = new SimpleStringProperty();
        private final IntegerProperty quantity = new SimpleIntegerProperty();
        private final DoubleProperty unitPrice = new SimpleDoubleProperty();
        private final DoubleProperty lineTotal = new SimpleDoubleProperty();

        public SaleItemRow(String name, int qty, double price, double total) {
            this.medicineName.set(name);
            this.quantity.set(qty);
            this.unitPrice.set(price);
            this.lineTotal.set(total);
        }

        public String getMedicineName() { return medicineName.get(); }
        public int getQuantity() { return quantity.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getLineTotal() { return lineTotal.get(); }
    }
}
