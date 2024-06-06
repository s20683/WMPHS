package com.s20683.wmphs.order;

import com.s20683.wmphs.carrier.Carrier;
import com.s20683.wmphs.gui2wmphs.request.CompleteLineDTO;
import com.s20683.wmphs.gui2wmphs.request.LineDTO;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.line.LineRepository;
import com.s20683.wmphs.stock.StockService;
import com.s20683.wmphs.tools.QueryTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CompletedOrderService {

    protected final Logger logger = LoggerFactory.getLogger(CompletedOrderService.class);
    private final LineRepository lineRepository;
    private final StockService stockService;
    private final OrderService orderService;

    public CompletedOrderService(LineRepository lineRepository, StockService stockService, OrderService orderService) {
        this.lineRepository = lineRepository;
        this.stockService = stockService;
        this.orderService = orderService;
    }

    @Transactional
    public String completeLine(CompleteLineDTO completeLineDTO) {
        QueryTimer timer = new QueryTimer();
        try {
            Optional<Line> lineFromDB = lineRepository.findById(completeLineDTO.getLine().getId());
            if (lineFromDB.isEmpty()) {
                logger.info("Line with id {} does not exist!", completeLineDTO.getLine().getId());
                return "Linia z id " + completeLineDTO.getLine().getId() + " nie istnieje!";
            }
            Line line = lineFromDB.get();
            line.setQuantityCompleted(completeLineDTO.getLine().getQuantityCompleted());
            stockService.completeLine(line);
            if (line.getQuantityToComplete() == 0) {
                orderService.checkOrderCompleted(completeLineDTO);
            }
            lineRepository.save(line);
            logger.info("Line {} updated on database, executed {}", line, timer);
            return "OK";
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}
