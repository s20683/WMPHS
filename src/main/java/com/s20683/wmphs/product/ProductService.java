package com.s20683.wmphs.product;

import com.s20683.wmphs.gui2wmphs.request.ProductDTO;
import com.s20683.wmphs.tools.QueryTimer;
import jakarta.annotation.PostConstruct;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    protected final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;
    private Map<Integer, Product> products = new HashMap<>();

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void init(){
        QueryTimer timer = new QueryTimer();
        productRepository
                .findAll()
                .forEach(product -> {
                    logger.info("Received from database product {}", product);
                    products.put(product.getId(), product);
                });
        logger.info("Find All operation for Products executed on {}", timer);
    }

    public Product getProduct(int id) {
        return products.get(id);
    }
    public String removeProduct(int id) {
        Product productToRemove = products.get(id);
        if (productToRemove == null) {
            logger.info("Cannot remove product with id {}, does not exist", id);
            return "Produkt z id " + id + " nie istnieje";
        }
        QueryTimer timer = new QueryTimer();
        try {
            productRepository.delete(productToRemove);
            products.remove(productToRemove.getId());
            logger.info("Product {} removed from database, executed {}", productToRemove, timer);
            return "OK";
        } catch (Exception exception) {
            logger.warn("Exception while remove product {}", productToRemove, exception);
            return exception.getMessage();
        }
    }
    public List<ProductDTO> getProducts(){
        return products.values().stream().map(Product::toDTO).collect(Collectors.toList());
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
            productRepository.save(product);
            logger.info("Product {} updated on database, executed in {}", product, timer);
            products.put(product.getId(), product);
            return "OK";
        }
    }
}
