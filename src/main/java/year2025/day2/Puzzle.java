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
    }

    private long part1() {
        return ranges.stream().flatMap(r -> r.invalidIds().stream()).mapToLong(l -> l).sum();
    }
}

record Range(long min, long max) {
    private static final Pattern REPEAT_TWO = Pattern.compile("(.+)\\1");

    static Range parse(String s) {
        var lr = s.split("-");
        return new Range(Long.parseLong(lr[0]), Long.parseLong(lr[1]));
    }

    List<Long> invalidIds() {
        List<Long> invalidIds = new ArrayList<>();
        for (var i = min; i <= max; i++) {
            if (REPEAT_TWO.matcher(String.valueOf(i)).matches()) {
                invalidIds.add(i);
            }
        }
        return List.copyOf(invalidIds);
    }
}
