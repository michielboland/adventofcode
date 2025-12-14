package year2025.day10;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

record Machine(Lights desired, Button[] buttons, int[] joltages, Map<Integer, List<Integer>> cache) {
    private static final Pattern PATTERN = Pattern.compile("\\[(.*)] (.*) \\{(.*)}");
    private static final List<Integer> ZEROLIST = List.of(0);

    static Machine parse(String line) {
        var matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        var desired = Lights.parse(matcher.group(1));
        return new Machine(
                desired,
                Button.parse(matcher.group(2).split(" "), desired.size()),
                Arrays.stream(matcher.group(3).split(",")).mapToInt(Integer::parseInt).toArray(),
                new HashMap<>()
        );
    }

    int[] operate(int buttonMask, final int[] originalJoltages) {
        var a = Arrays.stream(originalJoltages).toArray();
        System.arraycopy(originalJoltages, 0, a, 0, originalJoltages.length);
        for (int i = 0; i < buttons.length; i++) {
            if ((buttonMask & 1 << i) != 0) {
                var button = buttons[i];
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
                }).toArray();
    }

    @Override
    public String toString() {
        return "[" + desired + "] "
                + Arrays.stream(buttons).map(String::valueOf).collect(Collectors.joining(" "))
                + " {" + Arrays.stream(joltages).mapToObj(String::valueOf).collect(Collectors.joining(",")) + "}";
    }

    int fewestPresses() {
        return Objects.requireNonNull(presses(desired.bitmap())).stream().mapToInt(this::oneBits).min().orElseThrow();
    }

    List<Integer> presses(int target) {
        if (target == 0) {
            // workaround in case pushing all the buttons does nothing
            return ZEROLIST;
        }
        {
            var cached = cache.get(target);
            if (cached != null) {
                return cached;
            }
        }
        var buttonMasks = new ArrayList<Integer>();
        final var max = 1 << buttons.length;
        for (int buttonMask = 1; buttonMask < max; buttonMask++) {
            if (toggle(buttonMask) == target) {
                buttonMasks.add(buttonMask);
            }
        }
        cache.put(target, buttonMasks);
        if (buttonMasks.isEmpty()) {
            return null;
        }
        return buttonMasks;
    }

    int oneBits(int buttonMask) {
        int n = 0;
        for (int i = 0; i < buttons.length; i++) {
            if ((buttonMask & 1 << i) != 0) {
                n++;
            }
        }
        return n;
    }

    int toggle(int buttonMask) {
        int n = 0;
        for (int i = 0; i < buttons.length; i++) {
            if ((buttonMask & 1 << i) != 0) {
                n ^= buttons[i].bitmap();
            }
        }
        return n;
    }

    int fewestPresses2() {
        return Objects.requireNonNull(fewestPresses2(joltages));
    }

    Integer fewestPresses2(final int[] adjustedJoltages) {
        if (Arrays.stream(adjustedJoltages).allMatch(i -> i == 0)) {
            return 0;
        }
        Integer min = null;
        var buttonMasks = presses(lsb(adjustedJoltages));
        if (buttonMasks == null) {
            return null;
        }
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

    static int lsb(int[] joltages) {
        var z = new char[joltages.length];
        for (int i = 0; i < joltages.length; i++) {
            z[i] = (joltages[i] & 1) == 0 ? '.' : '#';
        }
        return Lights.parse(new String(z)).bitmap();
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
}

record Button(int bitmap, int size) {
    static Button[] parse(String[] s, int size) {
        return Arrays.stream(s).map(string -> parse(string, size)).toArray(Button[]::new);
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
