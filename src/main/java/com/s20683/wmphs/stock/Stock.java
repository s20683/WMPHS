package com.s20683.wmphs.stock;

import com.s20683.wmphs.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "Stock")
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "exp_date", nullable = false)
    private java.sql.Date expDate;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

}