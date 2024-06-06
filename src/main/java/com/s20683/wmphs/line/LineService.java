package com.s20683.wmphs.line;

import com.s20683.wmphs.gui2wmphs.request.LineDTO;
import com.s20683.wmphs.stock.StockService;
import com.s20683.wmphs.tools.QueryTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LineService {
    protected final Logger logger = LoggerFactory.getLogger(LineService.class);
    private final LineRepository lineRepository;
    private final StockService stockService;
//    private final Map<Integer, Line> lines = new HashMap<>();

    public LineService(LineRepository lineRepository, StockService stockService) {
        this.lineRepository = lineRepository;
        this.stockService = stockService;
    }

//    @PostConstruct
//    public void init(){
//        QueryTimer timer = new QueryTimer();
//        lineRepository
//                .findAll()
//                .forEach(line -> {
//                    logger.info("Received from database line {}", line);
//                    lines.put(line.getId(), line);
//                });
//        logger.info("Find All operation for Lines executed on {}", timer);
//    }

    public List<LineDTO> getLines() {
        return lineRepository.findAll().stream().map(Line::toDTO).collect(Collectors.toList());
    }
    public void addLineToMap(Line line) {
//        if (line != null && line.getId() != null) {
//            lines.put(line.getId(), line);
//        }
    }

    @Transactional
    public String removeLine(int id) {
//        Line line = lines.get(id);
//        if (line != null) {
        try {
            QueryTimer timer = new QueryTimer();
            Optional<Line> lineFromDB = lineRepository.findById(id);
            if (lineFromDB.isPresent()) {
                Line line = lineFromDB.get();
                stockService.unallocateStock(line);
                lineRepository.deleteById(id);
                logger.info("Removed line and updated stock {} on database, executed {}", id, timer);
//            line.getCarrier().removeLine(line);
//            lines.remove(line.getId());
                return "OK";
            } else {
                logger.info("Cannot remove line {} on database, line not exist", id);
                return "Nie można usunąć linii ponieważ nie istnieje!";
            }

        } catch (Exception exception) {
            logger.warn("Exception while remove line {}", id, exception);
            return "Błąd podczas usuwania linii: " + exception.getMessage();
        }
//        }
//        logger.info("Line with id {} does not exist, cannot delete from database", id);
//        return "Linia o podanym id " + id +  " nie istnieje!";
    }

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
