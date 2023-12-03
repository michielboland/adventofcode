package day3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day3 {
    public static void main(String[] args) throws IOException {
        new Puzzle1().solve();
    }
}

record Point(int x, int y) {
    Stream<Point> smear() {
        List<Point> points = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                points.add(new Point(x + i, y + j));
            }
        }
        return points.stream();
    }
}

record Rectangle(int x, int y, int width) {
    Stream<Point> smear() {
        return IntStream.range(0, width).mapToObj(i -> new Point(x + i, y));
    }
}

record Symbol(Point p, String code) {
}

record Part(Rectangle r, String code) {
}

class Schematic {
    private static final Pattern DIGITS = Pattern.compile("(\\d+)");
    private static final Pattern SYMBOL = Pattern.compile("([^.\\d]+)");
    private final Map<Point, Symbol> symbolsMap = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger();
    private final List<Part> parts = new ArrayList<>();
    private final List<Symbol> symbols = new ArrayList<>();

    void parse(Stream<String> lines) {
        lines.forEach(this::parse);
    }

    int solve() {
        symbols.forEach(symbol -> symbol.p().smear().forEach(point -> symbolsMap.put(point, symbol)));
        return parts.stream().filter(part -> part.r().smear().anyMatch(symbolsMap::containsKey)).mapToInt(part -> Integer.parseInt(part.code())).sum();
    }

    void parse(String line) {
        int y = counter.incrementAndGet();
        for (Matcher m = DIGITS.matcher(line); m.find(); ) {
            parts.add(new Part(new Rectangle(m.start() + 1, y, m.group(1).length()), m.group(1)));
        }
        for (Matcher m = SYMBOL.matcher(line); m.find(); ) {
            symbols.add(new Symbol(new Point(m.start() + 1, y), m.group(1)));
        }
    }
}

class Puzzle1 {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day3/day3_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            Schematic schematic = new Schematic();
            schematic.parse(reader.lines());
            System.out.println(schematic.solve());
        }
    }
}
