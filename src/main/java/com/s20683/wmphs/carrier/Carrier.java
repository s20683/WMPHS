package com.s20683.wmphs.carrier;

import com.s20683.wmphs.gui2wmphs.request.CarrierDTO;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.order.CompletationOrder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@Table(name = "Carrier")
@NoArgsConstructor
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

    @OneToMany(mappedBy = "carrier", fetch = FetchType.EAGER)
    private List<Line> lines = new ArrayList<>();

    public Carrier(String barcode, Integer volume, CompletationOrder completationOrder) {
        this.barcode = barcode;
        this.volume = volume;
        this.completationOrder = completationOrder;
    }
    public int getAvailableVolume(){
        int availableVolume = volume;
        for (Line line : lines) {
            availableVolume -= line.getQuantity() * line.getProduct().getVolume();
        }
        return availableVolume;
    }
    public void addLine(Line line) {
        if (line != null)
            lines.add(line);
    }
    public void removeLine(Line line) {
        lines.remove(line);
    }
    public CarrierDTO toDTO(){
        return new CarrierDTO(id, barcode, volume, completationOrder.getId());
    }

    public static final String EMPTY_BARCODE = "";

    @Override
    public String toString() {
        return "Carrier{" +
                "id=" + id +
                ", barcode='" + barcode + '\'' +
                ", volume=" + volume +
                ", completationOrder=" + completationOrder.getId() +
                ", lines=" + lines +
                '}';
    }
}