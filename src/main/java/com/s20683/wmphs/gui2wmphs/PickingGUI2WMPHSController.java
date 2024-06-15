package com.s20683.wmphs.gui2wmphs;

import com.s20683.wmphs.carrier.CarrierService;
import com.s20683.wmphs.gui2wmphs.request.*;
import com.s20683.wmphs.line.LineService;
import com.s20683.wmphs.order.CompletedOrderService;
import com.s20683.wmphs.order.CreateOrderService;
import com.s20683.wmphs.order.OrderService;
import com.s20683.wmphs.scheduler.SingleThreadScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/picking2wmphs")
public class PickingGUI2WMPHSController {
    protected final Logger logger = LoggerFactory.getLogger(PickingGUI2WMPHSController.class);


    @Autowired
    private final OrderService orderService;
    @Autowired
    private final CarrierService carrierService;
    @Autowired
    private final LineService lineService;
    @Autowired
    private final CompletedOrderService completedOrderService;
    @Autowired
    private final SingleThreadScheduler scheduler;
    @Autowired
    private final CreateOrderService createOrderService;

    public PickingGUI2WMPHSController(OrderService orderService, CarrierService carrierService, LineService lineService,
                                      CompletedOrderService completedOrderService, SingleThreadScheduler scheduler,
                                      CreateOrderService createOrderService) {
        this.orderService = orderService;
        this.carrierService = carrierService;
        this.lineService = lineService;
        this.completedOrderService = completedOrderService;
        this.scheduler = scheduler;
        this.createOrderService = createOrderService;
    }

    @GetMapping("/getReleasedOrder/{userId}")
    public CompletationOrderDTO getReleasedOrder(@PathVariable int userId) throws ExecutionException, InterruptedException {
        return scheduler.submitTask(()->orderService.getReleasedOrderForUser(userId)).get();
    }

    @GetMapping("/getCarriersToCompletation/{orderId}")
    public List<CarrierDTO> getCarriers(@PathVariable int orderId) throws ExecutionException, InterruptedException {
        return scheduler.submitTask(()->orderService.getCarriersToCompletation(orderId)).get();
    }
    @PostMapping("/setCarrierBarcode/{carrierId}/{barcode}")
    public SimpleResponse setCarrierBarcode(@PathVariable int carrierId, @PathVariable String barcode) throws ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /setCarrierBarcode/{}/{}", carrierId, barcode);
        return scheduler.proceedRequestWithSingleResponse(()->createOrderService.setCarrierBarcode(carrierId, barcode));
    }
    @PostMapping("/getLineForCarriers")
    public List<LineDTO> getLineForCarriers(@RequestBody CarriersIdDTO carriersIdDTO) throws ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /getLineForCarriers {}", carriersIdDTO);
        return scheduler.submitTask(()->carrierService.getPriorityLineForCarriers(carriersIdDTO.getCarriersId())).get();
    }
    @PostMapping("/completeLine")
    public SimpleResponse completeLine(@RequestBody CompleteLineDTO completeLineDTO) throws ExecutionException, InterruptedException {
        logger.info("Proceeding POST request /completeLine");
        logger.info("{}", completeLineDTO);
        return scheduler.proceedRequestWithSingleResponse(()->completedOrderService.completeLine(completeLineDTO));
    }

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
