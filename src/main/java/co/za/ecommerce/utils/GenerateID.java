package co.za.ecommerce.utils;

import java.util.UUID;

public final class GenerateID {
    /**
     * Generate unique transaction ID
     * Format: TXN-20250315-UUID
     */
    public static String generateTransactionId() {
        String date = DateUtil.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return String.format("TXN-%s-%s", date, uuid);
    }
}
