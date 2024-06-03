package com.s20683.wmphs.carrier;

import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.order.CompletationOrder;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Carrier")
public class Carrier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 20, nullable = false)
    private String barcode;

    @Column(nullable = false)
    private Integer volume;

    @ManyToOne
    @JoinColumn(name = "completation_order_id", nullable = false)
    private CompletationOrder completationOrder;

    @OneToMany(mappedBy = "carrier")
    private Set<Line> lines;

}