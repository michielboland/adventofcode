package year2024.day11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

public class Day11 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    private final Stones initialStones;

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day11_input"))) {
            initialStones = Stones.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        var after25 = initialStones.blink(25);
        System.out.println(after25.total());
        var after75 = after25.blink(50);
        System.out.println(after75.total());
    }
}

class Stones {
    private final SortedMap<Long, Long> stones = new TreeMap<>();

    static Stones from(Stream<String> lines) {
        var stones = new Stones();
        for (var line : lines.toList()) {
            stones.parse(line);
        }
        return stones;
    }

    void add(long number, long count) {
        stones.compute(number, (k, v) -> v == null ? count : v + count);
    }

    void parse(String line) {
        for (String number : line.split(" ")) {
            add(Long.parseLong(number), 1);
        }
    }

    Stones blink() {
        var newStones = new Stones();
        for (var stone : stones.keySet()) {
            var count = stones.get(stone);
            if (stone == 0) {
                newStones.add(1, count);
            } else {
                var number = String.valueOf(stone);
                var l = number.length();
                if ((l & 1) == 0) {
                    newStones.add(Long.parseLong(number.substring(0, l >> 1)), count);
                    newStones.add(Long.parseLong(number.substring(l >> 1)), count);
                } else {
                    newStones.add(stone * 2024L, count);
                }
            }
        }
        return newStones;
    }

    Stones blink(int count) {
        var result = this;
        for (int i = 0; i < count ; i++) {
            result = result.blink();
        }
        return result;
    }

    long total() {
        return stones.values().stream().mapToLong(l -> l).sum();
    }
}
