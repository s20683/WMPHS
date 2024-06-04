package com.s20683.wmphs.stock;

import com.s20683.wmphs.gui2wmphs.request.StockDTO;
import com.s20683.wmphs.product.Product;
import com.s20683.wmphs.product.ProductService;
import com.s20683.wmphs.tools.QueryTimer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StockService {
    protected final Logger logger = LoggerFactory.getLogger(StockService.class);
    private final StockRepository stockRepository;
    private final ProductService productService;
    private final Map<Integer, Stock> stocks = new HashMap<>();

    public StockService(StockRepository stockRepository, ProductService productService) {
        this.stockRepository = stockRepository;
        this.productService = productService;
    }

    public String removeStock(int id) {
        Stock stockToRemove = stocks.get(id);
        if (stockToRemove == null) {
            logger.info("Cannot remove stock with id {}, does not exist", id);
            return "Stock z id " + id + " nie istnieje";
        }
        QueryTimer timer = new QueryTimer();
        try {
            stockRepository.delete(stockToRemove);
            stocks.remove(stockToRemove.getId());
            logger.info("Stock {} removed from database, executed {}", stockToRemove, timer);
            return "OK";
        } catch (Exception exception) {
            logger.warn("Exception while remove stock {}", stockToRemove, exception);
            return exception.getMessage();
        }
    }
    @PostConstruct
    public void init(){
        QueryTimer timer = new QueryTimer();
        stockRepository
                .findAll()
                .forEach(stock -> {
                    logger.info("Received from database stock {}", stock);
                    stocks.put(stock.getId(), stock);
                });
        logger.info("Find All operation for Stocks executed on {}", timer);
    }
    public List<StockDTO> getStocks() {
        return stocks.values().stream().map(Stock::toDTO).collect(Collectors.toList());
    }
    public String addStock(StockDTO stockDTO) {
        Stock stock = stocks.get(stockDTO.getId());
        Product product = productService.getProduct(stockDTO.getProductId());
        if (product == null) {
            logger.info("Cannot create stock with null product!");
            return "Produkt o podanym id " + stockDTO.getProductId() + " nie istnieje!";
        }
        Date sqlExpDate = Date.valueOf(stockDTO.getExpDate());

        if (stock == null) {
            QueryTimer timer = new QueryTimer();
            stock = stockRepository.save(new Stock(stockDTO.getQuantity(), sqlExpDate, product));
            if (stock.getId() != null) {
                logger.info("Stock {} saved to database, executed in {}", stock, timer);
                stocks.put(stock.getId(), stock);
                return "OK";
            } else {
                logger.warn("Error while saving stock {} to database", stockDTO);
                return "Stock nie istnieje ale podczas dodawnia do bazy powstał błąd!";
            }
        } else {
            QueryTimer timer = new QueryTimer();
            stock.setQuantity(stockDTO.getQuantity());
            stock.setProduct(product);
            stock.setExpDate(sqlExpDate);
            stock = stockRepository.save(stock);
            logger.info("Stock {} updated on database, executed in {}", stock, timer);
            stocks.put(stock.getId(), stock);
            return "OK";
        }
    }

}
