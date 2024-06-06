package com.s20683.wmphs.gui2wmphs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineDTO {
    private int id;
    private int quantity;
    private int quantityCompleted;
    private int productId;
    private String productName;
    private String productLocation;
    private int carrierId;
}
