package com.s20683.wmphs.user;

import com.s20683.wmphs.order.CompletationOrder;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "AppUser")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String name;

    @OneToMany(mappedBy = "user")
    private Set<CompletationOrder> orders;

}