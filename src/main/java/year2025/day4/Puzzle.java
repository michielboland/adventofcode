package year2025.day4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class Puzzle {

    private final Grid grid = Grid.parse(new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day4_input")))).lines());

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(grid);
        System.out.println(grid.accessibleRolls());
    }
}

record Grid(Set<Coordinate> rolls, Set<Coordinate> spaces, int width, int height) {
    static Grid parse(Stream<String> lines) {
        Set<Coordinate> rolls = new HashSet<>();
        Set<Coordinate> spaces = new HashSet<>();
        final var y = new AtomicInteger();
        final Set<Integer> xs = new HashSet<>();
        lines.forEach(l -> {
            final var x = new AtomicInteger();
            l.chars().forEach(c -> {
                switch (c) {
                    case '.' -> spaces.add(new Coordinate(x.get(), y.get()));
                    case '@' -> rolls.add(new Coordinate(x.get(), y.get()));
                    default -> throw new IllegalArgumentException();
                }
                x.incrementAndGet();
            });
            xs.add(x.get());
            y.incrementAndGet();
        });
        if (xs.size() != 1) {
            throw new IllegalArgumentException();
        }
        return new Grid(Set.copyOf(rolls), Set.copyOf(spaces), xs.iterator().next(), y.get());
    }

    long accessibleRolls() {
        return rolls.stream().filter(this::accessible).count();
    }

    boolean accessible(Coordinate roll) {
        return roll.neighbours().filter(rolls::contains).count() < 4;
    }

    @Override
    public String toString() {
        List<String> lines = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            var a = new char[width];
            for (int x = 0; x < width; x++) {
                var c = new Coordinate(x, y);
                if (spaces.contains(c)) {
                    a[x] = '.';
                } else {
                    if (!rolls.contains(c)) {
                        throw new IllegalStateException();
                    }
                    a[x] = accessible(c) ? 'x' : '@';
                }
            }
            lines.add(new String(a));
        }
        return String.join("\n", lines);
    }
}

record Coordinate(int x, int y) {
    Coordinate north() {
        return new Coordinate(x, y - 1);
    }

    Coordinate south() {
        return new Coordinate(x, y + 1);
    }

    Coordinate east() {
        return new Coordinate(x + 1, y);
    }

    Coordinate west() {
        return new Coordinate(x - 1, y);
    }

    Stream<Coordinate> neighbours() {
        return Stream.of(north(), north().east(), east(), south().east(), south(), south().west(), west(), north().west());
    }
}
