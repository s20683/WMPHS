package com.s20683.wmphs.product;

import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.stock.Stock;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 15, nullable = false)
    private String name;

    @Column(length = 10, nullable = false)
    private String location;

    @Column(nullable = false)
    private Integer volume;

    @OneToMany(mappedBy = "product")
    private Set<Line> lines;

    @OneToMany(mappedBy = "product")
    private Set<Stock> stocks;
}