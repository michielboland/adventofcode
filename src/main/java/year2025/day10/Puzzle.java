package year2025.day10;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
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
        // 17827 is too high
        System.out.println(part2());
    }

    private int part1() {
        return machines.stream().mapToInt(Machine::fewestPresses).sum();
    }

    private int part2() {
        return machines.stream().mapToInt(Machine::fewestPresses2).sum();
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

    List<Integer> operate(int buttonMask, List<Integer> originalJoltages) {
        var a = originalJoltages.toArray(new Integer[0]);
        for (int i = 0; i < buttons.size(); i++) {
            if ((buttonMask & 1 << i) != 0) {
                var button = buttons.get(i);
                for (int j = 0; j < button.size(); j++) {
                    if (button.isSet(j)) {
                        a[j] -= 1;
                        if (a[j] < 0) {
                            return null;
                        }
                    }
                }
            }
        }
        return Arrays.stream(a).map(i -> {
                    if ((i & 1) != 0) {
                        throw new IllegalStateException();
                    }
                    return i >> 1;
                })
                .toList();
    }

    @Override
    public String toString() {
        return "[" + desired + "] "
                + buttons.stream().map(String::valueOf).collect(Collectors.joining(" "))
                + " {" + joltages.stream().map(String::valueOf).collect(Collectors.joining(",")) + "}";
    }

    int fewestPresses() {
        return presses(new Lights(0, desired.size()), desired, true).fewest();
    }

    Presses presses(Lights source, Lights target, boolean fewest) {
        if (source.equals(target)) {
            // workaround in case pushing all the buttons does nothing
            return Presses.ZERO;
        }
        var buttonMasks = new HashSet<Integer>();
        int min = -1;
        var visited = new HashSet<Integer>();
        var queue = new PriorityQueue<ND>();
        queue.add(new ND(source, 0, 0, null));
        do {
            var current = queue.remove();
            if (current.lights().equals(target)) {
                buttonMasks.add(current.buttonMask());
                if (min == -1) {
                    min = current.distance();
                }
                if (fewest) {
                    break;
                }
            }
            for (int i = 0; i < buttons.size(); i++) {
                var button = buttons.get(i);
                var next = current.lights().toggle(button);
                var nextNode = new ND(next, current.buttonMask() | 1 << i, current.distance() + 1, current);
                if (!visited.contains(nextNode.buttonMask())) {
                    visited.add(nextNode.buttonMask());
                    queue.add(nextNode);
                }
            }
        } while (!queue.isEmpty());
        return new Presses(min, buttonMasks);
    }

    int oneBits(int buttonMask) {
        int n = 0;
        for (int i = 0; i < buttons.size(); i++) {
            if ((buttonMask & 1 << i) != 0) {
                n++;
            }
        }
        return n;
    }

    int fewestPresses2() {
        return fewestPresses2(joltages);
    }

    Integer fewestPresses2(List<Integer> adjustedJoltages) {
        if (adjustedJoltages.stream().allMatch(i -> i == 0)) {
            return 0;
        }
        Integer min = null;
        var buttonMasks = presses(new Lights(0, adjustedJoltages.size()), lsb(adjustedJoltages), false).buttonMasks();
        for (var buttonMask : buttonMasks) {
            var next = operate(buttonMask, adjustedJoltages);
            if (next != null) {
                var nextPresses = fewestPresses2(next);
                if (nextPresses != null) {
                    var candidate = (nextPresses << 1) + oneBits(buttonMask);
                    if (min == null || candidate < min) {
                        min = candidate;
                    }
                }
            }
        }
        return min;
    }

    static Lights lsb(List<Integer> joltages) {
        var z = new char[joltages.size()];
        for (int i = 0; i < joltages.size(); i++) {
            z[i] = (joltages.get(i) & 1) == 0 ? '.' : '#';
        }
        return Lights.parse(new String(z));
    }
}

record Presses(int fewest, Set<Integer> buttonMasks) {
    static final Presses ZERO = new Presses(0, Set.of(0));
}

record ND(Lights lights, int buttonMask, int distance, ND previous) implements Comparable<ND> {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ND nd = (ND) o;
        return distance == nd.distance && buttonMask == nd.buttonMask && Objects.equals(lights, nd.lights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lights, buttonMask, distance);
    }

    @Override
    public int compareTo(ND o) {
        int d = Integer.compare(distance, o.distance);
        if (d != 0) {
            return d;
        }
        d = Integer.compare(buttonMask, o.buttonMask);
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

    boolean isSet(int i) {
        return (bitmap & (1 << (size - 1 - i))) != 0;
    }

    @Override
    public String toString() {
        return "(" + IntStream.range(0, Integer.SIZE).filter(this::isSet).mapToObj(String::valueOf).collect(Collectors.joining(",")) + ")";
    }
}
