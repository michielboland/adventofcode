package year2025.day5;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
        System.out.println(ingredients.stream().filter(this::fresh).count());
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
}
