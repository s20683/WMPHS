package com.s20683.wmphs.product;

import com.s20683.wmphs.gui2wmphs.request.ProductDTO;
import com.s20683.wmphs.tools.QueryTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductService {
    protected final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;
    private Map<Integer, Product> products = new HashMap<>();

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public String addProduct(ProductDTO productDTO) {
        Product product = products.get(productDTO.getId());

        if (product == null) {
            QueryTimer timer = new QueryTimer();
            product = productRepository.save(new Product(productDTO.getName(), productDTO.getLocation(), productDTO.getVolume()));
            if (product.getId() != null) {
                logger.info("Product {} saved to database, executed in {}", product, timer);
                products.put(product.getId(), product);
                return "OK";
            } else {
                logger.warn("Error while saving product {} to database", productDTO);
                return "Product does not exist but error while saving to database";
            }
        } else {
            QueryTimer timer = new QueryTimer();
            product.setName(productDTO.getName());
            product.setLocation(productDTO.getLocation());
            product.setVolume(productDTO.getVolume());
            product = productRepository.save(product);
            logger.info("Product {} updated on database, executed in {}", product, timer);
            products.put(product.getId(), product);
            return "OK";
        }
    }
}
