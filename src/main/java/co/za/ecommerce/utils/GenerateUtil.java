package co.za.ecommerce.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public final class GenerateUtil {
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

    /**
     * Generate unique order number
     * Format: ORD-20250315-A7B9C2
     */
    public static String generateOrderNumber() {
        String date = DateUtil.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String randomPart = generateRandomAlphanumeric();
        return String.format("ORD-%s-%s", date, randomPart);
    }

    /**
     * Generate random alphanumeric string
     */
    public static String generateRandomAlphanumeric() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 6; i++) {
            result.append(chars.charAt(new Random().nextInt(chars.length())));
        }

        return result.toString();
    }

    /**
     * Generate an OTP
     */
    public static String generateOTP() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    /**
     * Generate a strong temporary password that satisfies the account password policy
     * (at least one upper, lower, digit and special character; no whitespace; 12 chars).
     * Used for admin-triggered password resets.
     */
    public static String generateTempPassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "@#$%^&-+=()";
        String all = upper + lower + digits + special;

        Random random = new Random();
        StringBuilder result = new StringBuilder();

        // Guarantee one of each required character class.
        result.append(upper.charAt(random.nextInt(upper.length())));
        result.append(lower.charAt(random.nextInt(lower.length())));
        result.append(digits.charAt(random.nextInt(digits.length())));
        result.append(special.charAt(random.nextInt(special.length())));

        for (int i = result.length(); i < 12; i++) {
            result.append(all.charAt(random.nextInt(all.length())));
        }

        return result.toString();
    }

}
