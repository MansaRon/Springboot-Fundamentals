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
        lookupValueRepository.saveAll(defaults);
        log.info("Seeded {} lookup values.", defaults.size());
    }

    private List<LookupValue> provinces() {
        String[][] rows = {
                {"EASTERN_CAPE", "Eastern Cape"},
                {"FREE_STATE", "Free State"},
                {"GAUTENG", "Gauteng"},
                {"KWAZULU_NATAL", "KwaZulu-Natal"},
                {"LIMPOPO", "Limpopo"},
                {"MPUMALANGA", "Mpumalanga"},
                {"NORTH_WEST", "North West"},
                {"NORTHERN_CAPE", "Northern Cape"},
                {"WESTERN_CAPE", "Western Cape"}
        };
        List<LookupValue> values = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            values.add(LookupValue.builder()
                    .type(LookupType.PROVINCE)
                    .code(rows[i][0])
                    .label(rows[i][1])
                    .metadata(Map.of())
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
                        .metadata(Map.of("price", 0.0, "estimatedDays", 5))
                        .active(true).sortOrder(1).build(),
                LookupValue.builder().type(LookupType.DELIVERY_OPTION)
                        .code("EXPRESS").label("Express Delivery")
                        .metadata(Map.of("price", 50.0, "estimatedDays", 2))
                        .active(true).sortOrder(2).build(),
                LookupValue.builder().type(LookupType.DELIVERY_OPTION)
                        .code("OVERNIGHT").label("Overnight Delivery")
                        .metadata(Map.of("price", 150.0, "estimatedDays", 1))
                        .active(true).sortOrder(3).build()
        );
    }

    private List<LookupValue> paymentMethods() {
        return List.of(
                LookupValue.builder().type(LookupType.PAYMENT_METHOD)
                        .code("CREDIT_CARD").label("Credit Card")
                        .metadata(Map.of()).active(true).sortOrder(1).build(),
                LookupValue.builder().type(LookupType.PAYMENT_METHOD)
                        .code("CASH_ON_DELIVERY").label("Cash on Delivery")
                        .metadata(Map.of()).active(true).sortOrder(2).build(),
                LookupValue.builder().type(LookupType.PAYMENT_METHOD)
                        .code("EFT").label("EFT / Bank Transfer")
                        .metadata(Map.of()).active(true).sortOrder(3).build()
        );
    }
}
