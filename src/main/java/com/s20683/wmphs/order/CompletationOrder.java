package com.s20683.wmphs.order;

import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.destination.Destination;
import com.s20683.wmphs.gui2wmphs.request.CompletationOrderDTO;
import com.s20683.wmphs.user.AppUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "CompletationOrder")
@NoArgsConstructor
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

    @OneToMany(mappedBy = "completationOrder", fetch = FetchType.EAGER)
    private List<Carrier> carriers = new ArrayList<>();

    public CompletationOrder(Integer carrierVolume, Integer state, Destination destination, AppUser user) {
        this.carrierVolume = carrierVolume;
        this.state = state;
        this.destination = destination;
        this.user = user;
    }
    public void addCarrier(Carrier carrier) {
        if (carrier != null)
            carriers.add(carrier);
    }
    public void removeCarrier(Carrier carrier) {
        carriers.remove(carrier);
    }

    public CompletationOrderDTO toDTO(){
        return new CompletationOrderDTO(id, carrierVolume, state, destination.getId(), destination.getName(), user.getId(), user.getName());
    }
}