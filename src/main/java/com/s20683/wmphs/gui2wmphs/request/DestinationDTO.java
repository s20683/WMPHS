package com.s20683.wmphs.gui2wmphs.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationDTO {
    private int id;
    private String name;
    private String address;
    private int target;
}
