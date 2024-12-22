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
        market.repeatIterateAllBuyers();
        System.out.println(market.sumSecrets());
        market.bruteForceIt();
    }
}

class Market {
    static final int ITERATIONS = 2000;
    static final int B24 = 0xffffff;
    private final int[] secrets;
    private final byte[][] prices;
    private final byte[][] deltas;

    Market(Stream<String> secrets) {
        this.secrets = secrets.mapToInt(Integer::parseInt).toArray();
        prices = new byte[this.secrets.length][];
        deltas = new byte[this.secrets.length][];
        for (int i = 0; i < prices.length; i++) {
            prices[i] = new byte[ITERATIONS];
        }
        for (int i = 0; i < deltas.length; i++) {
            deltas[i] = new byte[ITERATIONS];
        }
    }

    static int findFirst(byte[] haystack, byte[] needle) {
        for (int i = 0; i <= haystack.length - needle.length; i++) {
            boolean found = true;
            for (int j = 0; found && j < needle.length; j++) {
                found = needle[j] == haystack[i + j];
            }
            if (found) {
                return i;
            }
        }
        return -1;
    }

    int bananas(byte[] pattern) {
        if (impossible(pattern)) {
            return 0;
        }
        int bananas = 0;
        for (int i = 0; i < deltas.length; i++) {
            int pos = findFirst(deltas[i], pattern);
            if (pos != -1) {
                bananas += prices[i][pos + 3];
            }
        }
        return bananas;
    }

    boolean impossible(byte[] pattern) {
        boolean possible;
        for (byte start = -9; start <= 9; start++) {
            possible = true;
            int t = start;
            for (byte b : pattern) {
                t = t + b;
                if (t < -9 || t > 9) {
                    possible = false;
                    break;
                }
            }
            if (possible) {
                return false;
            }
        }
        return true;
    }

    void bruteForceIt() {
        int bestPrice = 0;
        byte[] tryThese = new byte[]{0, 1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 6, -6, 7, -7, 8, -8, 9, -9};
        for (byte i : tryThese) {
            for (byte j : tryThese) {
                for (byte k : tryThese) {
                    for (byte l : tryThese) {
                        byte[] pattern = {i, j, k, l};
                        var price = bananas(pattern);
                        if (price > bestPrice) {
                            bestPrice = price;
                            System.out.printf("pattern (%d,%d,%d,%d) -> price %d%n", i, j, k, l, price);
                        }
                    }
                }
            }
        }
        System.out.println("best price = " + bestPrice);
    }

    void iterateBuyer(int round, int buyer) {
        int secret = secrets[buyer];
        int oldPrice = secret % 10;
        secret = (secret << 6 ^ secret) & B24;
        secret = (secret >> 5 ^ secret) & B24;
        secret = (secret << 11 ^ secret) & B24;
        int newPrice = secret % 10;
        secrets[buyer] = secret;
        prices[buyer][round] = (byte) newPrice;
        deltas[buyer][round] = (byte) (newPrice - oldPrice);
    }

    void iterateAllBuyers(int i) {
        for (int j = 0; j < secrets.length; j++) iterateBuyer(i, j);
    }

    void repeatIterateAllBuyers() {
        for (int i = 0; i < ITERATIONS; i++) iterateAllBuyers(i);
    }

    long sumSecrets() {
        return Arrays.stream(secrets).mapToLong(i -> i).sum();
    }
}
