package com.s20683.wmphs.stock;

import com.s20683.wmphs.gui2wmphs.request.CompressedStockDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockAggregator {
    public List<CompressedStockDTO> aggregateStocks(Map<Integer, Stock> stocks) {
        Map<Integer, CompressedStockDTO> compressedMap = new HashMap<>();
        for (Stock stock : stocks.values()) {
            CompressedStockDTO existing = compressedMap.get(stock.getProduct().getId());
            if (existing == null) {
                compressedMap.put(stock.getProduct().getId(), new CompressedStockDTO(stock.getId(), stock.getQuantity(), stock.getAllocatedQuantity(), stock.getProduct().getId(), stock.getProduct().getName(), stock.getProduct().getVolume()));
            } else {
                existing.setQuantity(existing.getQuantity() + stock.getQuantity());
                existing.setAllocatedQuantity(existing.getAllocatedQuantity() + stock.getAllocatedQuantity());
                existing.setNotAllocatedQuantity(existing.getQuantity() - existing.getAllocatedQuantity());
            }
        }
        return new ArrayList<>(compressedMap.values());
    }
}
