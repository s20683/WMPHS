package com.s20683.wmphs.order;

import com.s20683.wmphs.carrier.CarrierService;
import com.s20683.wmphs.destination.Destination;
import com.s20683.wmphs.destination.DestinationService;
import com.s20683.wmphs.gui2wmphs.request.CompletationOrderDTO;
import com.s20683.wmphs.line.LineService;
import com.s20683.wmphs.tools.QueryTimer;
import com.s20683.wmphs.user.AppUser;
import com.s20683.wmphs.user.AppUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {
    protected final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final DestinationService destinationService;
    @Autowired
    private final AppUserService appUserService;
    @Autowired
    private final CarrierService carrierService;
    @Autowired
    private final LineService lineService;
//    private final Map<Integer, CompletationOrder> orders = new HashMap<>();

    public OrderService(OrderRepository orderRepository, DestinationService destinationService, AppUserService appUserService,
                        CarrierService carrierService, LineService lineService) {
        this.orderRepository = orderRepository;
        this.destinationService = destinationService;
        this.appUserService = appUserService;
        this.carrierService = carrierService;
        this.lineService = lineService;
    }

//    @PostConstruct
//    public void init(){
//        QueryTimer timer = new QueryTimer();
//        orderRepository
//                .findAll()
//                .forEach(order -> {
//                    logger.info("Received from database order {}", order);
////                    orders.put(order.getId(), order);
//                    order.getCarriers()
//                            .forEach(carrier -> {
//                                logger.info("Received from database carrier {}", carrier);
//                                carrierService.addCarrierToMap(carrier);
//                                carrier.getLines().forEach(line -> {
//                                    logger.info("Received from database line {}", line);
//                                    lineService.addLineToMap(line);
//                                });
//                            });
//                });
//        logger.info("Find All operation for Orders executed on {}", timer);
//    }
    public CompletationOrder getOrder(int orderId) {
        Optional<CompletationOrder> order = orderRepository.findById(orderId);
        return order.orElse(null);
    }
    public List<CompletationOrderDTO> getCompletationOrders(){
        return orderRepository.findAll().stream().map(CompletationOrder::toDTO).collect(Collectors.toList());
    }

    public String addOrder(CompletationOrderDTO completationOrderDTO) {
        Optional<CompletationOrder> order = orderRepository.findById(completationOrderDTO.getId());
        Destination destination = destinationService.getDestination(completationOrderDTO.getDestinationId());
        if (destination == null) {
            logger.info("Cannot create order with null destination!");
            return "Destynacja o podanym id " + completationOrderDTO.getDestinationId() + " nie istnieje!";
        }
        AppUser user = appUserService.getAppUser(completationOrderDTO.getUserId());
        if (user == null) {
            logger.info("Cannot create order with null user!");
            return "Użytkownik o podanym id " + completationOrderDTO.getUserId() + " nie istnieje!";
        }

//        if (order.isPresent()) {
        QueryTimer timer = new QueryTimer();
        CompletationOrder orderFromDb = orderRepository.save(
                new CompletationOrder(
                        completationOrderDTO.getCarrierVolume(),
                        completationOrderDTO.getState(),
                        destination,
                        user
                )
        );
        if (order.isEmpty()) {
            if (orderFromDb.getId() != null) {
                logger.info("Order {} saved to database, executed in {}", order, timer);
            } else {
                logger.warn("Error while saving order {} to database", completationOrderDTO);
                return "Zlecenie nie istnieje ale podczas dodawnia do bazy powstał błąd!";
            }
//                orders.put(order.getId(), order);
            return "OK";
        } else {
            logger.info("Order {} updated on database, executed in {}", order, timer);
            return "OK";
        }
//        } else {
//            QueryTimer timer = new QueryTimer();
//            order.setCarrierVolume(completationOrderDTO.getCarrierVolume());
//            order.setState(completationOrderDTO.getState());
//            order.setDestination(destination);
//            order.setUser(user);
//            logger.info("Order {} updated on database, executed in {}", order, timer);
//            orders.put(order.getId(), order);
//            return "OK";
//        }
    }

    public String removeOrder(int id) {
//        CompletationOrder orderToRemove = orders.get(id);
//        if (orderToRemove == null) {
//            logger.info("Cannot remove order with id {}, does not exist", id);
//            return "Zlecenie z id " + id + " nie istnieje";
//        }
        QueryTimer timer = new QueryTimer();
        try {
            orderRepository.deleteById(id);
//            orders.remove(orderToRemove.getId());
            logger.info("Order {} removed from database, executed {}", id, timer);
            return "OK";
        } catch (Exception exception) {
            logger.warn("Exception while remove order {}", id, exception);
            return "Błąd podczas usuwania zlecenia: " + exception.getMessage();
        }
    }

    public void addOrderToMap(CompletationOrder order) {
//        if (order != null && order.getId() != null) {
//            orders.put(order.getId(), order);
//        }
    }
}
