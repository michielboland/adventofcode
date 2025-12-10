package year2025.day10;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Puzzle {

    private final List<Machine> machines = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day10_input")))).lines().map(Machine::parse).toList();

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
    }
}

record Machine(Lights initial, Lights desired, List<Button> buttons, List<Integer> joltages) {
    private static final Pattern PATTERN = Pattern.compile("\\[(.*)] (.*) \\{(.*)}");

    static Machine parse(String line) {
        var matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        var desired = Lights.parse(matcher.group(1));
        var initial = new Lights(0, desired.size());
        return new Machine(
                initial,
                desired,
                Arrays.stream(matcher.group(2).split(" ")).map(Button::parse).toList(),
                Arrays.stream(matcher.group(3).split(",")).map(Integer::valueOf).toList()
        );
    }
}

record Lights(int bitmap, int size) {
    static Lights parse(String symbols) {
        return new Lights(Integer.parseInt(symbols.replace('.', '0').replace('#', '1'), 2), symbols.length());
    }
}

record Button(int bitmap) {
    static Button parse(String s) {
        return new Button(Arrays.stream(s.replaceAll("[()]", "").split(",")).mapToInt(Integer::parseInt).map(i -> 1 << i).reduce(0, (a, b) -> a | b));
    }
}
