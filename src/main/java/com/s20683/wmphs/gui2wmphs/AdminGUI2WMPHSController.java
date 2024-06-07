package com.s20683.wmphs.gui2wmphs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.carrier.CarrierService;
import com.s20683.wmphs.destination.DestinationService;
import com.s20683.wmphs.gui2wmphs.request.*;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.line.LineService;
import com.s20683.wmphs.order.CreateOrderService;
import com.s20683.wmphs.order.OrderService;
import com.s20683.wmphs.product.ProductService;
import com.s20683.wmphs.scheduler.SingleThreadScheduler;
import com.s20683.wmphs.stock.AllocatedStockService;
import com.s20683.wmphs.stock.StockService;
import com.s20683.wmphs.user.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/gui2wmphs")
public class AdminGUI2WMPHSController {
    protected Logger logger = LoggerFactory.getLogger(AdminGUI2WMPHSController.class);
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
    @Autowired
    private AllocatedStockService allocatedStockService;
    @Autowired
    private SingleThreadScheduler scheduler;
    public AdminGUI2WMPHSController(ProductService productService, StockService stockService, DestinationService destinationService, AppUserService userService,
                                    OrderService orderService, CreateOrderService createOrderService, CarrierService carrierService,
                                    LineService lineService, SingleThreadScheduler scheduler, AllocatedStockService allocatedStockService) {
        this.productService = productService;
        this.stockService = stockService;
        this.destinationService = destinationService;
        this.userService = userService;
        this.orderService = orderService;
        this.createOrderService = createOrderService;
        this.carrierService = carrierService;
        this.lineService = lineService;
        this.scheduler = scheduler;
        this.allocatedStockService = allocatedStockService;
    }

