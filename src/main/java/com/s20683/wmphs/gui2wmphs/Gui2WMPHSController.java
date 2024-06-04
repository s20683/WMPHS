package com.s20683.wmphs.gui2wmphs;

import com.s20683.wmphs.gui2wmphs.request.ProductDTO;
import com.s20683.wmphs.gui2wmphs.request.SimpleResponse;
import com.s20683.wmphs.gui2wmphs.request.StockDTO;
import com.s20683.wmphs.product.ProductService;
import com.s20683.wmphs.stock.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/gui2wmphs")
public class Gui2WMPHSController {

    @Autowired
    private ProductService productService;
    @Autowired
    private StockService stockService;

    public Gui2WMPHSController(ProductService productService, StockService stockService) {
        this.productService = productService;
        this.stockService = stockService;
    }

    //*************** stock ************************
    @PostMapping("/addStock")
    public SimpleResponse addStock(@RequestBody StockDTO stockDTO) {
        String result = stockService.addStock(stockDTO);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @GetMapping("/getStocks")
    public List<StockDTO> getStocks(){
        return stockService.getStocks();
    }
    @DeleteMapping("/deleteStock/{stockId}")
    public SimpleResponse deleteStock(@PathVariable int stockId) {
        String result = stockService.removeStock(stockId);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    //*************** /stock ************************
    //*************** product ************************
    @DeleteMapping("/deleteProduct/{productId}")
    public SimpleResponse deleteProduct(@PathVariable int productId) {
        String result = productService.removeProduct(productId);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @PostMapping("/addProduct")
    public SimpleResponse addProduct(@RequestBody ProductDTO productDTO){
        String result = productService.addProduct(productDTO);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @GetMapping("/getProducts")
    public List<ProductDTO> getProducts(){
        return productService.getProducts();
    }
    //*************** /product ************************
}
