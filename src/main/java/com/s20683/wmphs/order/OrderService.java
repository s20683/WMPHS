package com.s20683.wmphs.order;

import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.destination.Destination;
import com.s20683.wmphs.destination.DestinationService;
import com.s20683.wmphs.gui2wmphs.request.CarrierDTO;
import com.s20683.wmphs.gui2wmphs.request.CompletationOrderDTO;
import com.s20683.wmphs.gui2wmphs.request.CompleteLineDTO;
import com.s20683.wmphs.tools.QueryTimer;
import com.s20683.wmphs.user.AppUser;
import com.s20683.wmphs.user.AppUserService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
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

    private final Map<Integer, CompletationOrder> orders = new HashMap<>();

    public OrderService(OrderRepository orderRepository, DestinationService destinationService, AppUserService appUserService) {
        this.orderRepository = orderRepository;
        this.destinationService = destinationService;
        this.appUserService = appUserService;
    }

    @PostConstruct
    public void init(){
        QueryTimer timer = new QueryTimer();
        orderRepository
                .findAll()
                .forEach(order -> {
                    logger.info("Received from database order {}", order);
                    orders.put(order.getId(), order);
                    Destination destination = destinationService.getDestinationById(order.getDestinationId());
                    order.setDestination(destination);
                    AppUser user = appUserService.getAppUserById(order.getUserId());
                    order.setUser(user);
                });
        logger.info("Find All operation for Orders executed on {}", timer);
    }

    public CompletationOrder getOrderById(int id) {
        return orders.get(id);
    }
    @Transactional
    public List<CarrierDTO> getCarriersToCompletation(int orderId) {
        CompletationOrder order = orders.get(orderId);
        if (order == null) {
            logger.info("Order with id {} does not exist", orderId);
            return new ArrayList<>();
        }
        List<Carrier> carriers = order.getCarriers();
        logger.info("Received carriers {}", carriers);
        carriers = carriers.stream()
                .filter(carrier -> carrier.getLines().stream()
                        .anyMatch(line -> line.getQuantityToComplete() != 0))
                .sorted(Comparator.comparing(Carrier::getId))
                .limit(6)
                .collect(Collectors.toList());
        logger.info("Filtered carriers {}", carriers);

        return carriers.stream().map(Carrier::toDTO).toList();
    }
    @Transactional
    public CompletationOrderDTO getReleasedOrderForUser(int userId) {
        try {
            CompletationOrder releasedOrder = null;

            Optional<CompletationOrder> inProgressOrderFromCache = orders.values().stream()
                .filter(order -> order.getUser().getId() == userId)
                    .filter(order -> order.getState() == OrderState.COMPLETATION.getValue())
                            .findFirst();

            if (inProgressOrderFromCache.isPresent()) {
                releasedOrder = inProgressOrderFromCache.get();
                return releasedOrder.toDTO();
            } else {
                Optional<CompletationOrder> releasedOrderFromCache = orders.values().stream()
                        .filter(order -> order.getUser().getId() == userId)
                        .filter(order -> order.getState() == OrderState.RELEASED.getValue())
                        .findFirst();
                if (releasedOrderFromCache.isEmpty()) {
                    return null;
                }

                releasedOrder = releasedOrderFromCache.get();
                releasedOrder.setState(OrderState.COMPLETATION.getValue());
                orderRepository.save(releasedOrder);
                return releasedOrder.toDTO();
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
    public String releaseOrderToCompletation(int orderId) {
        CompletationOrder order = orders.get(orderId);
        if (order != null) {
            if (order.getCarriers().isEmpty()) {
                logger.info("Cannot release order {} with empty carriers!", order);
                return "Nie można uwolnić zlecenia bez pojemników!";
            }
            for (Carrier carrier : order.getCarriers()) {
                if (carrier.getLines().isEmpty()) {
                    logger.info("Cannot release order {} with empty lines for carrier {}", order, carrier);
                    return "Nie można uwolnić zlecenia z pusto zaplanowanym pojemnikiem " + carrier.getId();
                }
            }
            if (order.getState() != OrderState.INIT.getValue()) {
                logger.info("Cannot release order {} with state {}", order, order.getState());
                return "Nie można uwolnić zlecenia w stanie " + order.getState();
            }
            order.setState(OrderState.RELEASED.getValue());
            orderRepository.save(order);
            logger.info("Order {} released successfully!", order);
            return "Zlecenie uwolniono do kompletacji!";
        }
        return "Zlecenie z id " + orderId + " nie istnieje!";
    }
    public List<CompletationOrderDTO> getCompletationOrders(){
        return orders.values().stream().map(CompletationOrder::toDTO).collect(Collectors.toList());
    }
    public void checkOrderCompleted(CompleteLineDTO completeLineDTO) throws RuntimeException{
        CompletationOrder order = orders.get(completeLineDTO.getOrderId());

        if (order == null) {
            logger.info("Cannot complete order {}, does not exist", completeLineDTO.getOrderId());
            throw new RuntimeException("Order not exist");
        }
        boolean allLinesComplete = order.getCarriers().stream()
                .flatMap(carrier -> carrier.getLines().stream())
                .allMatch(line -> line.getQuantityToComplete() == 0);

        if (allLinesComplete) {
            order.setState(OrderState.COMPLETED.getValue());
            orderRepository.save(order);
            logger.info("Order {} completed database", order);
        } else {
            logger.info("Order has incompleted lines!");
        }
    }

    public String addOrder(CompletationOrderDTO completationOrderDTO) {
        Optional<CompletationOrder> order = orderRepository.findById(completationOrderDTO.getId());
        Destination destination = destinationService.getDestinationById(completationOrderDTO.getDestinationId());
        if (destination == null) {
            logger.info("Cannot create order with null destination!");
            return "Destynacja o podanym id " + completationOrderDTO.getDestinationId() + " nie istnieje!";
        }
        AppUser user = appUserService.getAppUserById(completationOrderDTO.getUserId());
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
        CompletationOrder orderToRemove = orders.get(id);
        if (orderToRemove == null) {
            logger.info("Cannot remove order with id {}, does not exist", id);
            return "Zlecenie z id " + id + " nie istnieje";
        }
        if (!orderToRemove.getCarriers().isEmpty()) {
            logger.info("Cannot remove order {} with carriers!", orderToRemove);
            return "Zlecenie posiada zaplanowane pojemniki!";
        }
        QueryTimer timer = new QueryTimer();
        try {
            orderRepository.deleteById(id);
            orders.remove(orderToRemove.getId());
            logger.info("Order {} removed from database, executed {}", id, timer);
            return "OK";
        } catch (Exception exception) {
            logger.warn("Exception while remove order {}", id, exception);
            return "Błąd podczas usuwania zlecenia: " + exception.getMessage();
        }
    }

    public void addOrderToMap(CompletationOrder order) {
        if (order != null && order.getId() != null) {
            orders.put(order.getId(), order);
        }
    }
}
