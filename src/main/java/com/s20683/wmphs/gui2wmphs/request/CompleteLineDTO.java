package com.s20683.wmphs.gui2wmphs.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompleteLineDTO {
    private int orderId;
    private LineDTO line;
}
