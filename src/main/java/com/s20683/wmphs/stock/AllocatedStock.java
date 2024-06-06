package com.s20683.wmphs.stock;

import com.s20683.wmphs.line.Line;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "AllocatedStock")
@Setter
@Getter
@NoArgsConstructor
public class AllocatedStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "exp_date", nullable = false)
    private java.sql.Date expDate;

    @ManyToOne()
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne()
    @JoinColumn(name = "line_id", nullable = false)
    private Line line;

    public AllocatedStock(Integer quantity, Date expDate, Stock stock, Line line) {
        this.quantity = quantity;
        this.expDate = expDate;
        this.stock = stock;
        this.line = line;
    }

    @Override
    public String toString() {
        return "AllocatedStock{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", expDate=" + expDate +
                ", stock=" + stock.getId() +
                ", line=" + line.getId() +
                '}';
    }
}
