package year2024.day22;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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
        System.out.println(market.iterateAllBuyers());
    }
}

record Pair(long sumSecrets, int bestPrice) {
}

record Buyer(int initialSecret) {
    static final int ITERATIONS = 2000;
    static final int SEQUENCE_LENGTH = 4;
    static final int B24 = 0xffffff;

    void iterate(BiConsumer<String, Integer> priceWatcher, Consumer<Integer> secretWatcher) {
        int secret = initialSecret;
        List<Integer> deltas = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        for (int round = 0; round < ITERATIONS; round++) {
            int oldPrice = secret % 10;
            secret = (secret << 6 ^ secret) & B24;
            secret = (secret >> 5 ^ secret) & B24;
            secret = (secret << 11 ^ secret) & B24;
            int newPrice = secret % 10;
            if (deltas.size() >= SEQUENCE_LENGTH) {
                deltas.remove(0);
            }
            deltas.add(newPrice - oldPrice);
            if (deltas.size() >= SEQUENCE_LENGTH) {
                var pattern = deltas.stream().map(String::valueOf).collect(Collectors.joining());
                if (!visited.contains(pattern)) {
                    visited.add(pattern);
                    priceWatcher.accept(pattern, newPrice);
                }
            }
        }
        secretWatcher.accept(secret);
    }
}

record Market(List<Buyer> buyers) {
    Market(Stream<String> secrets) {
        this(secrets.map(s -> new Buyer(Integer.parseInt(s))).toList());
    }

    Pair iterateAllBuyers() {
        Map<String, Integer> prices = new HashMap<>();
        class SumHolder {
            long sum;

            void add(int i) {
                sum += i;
            }
        }
        var sumHolder = new SumHolder();
        buyers.forEach(buyer -> buyer.iterate(
                ((pattern, price) -> prices.compute(pattern, (k, v) -> v == null ? price : v + price)),
                sumHolder::add
        ));
        return new Pair(sumHolder.sum, prices.values().stream().max(Integer::compareTo).orElseThrow());
    }
}
