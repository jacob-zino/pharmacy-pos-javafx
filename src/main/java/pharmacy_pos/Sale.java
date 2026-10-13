package pharmacy_pos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Sale {

    private int id;
    private LocalDateTime saleDateTime;
    private double totalAmount;
    private int userId;
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getSaleDateTime() {
        return saleDateTime;
    }

    public void setSaleDateTime(LocalDateTime saleDateTime) {
        this.saleDateTime = saleDateTime;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void addItem(SaleItem item) {
        items.add(item);
        recalcTotal();
    }

    private void recalcTotal() {
        double sum = 0;
        for (SaleItem it : items) {
            sum += it.getLineTotal();
        }
        totalAmount = sum;
    }
}