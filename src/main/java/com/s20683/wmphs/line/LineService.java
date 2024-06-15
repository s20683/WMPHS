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
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    public void deleteLines(List<Line> linesRemove) {
        List<Line> linesToRemove = new ArrayList<>();
        for (Line line : lines.values()) {
            for (Line lineToRemove : linesRemove) {
                if (Objects.equals(line.getId(), lineToRemove.getId())) {
                    linesToRemove.add(line);
                }
            }
        }
        for (Line line : linesToRemove) {
            deleteLine(line.getId());
        }
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
}
