package com.s20683.wmphs.order;

import com.s20683.wmphs.gui2wmphs.request.CompleteLineDTO;
import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.line.LineService;
import com.s20683.wmphs.stock.AllocatedStockService;
import com.s20683.wmphs.tools.QueryTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CompletedOrderService {

    protected final Logger logger = LoggerFactory.getLogger(CompletedOrderService.class);
    private final LineService lineService;
    private final OrderService orderService;
    private final AllocatedStockService allocatedStockService;

    public CompletedOrderService(LineService lineService, OrderService orderService, AllocatedStockService allocatedStockService) {
        this.lineService = lineService;
        this.orderService = orderService;
        this.allocatedStockService = allocatedStockService;
    }

    @Transactional
    public String completeLine(CompleteLineDTO completeLineDTO) {
        QueryTimer timer = new QueryTimer();
        try {
            Line line = lineService.getLineById(completeLineDTO.getLine().getId());
            if (line == null) {
                logger.info("Line with id {} does not exist!", completeLineDTO.getLine().getId());
                return "Linia z id " + completeLineDTO.getLine().getId() + " nie istnieje!";
            }
            line.setQuantityCompleted(completeLineDTO.getLine().getQuantity());

            allocatedStockService.completeLine(line);
            if (line.getQuantityToComplete() == 0) {
                orderService.checkOrderCompleted(completeLineDTO);
            }
            lineService.updateLineOnDB(line);
            logger.info("Line {} updated on database, executed {}", line, timer);
            return "OK";
        } catch (Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}
