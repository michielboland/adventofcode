package year2024.day22;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public class Day22 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Market market;

    Puzzle() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day22_input"))) {
            market = new Market(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        market.repeatIterateAllBuyers(2000);
        System.out.println(market.sumSecrets());
    }
}

class Market {
    static final int B24 = 0xffffff;
    private final int[] secrets;

    Market(Stream<String> secrets) {
        this.secrets = secrets.mapToInt(Integer::parseInt).toArray();
    }

    void iterateBuyer(int buyer) {
        int secret = secrets[buyer];
        secret = (secret << 6 ^ secret) & B24;
        secret = (secret >> 5 ^ secret) & B24;
        secret = (secret << 11 ^ secret) & B24;
        secrets[buyer] = secret;
    }

    void iterateAllBuyers() {
        for (int i = 0; i < secrets.length; i++) iterateBuyer(i);
    }

    void repeatIterateAllBuyers(@SuppressWarnings("SameParameterValue") int count) {
        for (int i = 0; i < count; i++) iterateAllBuyers();
    }

    long sumSecrets() {
        return Arrays.stream(secrets).mapToLong(i -> i).sum();
    }
}
