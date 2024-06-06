package com.s20683.wmphs.carrier;

import com.s20683.wmphs.gui2wmphs.request.CarrierDTO;
import com.s20683.wmphs.gui2wmphs.request.LineDTO;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.tools.QueryTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarrierService {
    protected final Logger logger = LoggerFactory.getLogger(CarrierService.class);
    private final CarrierRepository carrierRepository;
//    private final Map<Integer, Carrier> carriers = new HashMap<>();

    public CarrierService(CarrierRepository carrierRepository) {
        this.carrierRepository = carrierRepository;
    }

//    @PostConstruct
//    public void init(){
//        QueryTimer timer = new QueryTimer();
//        carrierRepository
//                .findAll()
//                .forEach(carrier -> {
//                    logger.info("Received from database carrier {}", carrier);
//                    carriers.put(carrier.getId(), carrier);
//                });
//        logger.info("Find All operation for Carriers executed on {}", timer);
//    }
    public void addCarrierToMap(Carrier carrier) {
//        if (carrier != null && carrier.getId() != null) {
//            carriers.put(carrier.getId(), carrier);
//        }
    }

//    public String addCarrier(CarrierDTO carrierDTO) {
//        Carrier carrier = carriers.get(carrierDTO.getId());
//        CompletationOrder order = orderService.getOrder(carrierDTO.getOrderId());
//        if (order == null) {
//            logger.info("Cannot create carrier with null order!");
//            return "Zlecenie o podanym id " + carrierDTO.getOrderId() + " nie istnieje!";
//        }
//        if (carrier == null) {
//            QueryTimer timer = new QueryTimer();
//            carrier = carrierRepository.save(
//                    new Carrier(
//                            carrierDTO.getBarcode(),
//                            carrierDTO.getVolume(),
//                            order
//                    ));
//            if (carrier.getId() != null) {
//                logger.info("Carrier {} saved to database, executed in {}", carrier, timer);
//                carriers.put(carrier.getId(), carrier);
//                return "OK";
//            } else {
//                logger.warn("Error while saving carrier {} to database", carrierDTO);
//                return "Pojemnik kompletacyjny nie istnieje ale powstał problem podczas zapisu do bazy.";
//            }
//        } else {
//            QueryTimer timer = new QueryTimer();
//            carrier.setBarcode(carrierDTO.getBarcode());
//            carrier.setVolume(carrierDTO.getVolume());
//            carrier.setCompletationOrder(order);
//            logger.info("Carrier {} updated on database, executed in {}", carrier, timer);
//            carriers.put(carrier.getId(), carrier);
//            return "OK";
//        }
//    }

    public List<CarrierDTO> getCarriers() {
        return carrierRepository.findAll().stream().map(Carrier::toDTO).collect(Collectors.toList());
    }
    public Carrier getCarrier(int carrierId) {
        return carrierRepository.findById(carrierId).orElse(null);
    }
    @Transactional
    public String setCarrierBarcode(int carrierId, String barcode) {
        try {
            Optional<Carrier> carrierFromDB = carrierRepository.findById(carrierId);
            if (carrierFromDB.isPresent()) {
                Carrier carrier = carrierFromDB.get();
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
//        Carrier carrier = carriers.get(carrierId);
//        if (carrier != null) {
        //todo
//        if (!carrier.getLines().isEmpty()) {
//            logger.info("Cannot remove carrier {} with lines!", carrier);
//            return "Nie można usunąć pojemnika " + carrier.getBarcode() + " ponieważ posiada linię kompletacyjną!";
//        }
        try {
            QueryTimer timer = new QueryTimer();
            carrierRepository.deleteById(carrierId);
            logger.info("Removed carrier {} from database, executed {}", carrierId, timer);
//            carriers.remove(carrier.getId());
            return "OK";
        } catch (Exception e) {
            logger.error("Failed to delete carrier: {}", e.getMessage());
            return "Błąd podczas usuwania pojemnika: " + e.getMessage();
        }
//        }
//        logger.info("Carrier with id {} does not exist, cannot delete from database", carrierId);
//        return "Pojemnik o podanym id " + carrierId + " nie istnieje!";
    }
    public List<LineDTO> getPriorityLineForCarriers(List<Integer> carriersID) {
        List<Carrier> carriers = carrierRepository.findAllById(carriersID);
        if (carriers.isEmpty()) {
            logger.info("Carriers with ids {} does not exist", carriersID);
            return new ArrayList<>();
        }
        List<Line> allLines = carriers.stream()
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
}
