package year2024.day11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class Day11 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    private final Stones stones;

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day11_input"))) {
            stones = Stones.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        for (int i = 0; i < 25; i++) {
            stones.blink();
        }
        stones.print();
    }
}

class Stones {
    private final SortedMap<Long, Long> stones = new TreeMap<>();
    long gap = 0x100000000L;

    static Stones from(Stream<String> lines) {
        var stones = new Stones();
        for (var line : lines.toList()) {
            stones.parse(line);
        }
        return stones;
    }

    void add(long number) {
        long nextKey = stones.isEmpty() ? 0L : stones.lastKey() + gap;
        stones.put(nextKey, number);
    }

    void parse(String line) {
        for (String number : line.split(" ")) {
            add(Long.parseLong(number));
        }
    }

    void blink() {
        gap >>= 1;
        if (gap == 0) {
            throw new IllegalStateException();
        }
        for (var key : Set.copyOf(stones.keySet())) {
            var stone = stones.get(key);
            if (stone == 0) {
                stones.put(key, 1L);
            } else {
                var number = String.valueOf(stone);
                var l = number.length();
                if ((l & 1) == 0) {
                    stones.put(key, Long.parseLong(number.substring(0, l >> 1)));
                    stones.put(key + gap, Long.parseLong(number.substring(l >> 1)));
                } else {
                    stones.put(key, stone * 2024L);
                }
            }
        }
    }

    void print() {
        System.out.println(stones.size());
    }
}
