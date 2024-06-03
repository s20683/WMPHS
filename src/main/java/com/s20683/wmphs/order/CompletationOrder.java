package com.s20683.wmphs.order;

import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.destination.Destination;
import com.s20683.wmphs.user.AppUser;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "CompletationOrder")
public class CompletationOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "carrier_volume")
    private Integer carrierVolume;

    @Column(nullable = false)
    private Integer state;

    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
    @OneToMany(mappedBy = "completationOrder")
    private Set<Carrier> carriers;
}