package com.s20683.wmphs.gui2wmphs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class StockDTO {
    private int id;
    private LocalDate expDate;
    private int quantity;
    private int productId;
    private String productName;
}
