package com.s20683.wmphs.stock;

import com.s20683.wmphs.gui2wmphs.request.StockDTO;
import com.s20683.wmphs.product.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Table(name = "Stock")
@Data
@NoArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "exp_date", nullable = false)
    private java.sql.Date expDate;

    @ManyToOne()
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public Stock(Integer quantity, Date expDate, Product product) {
        this.quantity = quantity;
        this.expDate = expDate;
        this.product = product;
    }
    public StockDTO toDTO(){
        return new StockDTO(id, expDate.toLocalDate(), quantity, product.getId(), product.getName());
    }
}