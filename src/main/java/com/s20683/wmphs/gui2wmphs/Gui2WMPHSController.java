package com.s20683.wmphs.gui2wmphs;

import com.s20683.wmphs.gui2wmphs.request.ProductDTO;
import com.s20683.wmphs.gui2wmphs.request.SimpleResponse;
import com.s20683.wmphs.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gui2wmphs")
public class Gui2WMPHSController {

    @Autowired
    private ProductService productService;

    public Gui2WMPHSController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/addProduct")
    public SimpleResponse addProduct(@RequestBody ProductDTO productDTO){
        String result = productService.addProduct(productDTO);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
}
