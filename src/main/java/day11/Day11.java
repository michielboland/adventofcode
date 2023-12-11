package day11;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day11 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Galaxy(int x, int y) implements Comparable<Galaxy> {
    static Optional<Galaxy> at(int x, int y, int c) {
        return c == '#' ? Optional.of(new Galaxy(x, y)) : Optional.empty();
    }

    @Override
    public int compareTo(Galaxy o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }
}

record Universe(Set<Galaxy> galaxies, Set<Integer> horizontalNonExpansions, Set<Integer> verticalNonExpansions) {
    static void parse(Set<Galaxy> galaxies, int y, String line) {
        final AtomicInteger x = new AtomicInteger();
        line.chars().mapToObj(c -> Galaxy.at(x.getAndIncrement(), y, c)).filter(Optional::isPresent).map(Optional::get).forEach(galaxies::add);
    }

    static Universe parse(Stream<String> lines) {
        final AtomicInteger y = new AtomicInteger();
        Set<Galaxy> galaxies = new TreeSet<>();
        Set<Integer> h = new TreeSet<>();
        Set<Integer> v = new TreeSet<>();
        lines.forEach(line -> parse(galaxies, y.getAndIncrement(), line));
        galaxies.stream().mapToInt(Galaxy::x).forEach(h::add);
        galaxies.stream().mapToInt(Galaxy::y).forEach(v::add);
        return new Universe(galaxies, h, v);
    }

    long distance(Galaxy from, Galaxy to, long factor) {
        Set<Integer> xs = IntStream.rangeClosed(Integer.min(from.x(), to.x()), Integer.max(from.x(), to.x())).boxed().collect(Collectors.toSet());
        Set<Integer> ys = IntStream.rangeClosed(Integer.min(from.y(), to.y()), Integer.max(from.y(), to.y())).boxed().collect(Collectors.toSet());
        long dx = xs.size() - 1;
        long dy = ys.size() - 1;
        xs.removeAll(horizontalNonExpansions);
        ys.removeAll(verticalNonExpansions);
        dx += xs.size() * (factor - 1);
        dy += ys.size() * (factor - 1);
        return dx + dy;
    }

    long sumDistances(long factor) {
        return galaxies.stream().flatMap(a -> galaxies.stream().filter(b -> a.compareTo(b) < 0).map(b -> distance(a, b, factor))).mapToLong(i -> i).sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day11/day11_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var universe = Universe.parse(reader.lines());
            System.out.println(universe.sumDistances(2));
            System.out.println(universe.sumDistances(1000000));
        }
    }
}
