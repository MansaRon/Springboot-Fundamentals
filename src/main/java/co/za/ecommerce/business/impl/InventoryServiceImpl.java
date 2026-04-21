package co.za.ecommerce.business.impl;

import co.za.ecommerce.business.InventoryService;
import co.za.ecommerce.exception.InventoryException;
import co.za.ecommerce.model.CartItems;
import co.za.ecommerce.model.Product;
import co.za.ecommerce.model.order.OrderItems;
import co.za.ecommerce.repository.ProductRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@Service
@AllArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final ProductRepository productRepository;

    /**
     * Reduce inventory after successful order
     */
    @Override
    public void reduceInventory(List<CartItems> items) {
        log.info("Reducing inventory for {} items", items.size());

        for (CartItems item : items) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new InventoryException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Product not found: " + item.getProduct().getId(),
                            HttpStatus.NOT_FOUND.value()
                    ));

            int currentQuantity = product.getQuantity();
            int requestedQuantity = item.getQuantity();

            if (currentQuantity < requestedQuantity) {
                log.error("Insufficient inventory for product: {}. Available: {}, Required: {}",
                        product.getTitle(), currentQuantity, requestedQuantity);
                throw new InventoryException(
                        HttpStatus.BAD_REQUEST.toString(),
                        String.format("Insufficient inventory for %s", product.getTitle()),
                        HttpStatus.BAD_REQUEST.value()
                );
            }

            // Reduce inventory
            int newQuantity = currentQuantity - requestedQuantity;
            product.setQuantity(newQuantity);
            product.setUpdatedAt(now());

            productRepository.save(product);

            log.info("Inventory reduced for {}: {} -> {}",
                    product.getTitle(), currentQuantity, newQuantity);

            // Optional: Send low stock alert
            if (newQuantity <= 5) {
                log.warn("LOW STOCK ALERT: {} has only {} units remaining",
                        product.getTitle(), newQuantity);
                // TODO: Trigger low stock notification
            }
        }

        log.info("Inventory reduction completed successfully");
    }

    /**
     * Restore inventory (for refunds/cancellations)
     */
    @Override
    public void restoreInventory(List<OrderItems> items) {
        log.info("Restoring inventory for {} items", items.size());

        for (OrderItems item : items) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new InventoryException(
                            HttpStatus.NOT_FOUND.toString(),
                            "Product not found: " + item.getProduct().getId(),
                            HttpStatus.NOT_FOUND.value()
                    ));

            int currentQuantity = product.getQuantity();
            int returnedQuantity = item.getQuantity();
            int newQuantity = currentQuantity + returnedQuantity;

            product.setQuantity(newQuantity);
            product.setUpdatedAt(now());

            productRepository.save(product);

            log.info("Inventory restored for {}: {} -> {}",
                    product.getTitle(), currentQuantity, newQuantity);
        }

        log.info("Inventory restoration completed successfully");
    }

    private void updateInventory(List<CartItems> items) {
        for (CartItems item : items) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() - item.getQuantity());
            productRepository.save(product);
        }
    }

}
