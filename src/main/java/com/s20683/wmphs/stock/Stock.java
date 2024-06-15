package com.s20683.wmphs.stock;

import com.s20683.wmphs.gui2wmphs.request.StockDTO;
import com.s20683.wmphs.product.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Stock")
@Getter
@Setter
@NoArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "exp_date", nullable = false)
    private java.sql.Date expDate;

    @Column(nullable = false)
    private Integer allocatedQuantity;

    @Transient
    private Integer notAllocatedQuantity;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Transient
    private Product product;

    @Transient
    private List<AllocatedStock> allocatedStocks = new ArrayList<>();

    public Stock(Integer quantity, Integer allocatedQuantity, Date expDate, Product product) {
        this.quantity = quantity;
        this.allocatedQuantity = allocatedQuantity;
        this.expDate = expDate;
        this.product = product;
        this.productId = product.getId();
        this.notAllocatedQuantity = quantity - allocatedQuantity;
    }
    public StockDTO toDTO(){
        return new StockDTO(id, expDate.toLocalDate(), quantity, allocatedQuantity, product.getId(), product.getName());
    }
    public void addAllocatedStock(AllocatedStock allocatedStock) {
        allocatedStocks.add(allocatedStock);
    }
    public void removeAllocatedStock(AllocatedStock allocatedStock) {
        allocatedStocks.remove(allocatedStock);
    }
    public void completeQuantity(int quantity) {
        allocatedQuantity -= quantity;
        this.quantity -= quantity;
    }

    public void setProduct(Product product) {
        this.product = product;
        this.productId = product.getId();
    }

    public int getAvailableQuantity(){
        return quantity - allocatedQuantity;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", expDate=" + expDate +
                ", allocatedQuantity=" + allocatedQuantity +
                ", notAllocatedQuantity=" + notAllocatedQuantity +
                ", productId=" + productId +
                ", allocatedStocks=" + allocatedStocks +
                '}';
    }
}