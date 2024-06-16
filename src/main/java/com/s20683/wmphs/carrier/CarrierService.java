package com.s20683.wmphs.carrier;

import com.s20683.plc.Report;
import com.s20683.plc.tools.TrackId;
import com.s20683.wmphs.destination.Destination;
import com.s20683.wmphs.gui2wmphs.request.LineDTO;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.order.CompletationOrder;
import com.s20683.wmphs.order.OrderService;
import com.s20683.wmphs.order.OrderState;
import com.s20683.wmphs.tools.QueryTimer;
import com.s20683.wmphs.tools.Result;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Consumer;


@Service
public class CarrierService {
    protected final Logger logger = LoggerFactory.getLogger(CarrierService.class);
    private final CarrierRepository carrierRepository;
    private final Map<Integer, Carrier> carriers = new HashMap<>();
    private final Map<Short, Carrier> carrierByTID = new HashMap<>();

    @Autowired
    private final OrderService orderService;
    public CarrierService(CarrierRepository carrierRepository, OrderService orderService) {
        this.carrierRepository = carrierRepository;
        this.orderService = orderService;
    }

    @PostConstruct
    public void init(){
        QueryTimer timer = new QueryTimer();
        carrierRepository
                .findAll()
                .forEach(carrier -> {
                    logger.info("Received from database carrier {}", carrier);
                    carriers.put(carrier.getId(), carrier);
                    CompletationOrder order = orderService.getOrderById(carrier.getOrderId());
                    carrier.setCompletationOrder(order);
                    order.addCarrier(carrier);
                });
        logger.info("Find All operation for Carriers executed on {}", timer);
    }
    public Carrier getCarrierById(int carrierId) {
        return carriers.get(carrierId);
    }

    public Carrier getCarrier(int carrierId) {
        return carriers.get(carrierId);
    }
    public List<Carrier> getCarriers() {
        return carriers.values().stream().toList();
    }


    public void getCarrierDestination(TrackId trackId, String barcode, Consumer<Result<Destination>> proceeder) {
        Optional<Carrier> carrierFromMap = carriers.values().stream().filter(e->e.getBarcode().equals(barcode)).findFirst();
        if (carrierFromMap.isEmpty()) {
            logger.info("Carrier with barcode {} does not exist! => REJECT", barcode);
            proceeder.accept(Result.createFail(new NoSuchElementException("Carrier does not exist!")));
            return;
        }
        Carrier carrier = carrierFromMap.get();
        CompletationOrder order = carrier.getCompletationOrder();
        if (order == null) {
            logger.info("Order with carrier {} does not exist! => REJECT", barcode);
            proceeder.accept(Result.createFail(new NoSuchElementException("Carrier has no order!")));
            return;
        }
        if (order.getState() == OrderState.SORTED.getValue()) {
            logger.info("All carriers of this order are sorted {} => REJECT", order);
            proceeder.accept(Result.createFail(new IllegalStateException("All carriers of this order is sorted")));
            return;
        }
        if (order.getState() != OrderState.COMPLETED.getValue()) {
            logger.info("Order is not completed {} => REJECT", order);
            proceeder.accept(Result.createFail(new IllegalStateException("Order is not completed")));
            return;
        }
        Destination destination = order.getDestination();
        if (destination == null) {
            logger.info("Carrier {} has no destination! => REJECT", barcode);
            proceeder.accept(Result.createFail(new NoSuchElementException("Carrier has no destination")));
            return;
        }
        logger.info("Carrier {} => OK [{}]", barcode, destination.getTarget());
        carrierByTID.put(trackId.getTrackId(), carrier);
        proceeder.accept(Result.createSuccess(destination));
    }
    @Transactional
    public void reportCarrier(Report report) {
        Carrier carrier = carrierByTID.get(report.getReportTrackId().getTrackId());
        if (carrier == null) {
            logger.info("Carrier with tid {} does not exist!", report.getReportTrackId().getTrackId());
            return;
        }
        if (carrier.getCompletationOrder().getDestination().getTarget() == report.getReport()) {
            logger.info("Reported target is OK!");
            carrier.setSorted(true);
            carrierByTID.remove(report.getReportTrackId().getTrackId());
            carrierRepository.save(carrier);
            orderService.checkCarriersSorted(carrier.getCompletationOrder());
        } else {
            logger.info("Wrong report target: reported {} != required {}", report.getReport(), carrier.getCompletationOrder().getDestination().getTarget());
        }
    }

    @Transactional
    public void removeCarriers(List<Carrier> carriersRemove) {
        List<Carrier> carriersToRemove = new ArrayList<>();
        for (Carrier carrier : carriers.values()) {
            for (Carrier carrierToRemove : carriersRemove) {
                if (carrier.getId().equals(carrierToRemove.getId())) {
                    carriersToRemove.add(carrier);
                }
            }
        }
        for (Carrier carrier : carriersToRemove) {
            removeCarrier(carrier.getId());
        }
    }
    @Transactional
    public String removeCarrier(int carrierId) {
        Carrier carrier = carriers.get(carrierId);
        if (carrier != null) {

            if (!carrier.getLines().isEmpty()) {
                logger.info("Cannot remove carrier {} with lines!", carrier);
                return "Nie można usunąć pojemnika " + carrier.getBarcode() + " ponieważ posiada linię kompletacyjną!";
            }
            try {
                QueryTimer timer = new QueryTimer();
                carrierRepository.deleteById(carrierId);
                CompletationOrder order = carrier.getCompletationOrder();
                order.removeCarrier(carrier);
                carriers.remove(carrier.getId());
                logger.info("Removed carrier {} from database, executed {}", carrierId, timer);
                return "OK";
            } catch (Exception e) {
                logger.error("Failed to delete carrier: {}", e.getMessage());
                return "Błąd podczas usuwania pojemnika: " + e.getMessage();
            }
        }
        logger.info("Carrier with id {} does not exist, cannot delete from database", carrierId);
        return "Pojemnik o podanym id " + carrierId + " nie istnieje!";
    }

    public List<LineDTO> getPriorityLineForCarriers(List<Integer> carriersID) {
        List<Carrier> carriersResult = carriersID.stream()
                .filter(carriers::containsKey)
                .map(carriers::get)
                .toList();

        if (carriersResult.isEmpty()) {
            logger.info("Carriers with ids {} does not exist", carriersID);
            return new ArrayList<>();
        }
        List<Line> allLines = carriersResult.stream()
                .flatMap(carrier -> carrier.getLines().stream())
                .filter(line -> line.getQuantityToComplete() != 0)
                .toList();

        if (allLines.isEmpty()) {
            logger.info("No lines found for carriers with ids {}", carriersID);
            return new ArrayList<>();
        }
        String firstLocation = allLines.stream()
                .map(line -> line.getProduct().getLocation())
                .min(String::compareTo)
                .orElseThrow(() -> new RuntimeException("Unexpected empty stream"));

        return allLines.stream()
                .filter(line -> firstLocation.equals(line.getProduct().getLocation()))
                .map(Line::toDTO)
                .toList();
    }

    public void addCarrierToMap(Carrier carrier) {
        if (carrier != null)
            carriers.put(carrier.getId(), carrier);
    }
}
