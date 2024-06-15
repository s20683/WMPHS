package com.s20683.wmphs.gui2wmphs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDTO {
    private int carrierVolume;
    private int destinationId;
    private int userId;
    private List<CompressedStockDTO> lines;
}
