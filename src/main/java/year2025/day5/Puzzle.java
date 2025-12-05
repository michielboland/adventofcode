package year2025.day5;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Puzzle {

    private final List<Range> ranges;
    private final List<Long> ingredients;

    Puzzle() throws Exception {
        var input = Files.readString(Path.of(Objects.requireNonNull(getClass().getResource("day5_input")).toURI()));
        var parts = input.split("\n\n");
        ranges = Arrays.stream(parts[0].split("\n")).map(Range::parse).toList();
        ingredients = Arrays.stream(parts[1].split("\n")).map(Long::parseLong).toList();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    boolean fresh(long ingredient) {
        return ranges.stream().anyMatch(r -> r.contains(ingredient));
    }

    private void solve() {
        System.out.println(part1());
        // 338928290274353 is too high
        System.out.println(part2());
    }

    private long part1() {
        return ingredients.stream().filter(this::fresh).count();
    }

    private long part2() {
        List<Range> combined = Range.combine(ranges);
        return combined.stream().mapToLong(Range::size).sum();
    }
}

record Range(long min, long max) {
    static Range parse(String line) {
        var parts = line.split("-");
        return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }

    boolean contains(long n) {
        return n >= min && n <= max;
    }

    boolean overlaps(Range other) {
        return contains(other.min) || contains(other.max);
    }

    Range combine(Range other) {
        return new Range(Math.min(min, other.min), Math.max(max, other.max));
    }

    long size() {
        return max + 1L - min;
    }

    @Override
    public String toString() {
        return min + "-" + max;
    }

    static List<Range> combine(List<Range> ranges) {
        var copy = new ArrayList<>(ranges);
        for (var done = false; !done; ) {
            done = iterate(copy);
        }
        System.err.println(copy.stream().map(String::valueOf).collect(Collectors.joining("\n")));
        return copy;
    }

    private static boolean iterate(ArrayList<Range> copy) {
        for (int i = 0; i < copy.size(); i++) {
            for (int j = i + 1; j < copy.size(); j++) {
                var left = copy.get(i);
                var right = copy.get(j);
                if (left.overlaps(right)) {
                    copy.remove(j);
                    copy.remove(i);
                    copy.add(left.combine(right));
                    return false;
                }
            }
        }
        return true;
    }
}
