package year2022.day16;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Puzzle {
    private static final Pattern PATTERN = Pattern.compile("Valve (.+) has flow rate=(\\d+); tunnels? leads? to valves? (.+)");

    final Map<String, Integer> valveMap = new HashMap<>();
    final Map<String, Collection<String>> neighbourMap = new HashMap<>();

    Puzzle() throws Exception {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day16_input"))))) {
            for (String line : reader.lines().toList()) {
                final var matcher = PATTERN.matcher(line);
                if (!matcher.matches()) {
                    throw new IllegalArgumentException(line);
                }
                final var name = matcher.group(1);
                valveMap.put(name, Integer.parseInt(matcher.group(2)));
                neighbourMap.put(name, Arrays.stream(matcher.group(3).split(", ")).collect(Collectors.toSet()));
            }
        }
        neighbourMap.forEach((k, v) -> v.forEach(l -> {
            if (!neighbourMap.get(l).contains(k)) {
                throw new IllegalStateException();
            }
        }));
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(max());
    }

    int max() {
        int max = 0;
        int minutes = 30;
        Queue<ND> queue = new ArrayDeque<>();
        Set<State> visited = new HashSet<>();
        queue.add(ND.INITIAL);
        while (!queue.isEmpty()) {
            final var current = queue.remove();
            if (current.minute() == minutes) {
                int totalPressure = current.totalPressure();
                if (totalPressure > max) {
                    max = totalPressure;
                }
                continue;
            }
            final var valve = current.state().valve();
            final var openValves = current.state().openValves();
            boolean canMove = false;
            for (String neighbour : neighbourMap.get(valve)) {
                State newState = new State(neighbour, openValves);
                if (!visited.contains(newState)) {
                    canMove = true;
                    visited.add(newState);
                    queue.add(current.extend(newState, 0));
                }
            }
            final var flowRate = valveMap.get(valve);
            if (flowRate > 0 && !openValves.contains(valve)) {
                final var newOpenValves = openValves + valve;
                State newState = new State(valve, newOpenValves);
                if (!visited.contains(newState)) {
                    canMove = true;
                    visited.add(newState);
                    queue.add(current.extend(newState, flowRate));
                }
            }
            if (!canMove) {
                int totalPressure = current.totalPressure() + current.pressure() * (minutes - current.minute());
                if (totalPressure > max) {
                    max = totalPressure;
                }
            }
        }
        return max;
    }

}

record State(String valve, String openValves) {
    static final State INITIAL = new State("AA", "");
}

record ND(State state, int minute, int pressure, int totalPressure) {
    static final ND INITIAL = new ND(State.INITIAL, 1, 0, 0);

    ND extend(State state, int flowRate) {
        return new ND(state, minute + 1, pressure + flowRate, totalPressure + pressure + flowRate);
    }
}
