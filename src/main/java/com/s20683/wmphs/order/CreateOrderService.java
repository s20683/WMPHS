package com.s20683.wmphs.order;

import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.carrier.CarrierPacker;
import com.s20683.wmphs.carrier.CarrierRepository;
import com.s20683.wmphs.carrier.CarrierService;
import com.s20683.wmphs.destination.Destination;
import com.s20683.wmphs.destination.DestinationService;
import com.s20683.wmphs.gui2wmphs.request.CompressedStockDTO;
import com.s20683.wmphs.gui2wmphs.request.CreateOrderDTO;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.line.LineRepository;
import com.s20683.wmphs.line.LineService;
import com.s20683.wmphs.product.Product;
import com.s20683.wmphs.product.ProductService;
import com.s20683.wmphs.stock.AllocatedStockService;
import com.s20683.wmphs.stock.StockService;
import com.s20683.wmphs.tools.QueryTimer;
import com.s20683.wmphs.user.AppUser;
import com.s20683.wmphs.user.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CreateOrderService {
    protected final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private final DestinationService destinationService;
    @Autowired
    private final AppUserService appUserService;
    @Autowired
    private final ProductService productService;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final CarrierRepository carrierRepository;
    @Autowired
    private final LineRepository lineRepository;
    @Autowired
    private final StockService stockService;
    @Autowired
    private final OrderService orderService;
    @Autowired
    private final CarrierService carrierService;
    private final LineService lineService;
    private final CarrierPacker carrierPacker = new CarrierPacker();
    private final AllocatedStockService allocatedStockService;


    public CreateOrderService(DestinationService destinationService, AppUserService appUserService, ProductService productService,
                              OrderRepository orderRepository, CarrierRepository carrierRepository, LineRepository lineRepository,
                              OrderService orderService, CarrierService carrierService, LineService lineService, StockService stockService,
                              AllocatedStockService allocatedStockService) {
        this.destinationService = destinationService;
        this.appUserService = appUserService;
        this.productService = productService;
        this.orderRepository = orderRepository;
        this.carrierRepository = carrierRepository;
        this.lineRepository = lineRepository;
        this.orderService = orderService;
        this.carrierService = carrierService;
        this.lineService = lineService;
        this.stockService = stockService;
        this.allocatedStockService = allocatedStockService;
    }

    @Transactional
    public String createOrder(CreateOrderDTO createOrderDTO) throws RuntimeException{
        try {
            Destination destination = destinationService.getDestinationById(createOrderDTO.getDestinationId());
            if (destination == null) {
                logger.info("Cannot create order with null destination!");
                throw new RuntimeException("Destynacja o podanym id " + createOrderDTO.getDestinationId() + " nie istnieje!");
            }
            AppUser user = appUserService.getAppUserById(createOrderDTO.getUserId());
            if (user == null) {
                logger.info("Cannot create order with null user!");
                throw new RuntimeException("Użytkownik o podanym id " + createOrderDTO.getUserId() + " nie istnieje!");
            }
            List<CompressedStockDTO> lines = createOrderDTO.getLines();
            Map<Product, Integer> productsToPlan = new HashMap<>();
            for (CompressedStockDTO compressedStock : lines) {
                Product product = productService.getProduct(compressedStock.getProductId());
                if (product == null) {
                    logger.info("Error while planning, product with id {} does not exist", compressedStock.getProductId());
                    throw new RuntimeException("Podczas planowania wystąpił błąd, produkt z id " + compressedStock.getProductId() + " nie istnieje!");
                }
                productsToPlan.put(product, compressedStock.getQuantity());
            }
            CompletationOrder order = new CompletationOrder(
                    createOrderDTO.getCarrierVolume(),
                    OrderState.INIT.getValue(),
                    destination,
                    user
            );
            List<Carrier> carriers = carrierPacker.packProducts(productsToPlan, order);
            order.setCarriers(carriers);

            return saveData(order);
        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public String saveData(CompletationOrder order) throws Exception {
        QueryTimer timer = new QueryTimer();
        List<Carrier> savedCarries = new ArrayList<>();
        List<Line> allSavedLines = new ArrayList<>();
        CompletationOrder orderFromDB = orderRepository.save(order);
        order.setId(orderFromDB.getId());
        if (order.getId() != null) {
            for (Carrier carrier : order.getCarriers()) {
                carrier.setCompletationOrder(order);
                Carrier carrierFromDB = carrierRepository.save(carrier);
                carrier.setId(carrierFromDB.getId());
                if (carrier.getId() != null) {
                    savedCarries.add(carrier);
                    List<Line> savedLines = new ArrayList<>();
                    for (Line line : carrier.getLines()) {
                        line.setCarrier(carrier);
                        Line lineFromDB = lineRepository.save(line);
                        line.setId(lineFromDB.getId());
                        if (line.getId() != null) {
                            allocatedStockService.allocateStock(line);
                            savedLines.add(line);
                            allSavedLines.add(line);
                        } else {
                            logger.info("Error while saving line {} to database", line);
                            throw new RuntimeException("Powstał błąd podczas tworzenia zlecenia, który spowodowała linia " + line);
                        }
                    }
                    carrier.setLines(savedLines);
                } else {
                    logger.info("Error while saving carrier {} to database", carrier);
                    throw new RuntimeException("Powstał błąd podczas tworzenia zlecenia, który spowodował pojemnik " + carrier);
                }
            }
            order.setCarriers(savedCarries);
            logger.info("Order {} saved to database, executed in {}", order, timer);

            orderService.addOrderToMap(order);
            for (Carrier carrier : savedCarries) {
                carrierService.addCarrierToMap(carrier);
            }
            for (Line line : allSavedLines) {
                lineService.addLineToMap(line);
            }
            return "OK";
        } else {
            logger.warn("Error while saving order {} to database", order);
            throw new RuntimeException("Zlecenie nie istnieje ale podczas dodawnia do bazy powstał błąd!");
        }
    }
}
