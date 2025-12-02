package year2025.day2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class Puzzle {

    private final List<Range> ranges;

    Puzzle() throws Exception {
        ranges = Arrays.stream(Files.readString(Path.of(Objects.requireNonNull(getClass().getResource("day2_input")).toURI())).trim().split(","))
                .map(Range::parse)
                .toList();
    }

    public static void main(String[] args) throws Exception {
        var puzzle = new Puzzle();
        System.out.println(puzzle.part1());
    }

    private long part1() {
        // 28845472378 is too low
        return ranges.stream().flatMap(r -> r.invalidIds().stream()).mapToLong(l -> l).sum();
    }
}

record Range(long min, long max) {
    static Range parse(String s) {
        var lr = s.split("-");
        return new Range(Long.parseLong(lr[0]), Long.parseLong(lr[1]));
    }

    List<Long> invalidIds() {
        var left = String.valueOf(min);
        var right = String.valueOf(max);
        String startBits;
        if (right.length() != left.length()) {
            if (right.length() != left.length() + 1) {
                throw new IllegalStateException();
            }
            if (left.length() % 2 == 0) {
                startBits = left.substring(0, left.length() / 2);
            } else {
                startBits = right.substring(0, right.length() / 2);
            }
        } else {
            if (left.length() % 2 != 0) {
                return List.of();
            } else {
                startBits = left.substring(0, left.length() / 2);
            }
        }
        var l = Long.parseLong(startBits);
        Set<Long> invalidIds = new TreeSet<>();
        do {
            var s = String.valueOf(l).repeat(2);
            var candidate = Long.parseLong(s);
            if (candidate > max) {
                break;
            }
            if (candidate >= min) {
                invalidIds.add(candidate);
            }
            ++l;
        } while (true);
        return List.copyOf(invalidIds);
    }
}
