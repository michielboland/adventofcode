package year2025.day10;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Puzzle {

    private final List<Machine> machines = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day10_input")))).lines().map(Machine::parse).toList();

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
    }

    private int part1() {
        return machines.stream().mapToInt(Machine::fewestPresses).sum();
    }
}

record Machine(Lights desired, List<Button> buttons, List<Integer> joltages) {
    private static final Pattern PATTERN = Pattern.compile("\\[(.*)] (.*) \\{(.*)}");

    static Machine parse(String line) {
        var matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        var desired = Lights.parse(matcher.group(1));
        return new Machine(
                desired,
                Button.parse(matcher.group(2).split(" "), desired.size()),
                Arrays.stream(matcher.group(3).split(",")).map(Integer::valueOf).toList()
        );
    }

    @Override
    public String toString() {
        return "[" + desired + "] "
                + buttons.stream().map(String::valueOf).collect(Collectors.joining(" "))
                + " {" + joltages.stream().map(String::valueOf).collect(Collectors.joining(",")) + "}";
    }

    int fewestPresses() {
        var queue = new PriorityQueue<ND>();
        var visited = new HashSet<Lights>();
        queue.add(new ND(new Lights(0, desired.size()), new Button(0, desired.size()), 0, null));
        do {
            var current = queue.remove();
            visited.add(current.lights());
            if (current.lights().equals(desired)) {
                return current.distance();
            }
            for (var button : buttons) {
                var next = current.lights().toggle(button);
                if (!visited.contains(next)) {
                    visited.add(next);
                    queue.add(new ND(next, button, current.distance() + 1, current));
                }
            }
        } while (!queue.isEmpty());
        throw new IllegalStateException();
    }
}

record ND(Lights lights, Button button, int distance, ND previous) implements Comparable<ND> {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ND nd = (ND) o;
        return distance == nd.distance && Objects.equals(lights, nd.lights) && Objects.equals(button, nd.button);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lights, button, distance);
    }

    @Override
    public int compareTo(ND o) {
        int d = Integer.compare(distance, o.distance);
        if (d != 0) {
            return d;
        }
        // button sizes are always equal here
        d = Integer.compare(button.bitmap(), o.button.bitmap());
        if (d != 0) {
            return d;
        }
        return Integer.compare(lights.bitmap(), o.lights.bitmap());
    }
}

record Lights(int bitmap, int size) {
    static Lights parse(String symbols) {
        return new Lights(Integer.parseInt(symbols.replace('.', '0').replace('#', '1'), 2), symbols.length());
    }

    @Override
    public String toString() {
        return String.format("%" + size + "s", Integer.toBinaryString(bitmap)).replace(' ', '.').replace('0', '.').replace('1', '#');
    }

    Lights toggle(Button button) {
        return new Lights(bitmap ^ button.bitmap(), size);
    }
}

record Button(int bitmap, int size) {
    static List<Button> parse(String[] s, int size) {
        return Arrays.stream(s).map(string -> parse(string, size)).toList();
    }

    static Button parse(String s, int size) {
        return new Button(Arrays.stream(s.replaceAll("[()]", "").split(",")).mapToInt(Integer::parseInt).map(i -> 1 << (size - 1 - i)).reduce(0, (a, b) -> a | b), size);
    }

    @Override
    public String toString() {
        return "(" + IntStream.range(0, Integer.SIZE).filter(i -> (bitmap & (1 << (size - 1 - i))) != 0).mapToObj(String::valueOf).collect(Collectors.joining(",")) + ")";
    }
}
