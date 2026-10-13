package pharmacy_pos;

import java.time.LocalDate;

public class Medicine {

    private int id;
    private String name;
    private double price;
    private int stockQty;
    private LocalDate expiryDate;
    private boolean requiresPrescription;

    public Medicine() {
    }

    public Medicine(int id, String name, double price, int stockQty,
                    LocalDate expiryDate, boolean requiresPrescription) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stockQty = stockQty;
        this.expiryDate = expiryDate;
        this.requiresPrescription = requiresPrescription;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQty() {
        return stockQty;
    }

    public void setStockQty(int stockQty) {
        this.stockQty = stockQty;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRequiresPrescription() {
        return requiresPrescription;
    }

    public void setRequiresPrescription(boolean requiresPrescription) {
        this.requiresPrescription = requiresPrescription;
    }
}