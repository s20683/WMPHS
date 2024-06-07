package com.s20683.wmphs.stock;

import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.line.LineService;
import com.s20683.wmphs.tools.QueryTimer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;


@Service
public class AllocatedStockService {
    protected final Logger logger = LoggerFactory.getLogger(AllocatedStockService.class);
    private final AllocatedStockRepository allocatedStockRepository;
    private final Map<Integer, AllocatedStock> allocatedStocks = new HashMap<>();

    @Autowired
    private final StockService stockService;

    @Autowired
    private final LineService lineService;

    public AllocatedStockService(AllocatedStockRepository allocatedStockRepository, StockService stockService, LineService lineService) {
        this.allocatedStockRepository = allocatedStockRepository;
        this.stockService = stockService;
        this.lineService = lineService;
    }
    @PostConstruct
    public void init() {
        QueryTimer timer = new QueryTimer();
        allocatedStockRepository
                .findAll()
                .forEach(allocatedStock -> {
                    logger.info("Received from database allocatedStock {}", allocatedStock);
                    allocatedStocks.put(allocatedStock.getId(), allocatedStock);
                    Stock stock = stockService.getStockById(allocatedStock.getStockId());
                    stock.addAllocatedStock(allocatedStock);
                    Line line = lineService.getLineById(allocatedStock.getLineId());
                    line.addAllocatedStock(allocatedStock);
                    allocatedStock.setStock(stock);
                    allocatedStock.setLine(line);
                });
        logger.info("Find All operation for AllocatedStocks executed on {}", timer);
    }
    public List<AllocatedStock> getAllocatedStocksByLineId(int lineId) {
        return this.allocatedStocks.values().stream().filter(allocatedStock -> Objects.equals(allocatedStock.getLineId(), lineId)).toList();
    }

    @Transactional
    public void completeLine(Line line) throws Exception {
        List<AllocatedStock> allocatedStocksList = getAllocatedStocksByLineId(line.getId());

        logger.info("Find allocatedStocks {}", allocatedStocksList);

        int quantityToComplete = line.getQuantity();
        for (AllocatedStock allocatedStock : allocatedStocksList) {
            if (quantityToComplete <= 0) {
                line.getCarrier().removeLine(line);
                logger.info("Line completed {}", line);
                return;
            }
            Stock stock = allocatedStock.getStock();
            logger.info("Find and update stock {}", stock);

            stock.completeQuantity(allocatedStock.getQuantity());
            int qty = quantityToComplete;
            quantityToComplete -= allocatedStock.getQuantity();
            logger.info("Quantity to complete {} - {} => {}", qty, allocatedStock.getQuantity(), quantityToComplete);

            allocatedStockRepository.delete(allocatedStock);
            allocatedStocks.remove(allocatedStock.getId());
            stock.removeAllocatedStock(allocatedStock);
            line.removeAllocatedStock(allocatedStock);

            if (stock.getQuantity() == 0) {
                stockService.deleteStock(stock);
            } else {
                stockService.updateStockOnDB(stock);
            }
        }
        if (quantityToComplete <= 0) {
            line.getCarrier().removeLine(line);
            logger.info("Line completed {}", line);
            return;
        }
        logger.info("Not all line completed {} **** Wrong allocation", line);
    }
    public String removeStock(int id) {
        Stock stockToRemove = stockService.getStockById(id);
        if (stockToRemove == null) {
            logger.info("Cannot remove stock with id {}, does not exist", id);
            return "Stock z id " + id + " nie istnieje";
        }
        if (!stockToRemove.getAllocatedStocks().isEmpty()) {
            logger.info("Cannot remove allocated stock {}!", stockToRemove);
            return "Nie można usunąć stocka " + stockToRemove + " posiada alokacje!";
        }
        QueryTimer timer = new QueryTimer();
        try {
            stockService.deleteStock(stockToRemove);
            logger.info("Stock {} removed from database, executed {}", id, timer);
            return "OK";
        } catch (Exception exception) {
            logger.warn("Exception while remove stock {}", id, exception);
            return exception.getMessage();
        }
    }
    @Transactional
    public String removeLine(int id) {
        Line line = lineService.getLineById(id);

        if (line != null) {
            try {
                QueryTimer timer = new QueryTimer();

                unallocateStock(line);
                String result =  lineService.deleteLine(id);
                logger.info("Removed line and updated stock {} on database, executed {}", id, timer);
                return result;

            } catch (Exception exception) {
                logger.warn("Exception while remove line {}", id, exception);
                return "Błąd podczas usuwania linii: " + exception.getMessage();
            }
        }
        logger.info("Line with id {} does not exist, cannot delete from database", id);
        return "Linia o podanym id " + id +  " nie istnieje!";
    }
    @Transactional
    public void unallocateStock(Line line) throws Exception {
        List<AllocatedStock> allocatedStockList = getAllocatedStocksByLineId(line.getId());

        for (AllocatedStock allocatedStock : allocatedStockList) {
            Stock stock = allocatedStock.getStock();
            int quantityToDeallocate = allocatedStock.getQuantity();

            if (stock.getAllocatedQuantity() >= quantityToDeallocate) {
                stock.setAllocatedQuantity(stock.getAllocatedQuantity() - quantityToDeallocate);
                stockService.updateStockOnDB(stock);
            } else {
                throw new RuntimeException("More quantity requested for deallocation than was allocated");
            }

            allocatedStockRepository.delete(allocatedStock);
            stock.removeAllocatedStock(allocatedStock);
            line.removeAllocatedStock(allocatedStock);
            allocatedStocks.remove(allocatedStock.getId());
        }
    }
    @Transactional
    public void allocateStock(Line line) throws Exception {
        try {
            List<Stock> stocks = stockService.getAllByProductId(line.getProduct().getId())
                    .stream()
                    .sorted(Comparator.comparing(Stock::getExpDate))
                    .toList();
            int requiredQuantity = line.getQuantity();

            for (Stock stock : stocks) {
                if (requiredQuantity <= 0) {
                    break;
                }

                int availableToAllocate = stock.getAvailableQuantity();
                if (availableToAllocate > 0) {
                    int quantityToAllocate = Math.min(availableToAllocate, requiredQuantity);
                    AllocatedStock allocatedStock = new AllocatedStock(quantityToAllocate, stock.getExpDate(), stock, line);
                    allocatedStockRepository.save(allocatedStock);
                    allocatedStocks.put(allocatedStock.getId(), allocatedStock);
                    stock.setAllocatedQuantity(stock.getAllocatedQuantity() + quantityToAllocate);
                    line.addAllocatedStock(allocatedStock);
                    stockService.updateStockOnDB(stock);
                    allocatedStock.setStock(stock);
                    allocatedStock.setLine(line);
                    requiredQuantity -= quantityToAllocate;
                }
            }
            if (requiredQuantity > 0) {
                throw new RuntimeException("Nie można skompletować linii, brak wystarczającej ilości produktu w stockach");
            }
        } catch (Exception exception) {
            throw new RuntimeException("Błąd podczas aktualizacji stocków: " + exception.getMessage(), exception);
        }
    }
}
