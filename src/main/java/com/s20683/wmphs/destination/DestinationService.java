package com.s20683.wmphs.destination;

import com.s20683.wmphs.gui2wmphs.request.DestinationDTO;
import com.s20683.wmphs.product.Product;
import com.s20683.wmphs.tools.QueryTimer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DestinationService {
    protected final Logger logger = LoggerFactory.getLogger(DestinationService.class);
    @Autowired
    private final DestinationRepository destinationRepository;
    private Map<Integer, Destination> destinations = new HashMap<>();

    public DestinationService(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    @PostConstruct
    public void init(){
        QueryTimer timer = new QueryTimer();
        destinationRepository
                .findAll()
                .forEach(destination -> {
                    logger.info("Received from database destination {}", destination);
                    destinations.put(destination.getId(), destination);
                });
        logger.info("Find All operation for Destinations executed on {}", timer);
    }

    public String addDestination(DestinationDTO destinationDTO) {
        Destination destination = destinations.get(destinationDTO.getId());
        if (destination == null) {
            QueryTimer timer = new QueryTimer();
            destination = destinationRepository.save(
                    new Destination(
                            destinationDTO.getName(),
                            destinationDTO.getAddress(),
                            destinationDTO.getTarget()
                    )
            );
            if (destination.getId() != null) {
                logger.info("Destination {} saved to database, executed in {}", destination, timer);
                destinations.put(destination.getId(), destination);
                return "OK";
            } else {
                logger.warn("Error while saving destination {} to database", destinationDTO);
                return "Destynacja nie istnieje ale powstał problem podczas zapisu do bazy.";
            }
        } else {
            QueryTimer timer = new QueryTimer();
            destination.setName(destinationDTO.getName());
            destination.setAddress(destinationDTO.getAddress());
            destination.setTarget(destinationDTO.getTarget());
            logger.info("Destination {} updated on database, executed in {}", destination, timer);
            destinations.put(destination.getId(), destination);
            return "OK";
        }
    }
    public String removeDestination(int id) {
        Destination destinationToRemove = destinations.get(id);
        if (destinationToRemove == null) {
            logger.info("Cannot remove destination with id {}, does not exist", id);
            return "Destynacja z id " + id + " nie istnieje";
        }
        QueryTimer timer = new QueryTimer();
        try {
            destinationRepository.delete(destinationToRemove);
            destinations.remove(destinationToRemove.getId());
            logger.info("Destination {} removed from database, executed {}", destinationToRemove, timer);
            return "OK";
        } catch (Exception exception) {
            logger.warn("Exception while remove destination {}", destinationToRemove, exception);
            return exception.getMessage();
        }
    }
    public List<DestinationDTO> getDestinations() {
        return destinations.values().stream().map(Destination::toDTO).collect(Collectors.toList());
    }
    public Destination getDestination(int destinationId){
        return destinations.get(destinationId);
    }

}
