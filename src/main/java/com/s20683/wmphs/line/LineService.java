package com.s20683.wmphs.line;

import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.carrier.CarrierService;
import com.s20683.wmphs.product.Product;
import com.s20683.wmphs.product.ProductService;
import com.s20683.wmphs.tools.QueryTimer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LineService {
    protected final Logger logger = LoggerFactory.getLogger(LineService.class);
    private final LineRepository lineRepository;
    private final Map<Integer, Line> lines = new HashMap<>();
    @Autowired
    private final CarrierService carrierService;
    @Autowired
    private final ProductService productService;

    public LineService(LineRepository lineRepository, CarrierService carrierService, ProductService productService) {
        this.lineRepository = lineRepository;
        this.carrierService = carrierService;
        this.productService = productService;
    }

    @PostConstruct
    public void init(){
        QueryTimer timer = new QueryTimer();
        lineRepository
                .findAll()
                .forEach(line -> {
                    logger.info("Received from database line {}", line);
                    lines.put(line.getId(), line);
                    Carrier carrier = carrierService.getCarrierById(line.getCarrierId());
                    carrier.addLine(line);
                    line.setCarrier(carrier);
                    Product product = productService.getProductById(line.getProductId());
                    line.setProduct(product);
                });
        logger.info("Find All operation for Lines executed on {}", timer);
    }

    public Line getLineById(int id) {
        return lines.get(id);
    }
    public String deleteLine(int id) {
        Line lineToRemove = lines.get(id);
        if (lineToRemove == null) {
            logger.info("Cannot remove line with id {}, does not exist", id);
            return "Linia z id " + id + " nie istnieje";
        }
        QueryTimer timer = new QueryTimer();
        try {
            lineRepository.deleteById(id);
            lines.remove(lineToRemove.getId());
            lineToRemove.getCarrier().removeLine(lineToRemove);
            logger.info("Line {} removed from database, executed {}", id, timer);
            return "OK";
        } catch (Exception exception) {
            logger.warn("Exception while removing line {}", id, exception);
            return exception.getMessage();
        }
    }
    public void updateLineOnDB(Line line) {
        lineRepository.save(line);
    }

    public void addLineToMap(Line line) {
        if (line != null)
            lines.put(line.getId(), line);
    }

//    @Transactional
//    public String removeLine(int id) {
//        Line line = lines.get(id);
//        if (line != null) {
//            try {
//                QueryTimer timer = new QueryTimer();
//
//                stockService.unallocateStock(line);
//                lineRepository.deleteById(id);
//                logger.info("Removed line and updated stock {} on database, executed {}", id, timer);
////            line.getCarrier().removeLine(line);
////            lines.remove(line.getId());
//                return "OK";
//
//
//            } catch (Exception exception) {
//                logger.warn("Exception while remove line {}", id, exception);
//                return "Błąd podczas usuwania linii: " + exception.getMessage();
//            }
//        }
//        logger.info("Line with id {} does not exist, cannot delete from database", id);
//        return "Linia o podanym id " + id +  " nie istnieje!";
//    }

//    public String addLine(LineDTO lineDTO) {
//        Line line = lines.get(lineDTO.getId());
//        Product product = productService.getProduct(lineDTO.getProductId());
//        if (product == null) {
//            logger.info("Cannot create line with null product!");
//            return "Produkt o podanym id " + lineDTO.getProductId() + " nie istnieje!";
//        }
//        Carrier carrier = carrierService.getCarrier(lineDTO.getCarrierId());
//        if (carrier == null) {
//            logger.info("Cannot create line with null carrier!");
//            return "Pojemnik o podanym id " + lineDTO.getCarrierId() + " nie istnieje!";
//        }
//
//        if (line == null) {
//            QueryTimer timer = new QueryTimer();
//            line = lineRepository.save(
//                    new Line(lineDTO.getQuantity(),
//                            lineDTO.getQuantityCompleted(),
//                            product,
//                            carrier
//                    ));
//            if (line.getId() != null) {
//                logger.info("Line {} saved to database, executed in {}", line, timer);
//                lines.put(line.getId(), line);
//                return "OK";
//            } else {
//                logger.warn("Error while saving line {} to database", lineDTO);
//                return "Linia kompletacyja nie istnieje ale powstał problem podczas zapisu do bazy.";
//            }
//        } else {
//            QueryTimer timer = new QueryTimer();
//            line.setQuantity(lineDTO.getQuantity());
//            line.setQuantityCompleted(lineDTO.getQuantityCompleted());
//            line.setCarrier(carrier);
//            line.setProduct(product);
//            logger.info("Line {} updated on database, executed in {}", line, timer);
//            lines.put(line.getId(), line);
//            return "OK";
//        }
//    }




}
