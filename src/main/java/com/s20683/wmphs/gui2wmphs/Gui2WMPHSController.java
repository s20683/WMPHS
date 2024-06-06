package com.s20683.wmphs.gui2wmphs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.s20683.config.PLCInitializer;
import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.carrier.CarrierService;
import com.s20683.wmphs.destination.DestinationService;
import com.s20683.wmphs.gui2wmphs.request.*;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.line.LineService;
import com.s20683.wmphs.order.CreateOrderService;
import com.s20683.wmphs.order.OrderService;
import com.s20683.wmphs.product.ProductService;
import com.s20683.wmphs.stock.StockService;
import com.s20683.wmphs.user.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/gui2wmphs")
public class Gui2WMPHSController {
    protected Logger logger = LoggerFactory.getLogger(Gui2WMPHSController.class);
    @Autowired
    private ProductService productService;
    @Autowired
    private StockService stockService;
    @Autowired
    private DestinationService destinationService;
    @Autowired
    private AppUserService userService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private CreateOrderService createOrderService;
    @Autowired
    private CarrierService carrierService;
    @Autowired
    private LineService lineService;
    public Gui2WMPHSController(ProductService productService, StockService stockService, DestinationService destinationService, AppUserService userService,
                               OrderService orderService, CreateOrderService createOrderService, CarrierService carrierService,
                               LineService lineService) {
        this.productService = productService;
        this.stockService = stockService;
        this.destinationService = destinationService;
        this.userService = userService;
        this.orderService = orderService;
        this.createOrderService = createOrderService;
        this.carrierService = carrierService;
        this.lineService = lineService;
    }

    //*************** line ************************
    @GetMapping("/getLines/{carrierId}")
    public List<LineDTO> getLines(@PathVariable int carrierId) {
        logger.info("Proceeding GET request /getLines/{}", carrierId);
        return carrierService.getCarrier(carrierId).getLines().stream().map(Line::toDTO).collect(Collectors.toList());
    }
    @DeleteMapping("/deleteLine/{lineId}")
    public SimpleResponse deleteLine(@PathVariable int lineId) {
        logger.info("Proceeding DELETE request /deleteLine/{}", lineId);
        String result = lineService.removeLine(lineId);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    //*************** /line ************************
    //*************** carrier ************************
    @DeleteMapping("/deleteCarrier/{carrierId}")
    public SimpleResponse deleteCarrier(@PathVariable int carrierId) {
        logger.info("Proceeding DELETE request /deleteCarrier/{}", carrierId);
        String result = carrierService.removeCarrier(carrierId);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @GetMapping("/getCarriers/{orderId}")
    public List<CarrierDTO> getCarriers(@PathVariable int orderId) {
        logger.info("Proceeding GET request /getCarriers/{}", orderId);
        return orderService.getOrder(orderId).getCarriers().stream().map(Carrier::toDTO).toList();
    }
    //*************** /carrier ************************
    //*************** order ************************
    @GetMapping("/getOrders")
    public List<CompletationOrderDTO> getOrders(){
        logger.info("Proceeding GET request /getOrders");
        return orderService.getCompletationOrders();
    }
    @PostMapping("/createOrder")
    public SimpleResponse createOrder(@RequestBody CreateOrderDTO createOrderDTO) throws JsonProcessingException {
        logger.info("Proceeding POST request /createOrder");
        logger.info("{}", createOrderDTO);
        try {
            String result = createOrderService.createOrder(createOrderDTO);
            if ("OK".equals(result))
                return SimpleResponse.createSimpleOk();
            else
                return new SimpleResponse(false, result);
        } catch (RuntimeException exception) {
            return new SimpleResponse(false, exception.getMessage());
        }
    }
    @DeleteMapping("/deleteOrder/{orderId}")
    public SimpleResponse deleteOrder(@PathVariable int orderId) {
        logger.info("Proceeding DELETE request /deleteOrder/{}", orderId);
        String result = orderService.removeOrder(orderId);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    //*************** /order ************************
    //*************** stock ************************
    @PostMapping("/addStock")
    public SimpleResponse addStock(@RequestBody StockDTO stockDTO) throws JsonProcessingException {
        logger.info("Proceeding POST request /addStock");
        logger.info("{}", stockDTO);
        String result = stockService.addStock(stockDTO);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @GetMapping("/getStocks")
    public List<StockDTO> getStocks(){
        logger.info("Proceeding GET request /getStocks");
        return stockService.getStocks();
    }
    @GetMapping("/getCompressedStocks")
    public List<CompressedStockDTO> getCompressedStocks(){
        logger.info("Proceeding GET request /getCompressedStocks");
        return stockService.getCompressedStocks();
    }
    @DeleteMapping("/deleteStock/{stockId}")
    public SimpleResponse deleteStock(@PathVariable int stockId) {
        logger.info("Proceeding DELETE request /deleteStock/{}", stockId);
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
        logger.info("Proceeding DELETE request /deleteProduct/{}", productId);
        String result = productService.removeProduct(productId);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @PostMapping("/addProduct")
    public SimpleResponse addProduct(@RequestBody ProductDTO productDTO) throws JsonProcessingException {
        logger.info("Proceeding POST request /addProduct");
        logger.info("{}", productDTO);
        String result = productService.addProduct(productDTO);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @GetMapping("/getProducts")
    public List<ProductDTO> getProducts(){
        logger.info("Proceeding GET request /getProducts");
        return productService.getProducts();
    }
    //*************** /product ***********************
    //*************** destination ***********************
    @GetMapping("/getDestinations")
    public List<DestinationDTO> getDestinations(){
        logger.info("Proceeding GET request /getDestinations");
        return destinationService.getDestinations();
    }
    @PostMapping("/addDestination")
    public SimpleResponse addDestination(@RequestBody DestinationDTO destinationDTO) throws JsonProcessingException {
        logger.info("Proceeding POST request /addDestination");
        logger.info("{}", destinationDTO);
        String result = destinationService.addDestination(destinationDTO);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @DeleteMapping("/deleteDestination/{destinationId}")
    public SimpleResponse deleteDestination(@PathVariable int destinationId) {
        logger.info("Proceeding DELETE request /deleteDestination/{}", destinationId);
        String result = destinationService.removeDestination(destinationId);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    //*************** /destination ***********************
    //*************** user ***********************
    @GetMapping("/getUsers")
    public List<AppUserDTO> getUsers(){
        logger.info("Proceeding GET request /getUsers");
        return userService.getAppUsers();
    }
    @PostMapping("/addUser")
    public SimpleResponse addUser(@RequestBody AppUserDTO appUserDTO) throws JsonProcessingException {
        logger.info("Proceeding POST request /addUser");
        logger.info("{}", appUserDTO);
        String result = userService.addAppUser(appUserDTO);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    @DeleteMapping("/deleteUser/{userId}")
    public SimpleResponse deleteUser(@PathVariable int userId) {
        logger.info("Proceeding DELETE request /deleteUser/{}", userId);
        String result = userService.removeUser(userId);
        if ("OK".equals(result))
            return SimpleResponse.createSimpleOk();
        else
            return new SimpleResponse(false, result);
    }
    //*************** /user ***********************
}
