package year2025.day4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
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
        System.out.println(grid.accessibleRolls());
        System.out.println(grid.bruteforceIt());
    }
}

record Grid(Set<Coordinate> rolls) {
    static Grid parse(Stream<String> lines) {
        Set<Coordinate> rolls = new HashSet<>();
        final var y = new AtomicInteger();
        final Set<Integer> xs = new HashSet<>();
        lines.forEach(l -> {
            final var x = new AtomicInteger();
            l.chars().forEach(c -> {
                if (c == '@') {
                    rolls.add(new Coordinate(x.get() + Coordinate.M * y.get()));
                }
                x.incrementAndGet();
            });
            xs.add(x.get());
            y.incrementAndGet();
        });
        if (xs.size() != 1) {
            throw new IllegalArgumentException();
        }
        return new Grid(rolls);
    }

    long accessibleRolls() {
        return rolls.stream().filter(this::accessible).count();
    }

    long removeAccessibleRolls() {
        long n = 0;
        var i = rolls.iterator();
        while (i.hasNext()) {
            var roll = i.next();
            if (accessible(roll)) {
                // Yes, this works
                i.remove();
                ++n;
            }
        }
        return n;
    }

    long bruteforceIt() {
        long n = 0;
        do {
            var a = removeAccessibleRolls();
            if (a == 0) {
                return n;
            }
            n += a;
        } while (true);
    }

    boolean accessible(Coordinate roll) {
        return roll.neighbours().filter(rolls::contains).count() < 4;
    }
}

record Coordinate(int p) {
    static final int M = 1_000;

    Coordinate north() {
        return new Coordinate(p - M);
    }

    Coordinate south() {
        return new Coordinate(p + M);
    }

    Coordinate east() {
        return new Coordinate(p + 1);
    }

    Coordinate west() {
        return new Coordinate(p - 1);
    }

    Stream<Coordinate> neighbours() {
        return Stream.of(north(), north().east(), east(), south().east(), south(), south().west(), west(), north().west());
    }
}
