package com.s20683.wmphs.gui2wmphs.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompletationOrderDTO {
    private int id;
    private int carrierVolume;
    private int state;
    private int destinationId;
    private String destinationName;
    private int userId;
    private String userName;
}
