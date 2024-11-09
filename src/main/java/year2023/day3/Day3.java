package year2023.day3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day3 {
    public static void main(String[] args) throws IOException {
        new Puzzle1().solve();
        new Puzzle2().solve();
    }
}

record Point(int x, int y) {
    Stream<Point> smear() {
        return IntStream.rangeClosed(-1, 1).boxed().flatMap(i -> IntStream.rangeClosed(-1, 1).mapToObj(j -> new Point(x + i, y + j)));
    }
}

record Rectangle(int x, int y, int width) {
    Stream<Point> smear() {
        return IntStream.range(0, width).mapToObj(i -> new Point(x + i, y));
    }
}

record Symbol(Point p, String code, Set<Part> parts) {
    Symbol(Point p, String code) {
        this(p, code, new HashSet<>());
    }

    boolean isGear() {
        return "*".equals(code) && parts.size() == 2;
    }

    int gearRatio() {
        return parts.stream().map(part -> Integer.parseInt(part.code())).reduce(1, (a, b) -> a * b);
    }
}

record Part(Rectangle r, String code) {
}

class Schematic {
    private static final Pattern DIGITS = Pattern.compile("(\\d+)");
    private static final Pattern SYMBOL = Pattern.compile("([^.\\d]+)");
    private final Map<Point, Symbol> symbolsMap = new HashMap<>();
    private final AtomicInteger counter = new AtomicInteger();
    private final List<Part> parts = new ArrayList<>();
    private final Set<Part> usedParts = new HashSet<>();
    private final List<Symbol> symbols = new ArrayList<>();

    void parse(Stream<String> lines) {
        lines.forEach(this::parse);
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

    void buildMap() {
        symbols.forEach(symbol -> symbol.p().smear().forEach(point -> symbolsMap.put(point, symbol)));
        parts.forEach(part -> part.r().smear().forEach(point -> Optional.ofNullable(symbolsMap.get(point)).ifPresent(symbol -> {
                    usedParts.add(part);
                    symbol.parts().add(part);
                }
        )));
    }

    int solve() {
        buildMap();
        return usedParts.stream().mapToInt(part -> Integer.parseInt(part.code())).sum();
    }

    int gears() {
        buildMap();
        return symbols.stream().filter(Symbol::isGear).mapToInt(Symbol::gearRatio).sum();
    }
}

abstract class Puzzle {
    abstract void solve(Schematic schematic);

    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day3/day3_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            Schematic schematic = new Schematic();
            schematic.parse(reader.lines());
            solve(schematic);
        }
    }
}

class Puzzle1 extends Puzzle {

    @Override
    void solve(Schematic schematic) {
        System.out.println(schematic.solve());
    }
}

class Puzzle2 extends Puzzle {

    @Override
    void solve(Schematic schematic) {
        System.out.println(schematic.gears());
    }
}
