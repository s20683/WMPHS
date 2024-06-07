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

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Transient
    private Product product;

    @Column(name = "carrier_id", nullable = false)
    private Integer carrierId;

    @Transient
    private Carrier carrier;

//    @OneToMany(mappedBy = "line", fetch = FetchType.EAGER)
    @Transient
    private List<AllocatedStock> allocatedStocks = new ArrayList<>();

    public Line(Integer quantity, Integer quantityCompleted, Product product, Carrier carrier) {
        this.quantity = quantity;
        this.quantityCompleted = quantityCompleted;
        this.product = product;
        this.productId = product.getId();
        this.carrier = carrier;
        this.carrierId = carrier.getId();
    }
    public LineDTO toDTO(){
        return new LineDTO(id, quantity, quantityCompleted, product.getId(),product.getName(), product.getLocation(),carrier.getId());
    }
    public void addAllocatedStock(AllocatedStock allocatedStock) {
        allocatedStocks.add(allocatedStock);
    }
    public void removeAllocatedStock(AllocatedStock allocatedStock) {
        allocatedStocks.remove(allocatedStock);
    }
    public int getQuantityToComplete() {
        return quantity - quantityCompleted;
    }

    public void setCarrier(Carrier carrier) {
        this.carrier = carrier;
        this.carrierId = carrier.getId();
    }

    public void setProduct(Product product) {
        this.product = product;
        this.productId = product.getId();
    }

    @Override
    public String toString() {
        return "Line{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", quantityCompleted=" + quantityCompleted +
                ", product=" + product +
                '}';
    }
}