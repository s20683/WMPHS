package com.s20683.wmphs.gui2wmphs.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductDTO {
    private int id;
    private String name;
    private String location;
    private int volume;

}
