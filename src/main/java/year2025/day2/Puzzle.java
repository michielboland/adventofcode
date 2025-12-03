package year2025.day2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

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
        System.out.println(puzzle.part2());
    }

    private long part1() {
        return sum(Pattern.compile("(.+)\\1"));
    }

    private long part2() {
        return sum(Pattern.compile("(.+)\\1+"));
    }

    private long sum(Pattern pattern) {
        return ranges.stream().flatMap(r -> r.invalidIds(pattern).stream()).mapToLong(l -> l).sum();
    }
}

record Range(long min, long max) {

    static Range parse(String s) {
        var lr = s.split("-");
        return new Range(Long.parseLong(lr[0]), Long.parseLong(lr[1]));
    }

    List<Long> invalidIds(Pattern pattern) {
        List<Long> invalidIds = new ArrayList<>();
        for (var i = min; i <= max; i++) {
            if (pattern.matcher(String.valueOf(i)).matches()) {
                invalidIds.add(i);
            }
        }
        return List.copyOf(invalidIds);
    }
}
