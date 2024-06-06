package com.s20683.wmphs.line;

import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.gui2wmphs.request.LineDTO;
import com.s20683.wmphs.product.Product;
import com.s20683.wmphs.stock.AllocatedStock;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "Line")
@NoArgsConstructor
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

    @OneToMany(mappedBy = "line", fetch = FetchType.EAGER)
    private List<AllocatedStock> allocatedStocks = new ArrayList<>();

    public Line(Integer quantity, Integer quantityCompleted, Product product, Carrier carrier) {
        this.quantity = quantity;
        this.quantityCompleted = quantityCompleted;
        this.product = product;
        this.carrier = carrier;
    }
    public LineDTO toDTO(){
        return new LineDTO(id, quantity, quantityCompleted, product.getId(),product.getName(), carrier.getId());
    }

    @Override
    public String toString() {
        return "Line{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", quantityCompleted=" + quantityCompleted +
                ", product=" + product +
                ", carrier=" + carrier.getId() +
                '}';
    }
}