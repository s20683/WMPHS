package com.s20683.wmphs.destination;

import com.s20683.wmphs.order.CompletationOrder;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "Destination")
public class Destination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 15, nullable = false)
    private String name;

    @Column(length = 100, nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer target;

    @OneToMany(mappedBy = "destination")
    private Set<CompletationOrder> orders;

}