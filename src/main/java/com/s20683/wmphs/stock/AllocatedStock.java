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
public class AllocatedStock implements Comparable<AllocatedStock> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "exp_date", nullable = false)
    private java.sql.Date expDate;

    @Column(name = "stock_id", nullable = false)
    private Integer stockId;

    @Transient
    private Stock stock;

    @Column(name = "line_id", nullable = false)
    private Integer lineId;

    @Transient
    private Line line;

    public AllocatedStock(Integer quantity, Date expDate, Stock stock, Line line) {
        this.quantity = quantity;
        this.expDate = expDate;
        this.stock = stock;
        this.stockId = stock.getId();
        this.line = line;
        this.lineId = line.getId();
    }

    public void setStock(Stock stock) {
        this.stock = stock;
        this.stockId = stock.getId();
    }

    public void setLine(Line line) {
        this.line = line;
        this.lineId = line.getId();
    }

    @Override
    public String toString() {
        return "AllocatedStock{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", expDate=" + expDate +
                '}';
    }

    @Override
    public int compareTo(AllocatedStock o) {
        return this.expDate.compareTo(o.getExpDate());
    }
}
