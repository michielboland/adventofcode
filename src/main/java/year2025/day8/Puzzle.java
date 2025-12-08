package year2025.day8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Puzzle {

    static final int CONNECTIONS = 1000;
    private final List<Box> boxes = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day8_input")))).lines().map(Box::parse).toList();

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
    }

    private long part1() {
        List<Pair> pairs = new ArrayList<>();
        for (int i = 0; i < boxes.size(); i++) {
            for (int j = i + 1; j < boxes.size(); j++) {
                pairs.add(Pair.of(boxes.get(i), boxes.get(j)));
            }
        }
        var sorted = pairs.stream().sorted(Comparator.comparing(Pair::distance)).toList();
        List<Set<Box>> circuits = new ArrayList<>();
        for (int i = 0; i < CONNECTIONS && i < sorted.size(); i++) {
            var pair = sorted.get(i);
            boolean found = false;
            for (int j = 0; j < circuits.size(); j++) {
                var left = circuits.get(j);
                var aInLeft = left.contains(pair.a());
                var bInLeft = left.contains(pair.b());
                if (aInLeft && bInLeft) {
                    found = true;
                    break;
                }
                if (aInLeft || bInLeft) {
                    for (int k = j + 1; k < circuits.size(); k++) {
                        var right = circuits.get(k);
                        var aInRight = right.contains(pair.a());
                        var bInRight = right.contains(pair.b());
                        if (aInRight || bInRight) {
                            found = true;
                            left.addAll(right);
                            circuits.remove(k);
                            break;
                        }
                    }
                    if (!found) {
                        left.add(aInLeft ? pair.b() : pair.a());
                        found = true;
                    }
                    break;
                }
            }
            if (!found) {
                var newCircuit = new HashSet<Box>();
                newCircuit.add(pair.a());
                newCircuit.add(pair.b());
                circuits.add(newCircuit);
            }
        }
        var sortedSizes = circuits.stream().mapToLong(Set::size).sorted().toArray();
        var l = sortedSizes.length;
        return sortedSizes[l - 1] * sortedSizes[l - 2] * sortedSizes[l - 3];
    }
}

record Pair(long distance, Box a, Box b) {
    static Pair of(Box a, Box b) {
        return new Pair(a.distance(b), a, b);
    }
}

record Box(long x, long y, long z) {
    static Box parse(String line) {
        var parts = line.split(",");
        return new Box(Long.parseLong(parts[0]), Long.parseLong(parts[1]), Long.parseLong(parts[2]));
    }

    static long square(long n) {
        return n * n;
    }

    long distance(Box other) {
        return square(x - other.x) + square(y - other.y) + square(z - other.z);
    }
}
