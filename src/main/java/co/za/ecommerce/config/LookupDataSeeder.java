package co.za.ecommerce.config;

import co.za.ecommerce.model.lookup.LookupType;
import co.za.ecommerce.model.lookup.LookupValue;
import co.za.ecommerce.repository.LookupValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Seeds the {@code lookup_values} collection with default reference data on first
 * startup (when empty). Delivery options and payment methods mirror the
 * {@code DeliverMethod} / {@code PaymentMethod} enums; provinces are the nine SA
 * provinces. Idempotent: skips entirely once any lookup data exists, so admin
 * edits made via the API are never overwritten.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookupDataSeeder implements CommandLineRunner {

    private final LookupValueRepository lookupValueRepository;

    @Override
    public void run(String... args) {
        if (lookupValueRepository.count() > 0) {
            log.info("Lookup values already present — skipping seed.");
            return;
        }

        log.info("Seeding default lookup values...");
        List<LookupValue> defaults = new ArrayList<>();
        defaults.addAll(provinces());
        defaults.addAll(deliveryOptions());
        defaults.addAll(paymentMethods());
        defaults.addAll(categories());
        lookupValueRepository.saveAll(defaults);
        log.info("Seeded {} lookup values.", defaults.size());
    }

    private List<LookupValue> categories() {
        // Code is the machine value; label is the display text. Mirrors the former Category enum.
        String[][] categories = {
                {"CLOTHING", "Clothing"},
                {"ELECTRONICS", "Electronics"},
                {"ACCESSORIES", "Accessories"},
                {"FURNITURE", "Furniture"},
                {"SPORTS", "Sports"}
        };
        List<LookupValue> values = new ArrayList<>();
        for (int i = 0; i < categories.length; i++) {
            values.add(LookupValue.builder()
                    .type(LookupType.CATEGORY)
                    .code(categories[i][0])
                    .label(categories[i][1])
                    .active(true)
                    .sortOrder(i + 1)
                    .build());
        }
        return values;
    }

    private List<LookupValue> provinces() {
        // Province code mirrors the label (matches the values the frontend hardcoded).
        String[] names = {
                "Eastern Cape", "Free State", "Gauteng", "KwaZulu-Natal", "Limpopo",
                "Mpumalanga", "Northern Cape", "North West", "Western Cape"
        };
        List<LookupValue> values = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            values.add(LookupValue.builder()
                    .type(LookupType.PROVINCE)
                    .code(names[i])
                    .label(names[i])
                    .active(true)
                    .sortOrder(i + 1)
                    .build());
        }
        return values;
    }

    private List<LookupValue> deliveryOptions() {
        return List.of(
                LookupValue.builder().type(LookupType.DELIVERY_OPTION)
                        .code("STANDARD").label("Standard Delivery")
                        .metadata(Map.of(
                                "description", "Delivered within 5-7 business days",
                                "estimatedDays", "5-7 days",
                                "price", 0))
                        .active(true).sortOrder(1).build(),
                LookupValue.builder().type(LookupType.DELIVERY_OPTION)
                        .code("EXPRESS").label("Express Delivery")
                        .metadata(Map.of(
                                "description", "Delivered within 2-3 business days",
                                "estimatedDays", "2-3 days",
                                "price", 50))
                        .active(true).sortOrder(2).build(),
                LookupValue.builder().type(LookupType.DELIVERY_OPTION)
                        .code("OVERNIGHT").label("Overnight Delivery")
                        .metadata(Map.of(
                                "description", "Delivered next business day",
                                "estimatedDays", "Next day",
                                "price", 150))
                        .active(true).sortOrder(3).build()
        );
    }

    private List<LookupValue> paymentMethods() {
        // iconType drives the frontend's icon component selection.
        return List.of(
                LookupValue.builder().type(LookupType.PAYMENT_METHOD)
                        .code("CREDIT_CARD").label("Credit/Debit Card")
                        .metadata(Map.of("iconType", "card"))
                        .active(true).sortOrder(1).build(),
                LookupValue.builder().type(LookupType.PAYMENT_METHOD)
                        .code("EFT").label("EFT/Bank Transfer")
                        .metadata(Map.of("iconType", "eft"))
                        .active(true).sortOrder(2).build(),
                LookupValue.builder().type(LookupType.PAYMENT_METHOD)
                        .code("CASH_ON_DELIVERY").label("Cash on Delivery")
                        .metadata(Map.of("iconType", "cash"))
                        .active(true).sortOrder(3).build()
        );
    }
}
