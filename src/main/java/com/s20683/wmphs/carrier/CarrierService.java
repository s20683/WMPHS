package com.s20683.wmphs.carrier;

import com.s20683.wmphs.gui2wmphs.request.LineDTO;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.order.CompletationOrder;
import com.s20683.wmphs.order.OrderService;
import com.s20683.wmphs.tools.QueryTimer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;


@Service
public class CarrierService {
    protected final Logger logger = LoggerFactory.getLogger(CarrierService.class);
    private final CarrierRepository carrierRepository;
    private final Map<Integer, Carrier> carriers = new HashMap<>();

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
    @Transactional
    public String setCarrierBarcode(int carrierId, String barcode) {
        try {
            Carrier carrier = carriers.get(carrierId);
            if (carrier != null) {
                carrier.setBarcode(barcode);
                QueryTimer timer = new QueryTimer();
                carrierRepository.save(carrier);
                logger.info("Set barcode {} to carrier {}, executed in {}", barcode, carrier, timer);
                return "OK";
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
        logger.info("Carrier with id {} does not exist!", carrierId);
        return "Pojemnik o id " + carrierId + " nie istnieje!";
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
