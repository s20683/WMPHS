package com.s20683.wmphs.destination;

import com.s20683.wmphs.gui2wmphs.request.DestinationDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Entity
@Table(name = "Destination")
@NoArgsConstructor
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

    public Destination(String name, String address, Integer target) {
        this.name = name;
        this.address = address;
        this.target = target;
    }
    public DestinationDTO toDTO(){
        return new DestinationDTO(id, name, address, target);
    }
}