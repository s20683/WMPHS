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

    @Column(name = "destination_id", nullable = false)
    private Integer destinationId;

    @Transient
    private Destination destination;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Transient
    private AppUser user;

    @Transient
    private List<Carrier> carriers = new ArrayList<>();

    public CompletationOrder(Integer carrierVolume, Integer state, Destination destination, AppUser user) {
        this.carrierVolume = carrierVolume;
        this.state = state;
        this.destination = destination;
        this.destinationId = destination.getId();
        this.user = user;
        this.userId = user.getId();
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

    public void setDestination(Destination destination) {
        this.destination = destination;
        this.destinationId = destination.getId();
    }

    public void setUser(AppUser user) {
        this.user = user;
        this.userId = user.getId();
    }

    @Override
    public String toString() {
        return "CompletationOrder{" +
                "id=" + id +
                ", carrierVolume=" + carrierVolume +
                ", state=" + state +
                ", destinationId=" + destinationId +
                ", destination=" + destination +
                ", userId=" + userId +
                ", user=" + user +
                ", carriers=" + carriers +
                '}';
    }
}