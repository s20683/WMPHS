package com.s20683.wmphs.gui2wmphs.request;

import lombok.Data;

@Data
public class CompressedStockDTO {
    private int id;
    private int quantity;
    private int allocatedQuantity;
    private int notAllocatedQuantity;
    private int productId;
    private String productName;
    private int productVolume;

    public CompressedStockDTO(int id, int quantity, int allocatedQuantity, int productId, String productName, int productVolume) {
        this.id = id;
        this.quantity = quantity;
        this.allocatedQuantity = allocatedQuantity;
        this.productId = productId;
        this.productName = productName;
        this.productVolume = productVolume;
        this.notAllocatedQuantity = quantity - allocatedQuantity;
    }
}