    //*************** line ************************
    @GetMapping("/getLines/{carrierId}")
    public List<LineDTO> getLines(@PathVariable int carrierId) {
        return carrierService.getCarrier(carrierId).getLines().stream().map(Line::toDTO).collect(Collectors.toList());
    }
    @DeleteMapping("/deleteLine/{lineId}")
    public SimpleResponse deleteLine(@PathVariable int lineId) throws ExecutionException, InterruptedException {
        logger.info("Proceeding DELETE request /deleteLine/{}", lineId);
        return scheduler.proceedRequestWithSingleResponse(()->allocatedStockService.removeLine(lineId));
    }
    //*************** /line ************************
    //*************** carrier ************************
    @DeleteMapping("/deleteCarrier/{carrierId}")
    public SimpleResponse deleteCarrier(@PathVariable int carrierId) throws ExecutionException, InterruptedException {
        logger.info("Proceeding DELETE request /deleteCarrier/{}", carrierId);
        return scheduler.proceedRequestWithSingleResponse(()->carrierService.removeCarrier(carrierId));
    }
    @GetMapping("/getCarriers/{orderId}")
    public List<CarrierDTO> getCarriers(@PathVariable int orderId) {
        return orderService.getOrderById(orderId).getCarriers().stream().map(Carrier::toDTO).toList();
    }
    //*************** /carrier ************************
    //*************** order ************************
    @GetMapping("/getOrders")
    public List<CompletationOrderDTO> getOrders(){
        return orderService.getCompletationOrders();
    }
    @PostMapping("/createOrder")
    public SimpleResponse createOrder(@RequestBody CreateOrderDTO createOrderDTO) throws JsonProcessingException, ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /createOrder");
        logger.info("{}", createOrderDTO);
        return scheduler.proceedRequestWithSingleResponse(()->createOrderService.createOrder(createOrderDTO));
    }
    @DeleteMapping("/deleteOrder/{orderId}")
    public SimpleResponse deleteOrder(@PathVariable int orderId) throws ExecutionException, InterruptedException {
        logger.info("Proceeding DELETE request /deleteOrder/{}", orderId);
        return scheduler.proceedRequestWithSingleResponse(()->orderService.removeOrder(orderId));
    }
    @PostMapping("/releaseOrder/{orderId}")
    public SimpleResponse releaseOrder(@PathVariable int orderId) throws ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /releaseOrder/{}", orderId);
        return scheduler.proceedRequestWithSingleResponse(()->orderService.releaseOrderToCompletation(orderId));
    }
    //*************** /order ************************
    //*************** stock ************************
    @PostMapping("/addStock")
    public SimpleResponse addStock(@RequestBody StockDTO stockDTO) throws JsonProcessingException, ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /addStock");
        logger.info("{}", stockDTO);
        return scheduler.proceedRequestWithSingleResponse(()->stockService.createStock(stockDTO));
    }
    @GetMapping("/getStocks")
    public List<StockDTO> getStocks(){
        return stockService.getStocks();
    }
    @GetMapping("/getCompressedStocks")
    public List<CompressedStockDTO> getCompressedStocks(){
        return stockService.getCompressedStocks();
    }
    @DeleteMapping("/deleteStock/{stockId}")
    public SimpleResponse deleteStock(@PathVariable int stockId) throws ExecutionException, InterruptedException {
        logger.info("Proceeding DELETE request /deleteStock/{}", stockId);
        return scheduler.proceedRequestWithSingleResponse(()->allocatedStockService.removeStock(stockId));
    }
    //*************** /stock ************************
    //*************** product ************************
    @DeleteMapping("/deleteProduct/{productId}")
    public SimpleResponse deleteProduct(@PathVariable int productId) throws ExecutionException, InterruptedException {
        logger.info("Proceeding DELETE request /deleteProduct/{}", productId);
        return scheduler.proceedRequestWithSingleResponse(()->productService.removeProduct(productId));
    }
    @PostMapping("/addProduct")
    public SimpleResponse addProduct(@RequestBody ProductDTO productDTO) throws JsonProcessingException, ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /addProduct");
        logger.info("{}", productDTO);
        return scheduler.proceedRequestWithSingleResponse(()->productService.addProduct(productDTO));
    }
    @GetMapping("/getProducts")
    public List<ProductDTO> getProducts(){
        return productService.getProducts();
    }
    //*************** /product ***********************
    //*************** destination ***********************
    @GetMapping("/getDestinations")
    public List<DestinationDTO> getDestinations(){
        return destinationService.getDestinations();
    }
    @PostMapping("/addDestination")
    public SimpleResponse addDestination(@RequestBody DestinationDTO destinationDTO) throws ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /addDestination");
        logger.info("{}", destinationDTO);
        return scheduler.proceedRequestWithSingleResponse(()->destinationService.addDestination(destinationDTO));
    }
    @DeleteMapping("/deleteDestination/{destinationId}")
    public SimpleResponse deleteDestination(@PathVariable int destinationId) throws ExecutionException, InterruptedException {
        logger.info("Proceeding DELETE request /deleteDestination/{}", destinationId);
        return scheduler.proceedRequestWithSingleResponse(()->destinationService.removeDestination(destinationId));
    }
    //*************** /destination ***********************
    //*************** user ***********************
    @GetMapping("/getUsers")
    public List<AppUserDTO> getUsers(){
        return userService.getAppUsers();
    }
    @PostMapping("/addUser")
    public SimpleResponse addUser(@RequestBody AppUserDTO appUserDTO) throws JsonProcessingException, ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /addUser");
        logger.info("{}", appUserDTO);
        return scheduler.proceedRequestWithSingleResponse(()->userService.addAppUser(appUserDTO));
    }
    @DeleteMapping("/deleteUser/{userId}")
    public SimpleResponse deleteUser(@PathVariable int userId) throws ExecutionException, InterruptedException {
        logger.info("Proceeding DELETE request /deleteUser/{}", userId);
        return scheduler.proceedRequestWithSingleResponse(()->userService.removeUser(userId));
    }
    //*************** /user ***********************

    @ExceptionHandler(ExecutionException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleExecutionException(ExecutionException e) {
        return "Wystąpił błąd podczas przetwarzania zadania: " + e.getMessage();
    }

    @ExceptionHandler(InterruptedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInterruptedException(InterruptedException e) {
        return "Zadanie zostało przerwane: " + e.getMessage();
    }
}
