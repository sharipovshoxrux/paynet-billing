package uz.baraka.paynetbilling.util;

import java.util.concurrent.ThreadLocalRandom;

public class IdGenerator {
    private IdGenerator() {
    }

    public static String generateApplicationId() {
        long value = ThreadLocalRandom.current().nextLong(100_000_000L, 1_000_000_000L);
        return String.valueOf(value);
    }
}
