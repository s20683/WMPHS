package com.s20683.wmphs.carrier;

import com.s20683.wmphs.line.Line;
import com.s20683.wmphs.line.LineRepository;
import com.s20683.wmphs.line.LineService;
import com.s20683.wmphs.order.CompletationOrder;
import com.s20683.wmphs.product.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CarrierPacker {

    public List<Carrier> packProducts(Map<Product, Integer> productQuantityMap, CompletationOrder order) throws RuntimeException {
        List<Line> lines = new ArrayList<>();
        List<Carrier> carriers = new ArrayList<>();
        carriers.add(new Carrier(Carrier.EMPTY_BARCODE, order.getCarrierVolume(), order));

        List<Map.Entry<Product, Integer>> sortedEntries = new ArrayList<>(productQuantityMap.entrySet());
        sortedEntries.sort((e1, e2) -> Integer.compare(e2.getKey().getVolume(), e1.getKey().getVolume()));

        for (Map.Entry<Product, Integer> entry : sortedEntries) {
            Product product = entry.getKey();
            int totalQuantity = entry.getValue();
            int productVolume = product.getVolume();

            while (totalQuantity > 0) {
                if (productVolume > order.getCarrierVolume())
                    throw new RuntimeException("Product volume " + productVolume + " is too big for carrierVolume " + order.getCarrierVolume());
                boolean placed = false;
                for (Carrier carrier : carriers) {
                    int availableVolume = carrier.getAvailableVolume();

                    int maxFittableUnits = availableVolume / productVolume;
                    if (maxFittableUnits > 0) {
                        int unitsToPlace = Math.min(maxFittableUnits, totalQuantity);
                        Line line = new Line(unitsToPlace, 0, product, carrier);
                        lines.add(line);
                        carrier.addLine(line);
                        totalQuantity -= unitsToPlace;
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    Carrier newCarrier = new Carrier(Carrier.EMPTY_BARCODE, order.getCarrierVolume(), order);
                    carriers.add(newCarrier);
                }
            }
        }
        return carriers;
    }

}
