package com.s20683.wmphs.stock;

import com.s20683.wmphs.gui2wmphs.request.CompressedStockDTO;
import com.s20683.wmphs.gui2wmphs.request.StockDTO;
import com.s20683.wmphs.product.Product;
import com.s20683.wmphs.product.ProductService;
import com.s20683.wmphs.tools.QueryTimer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockService {
    protected final Logger logger = LoggerFactory.getLogger(StockService.class);
    private final StockRepository stockRepository;
    private final ProductService productService;
    private final Map<Integer, Stock> stocks = new HashMap<>();
    private final StockAggregator stockAggregator = new StockAggregator();

    public StockService(StockRepository stockRepository, ProductService productService) {
        this.stockRepository = stockRepository;
        this.productService = productService;
    }
    @PostConstruct
    public void init(){
        QueryTimer timer = new QueryTimer();
        stockRepository
                .findAll()
                .forEach(stock -> {
                    logger.info("Received from database stock {}", stock);
                    Product product = productService.getProductById(stock.getProductId());
                    stock.setProduct(product);
                    stocks.put(stock.getId(), stock);
                });
        logger.info("Find All operation for Stocks executed on {}", timer);

    }
    public List<StockDTO> getStocks() {
        return stocks.values().stream().map(Stock::toDTO).collect(Collectors.toList());
    }
    public Stock getStockById(int id) {
        return stocks.get(id);
    }
    public List<CompressedStockDTO> getCompressedStocks(){
        return stockAggregator.aggregateStocks(stocks);
    }
    public void updateStockOnDB(Stock stock) {
        stockRepository.save(stock);
    }
    public void deleteStock(Stock stock) {
        stockRepository.delete(stock);
        stocks.remove(stock.getId());
        logger.info("Deleted stock {}", stock);
    }
    public String createStock(StockDTO stockDTO) {
        Stock stock = stocks.get(stockDTO.getId());
        Product product = productService.getProduct(stockDTO.getProductId());
        if (product == null) {
            logger.info("Cannot create stock with null product!");
            return "Produkt o podanym id " + stockDTO.getProductId() + " nie istnieje!";
        }
        Date sqlExpDate = Date.valueOf(stockDTO.getExpDate());

        if (stock == null) {
            try {
                QueryTimer timer = new QueryTimer();
                stock = stockRepository.save(new Stock(stockDTO.getQuantity(), stockDTO.getAllocatedQuantity(), sqlExpDate, product));
                if (stock.getId() != null) {
                    logger.info("Stock {} saved to database, executed in {}", stock, timer);
                    stocks.put(stock.getId(), stock);
                    return "OK";
                } else {
                    logger.warn("Error while saving stock {} to database", stockDTO);
                    return "Stock nie istnieje ale podczas dodawnia do bazy powstał błąd!";
                }
            } catch (Exception exception) {
                logger.warn("Error while saving stock {} to database", stockDTO, exception);
                return "Stock nie istnieje ale podczas dodawnia do bazy powstał błąd " + exception.getMessage();
            }
        } else {
            QueryTimer timer = new QueryTimer();
            stock.setQuantity(stockDTO.getQuantity());
            stock.setProduct(product);
            stock.setExpDate(sqlExpDate);
            stockRepository.save(stock);
            logger.info("Stock {} updated on database, executed in {}", stock, timer);
            return "OK";
        }
    }
    public List<Stock> getAllByProductId(int productId) {
        return stocks.values().stream().filter(stock -> stock.getProduct().getId().equals(productId)).toList();
    }
}
