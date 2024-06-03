package com.s20683.wmphs.line;

import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.product.Product;
import jakarta.persistence.*;

@Entity
@Table(name = "Line")
public class Line {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "quantity_completed", nullable = false)
    private Integer quantityCompleted;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;
}