package year2025.day12;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Puzzle {

    private final List<Shape> shapes;
    private final List<Region> regions;

    Puzzle() throws Exception {
        var input = Files.readString(Path.of(Objects.requireNonNull(getClass().getResource("day12_input")).toURI()));
        var parts = input.split("\n\n");
        shapes = IntStream.range(0, parts.length - 1).mapToObj(i -> Shape.parse(parts[i].split("\n"))).toList();
        regions = Arrays.stream(parts[parts.length - 1].split("\n")).map(Region::parse).toList();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
    }

    private int part1() {
        int n = 0;
        for (var region : regions) {
            List<Integer> shapeCounts = region.shapeCounts();
            int t = 0;
            for (int i = 0, shapeCountsSize = shapeCounts.size(); i < shapeCountsSize; i++) {
                t += shapeCounts.get(i) * shapes.get(i).size();
            }
            if (t <= region.area()) {
                n++;
            }
        }
        return n;
    }
}

record Shape(int size) {
    static Shape parse(String[] lines) {
        int n = 0;
        for (int i = 1; i < lines.length; i++) {
            for (byte b : lines[i].getBytes()) {
                if (b == '#') {
                    ++n;
                }
            }
        }
        return new Shape(n);
    }
}

record Region(int width, int height, List<Integer> shapeCounts) {
    int area() {
        return width * height;
    }

    static final Pattern PATTERN = Pattern.compile("(.*)x(.*): (.*)");

    static Region parse(String line) {
        var matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(line);
        }
        return new Region(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
                Arrays.stream(matcher.group(3).split(" ")).map(Integer::valueOf).toList());
    }
}
