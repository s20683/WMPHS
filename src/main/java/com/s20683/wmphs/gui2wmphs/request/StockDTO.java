package com.s20683.wmphs.gui2wmphs.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StockDTO {
    private int id;
    private LocalDate expDate;
    private int quantity;
    private int allocatedQuantity;
    private int notAllocatedQuantity;
    private int productId;
    private String productName;

    public StockDTO(int id, LocalDate expDate, int quantity, int allocatedQuantity,int productId, String productName) {
        this.id = id;
        this.expDate = expDate;
        this.quantity = quantity;
        this.allocatedQuantity = allocatedQuantity;
        this.productId = productId;
        this.productName = productName;
        this.notAllocatedQuantity = quantity - allocatedQuantity;
    }
}
