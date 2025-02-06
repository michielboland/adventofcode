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
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Puzzle {
    private static final Pattern PATTERN = Pattern.compile("Valve (.+) has flow rate=(\\d+); tunnels? leads? to valves? (.+)");
    public static final String INITIAL_VALVE = "AA";

    final Map<String, Integer> valveMap = new HashMap<>();
    final Map<String, Collection<String>> neighbourMap = new HashMap<>();
    final Map<String, Map<String, Integer>> valveDistances = new HashMap<>();
    final Map<String, Long> valveBits = new HashMap<>();

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
        valveDistances.put(INITIAL_VALVE, distanceMap(INITIAL_VALVE));
        final var bit = new Bit();
        valveMap.forEach((valve, flowRate) -> {
            if (flowRate >= 0) {
                valveDistances.put(valve, distanceMap(valve));
                valveBits.put(valve, bit.value);
                bit.shift();
            }
        });
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(max(30, false));
        System.out.println(max(26, true));
    }

    Map<String, Integer> distanceMap(String origin) {
        Map<String, Integer> distanceMap = new HashMap<>();
        var queue = new PriorityQueue<ValveDistance>();
        Set<String> visited = new HashSet<>();
        queue.add(new ValveDistance(origin, 0));
        while (!queue.isEmpty()) {
            var current = queue.remove();
            if (valveMap.get(current.valve()) > 0) {
                // Add one for turning on the valve
                distanceMap.put(current.valve(), current.distance() + 1);
            }
            for (String neighbour : neighbourMap.get(current.valve())) {
                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(new ValveDistance(neighbour, current.distance() + 1));
                }
            }
        }
        return distanceMap;
    }

    int max(int minutes, boolean elephant) {
        Map<Long, Integer> maxPressures = new HashMap<>();
        Queue<ND> queue = new ArrayDeque<>();
        queue.add(ND.INITIAL);
        while (!queue.isEmpty()) {
            final var current = queue.remove();
            int totalPressure = current.finalTotalPressure(minutes);
            final var valve = current.state().valve();
            final var openValveBitmap = current.state().openValveBitmap();
            var maxForValves = maxPressures.get(openValveBitmap);
            if (maxForValves == null || totalPressure > maxForValves) {
                maxPressures.put(openValveBitmap, totalPressure);
            }
            final var flowRate = valveMap.get(valve);
            valveDistances.get(valve).forEach((newValve, distance) -> {
                if (current.minute() + distance < minutes) {
                    var bit = valveBits.get(newValve);
                    if ((bit & openValveBitmap) == 0) {
                        queue.add(current.extend(current.state().extend(newValve, bit), flowRate, distance, valveMap.get(newValve)));
                    }
                }
            });
        }
        if (elephant) {
            Map<Long, Integer> combinedPressures = new HashMap<>();
            maxPressures.forEach((myValves, myPressure) -> maxPressures.forEach((elephantValves, elephantPressure) -> {
                if (myValves < elephantValves && (myValves & elephantValves) == 0) {
                    var combinedValves = myValves | elephantValves;
                    var combinedPressure = myPressure + elephantPressure;
                    var maxForCombinedValves = combinedPressures.get(combinedValves);
                    if (maxForCombinedValves == null || combinedPressure > maxForCombinedValves) {
                        combinedPressures.put(combinedValves, combinedPressure);
                    }
                }
            }));
            return combinedPressures.values().stream().max(Integer::compareTo).orElseThrow();
        } else {
            return maxPressures.values().stream().max(Integer::compareTo).orElseThrow();
        }
    }
}

class Bit {
    long value = 1L;

    void shift() {
        value <<= 1;
    }
}

record ValveDistance(String valve, int distance) implements Comparable<ValveDistance> {

    @Override
    public int compareTo(ValveDistance o) {
        return distance == o.distance ? valve.compareTo(o.valve) : Integer.compare(distance, o.distance);
    }
}

record State(String valve, long openValveBitmap) {
    static final State INITIAL = new State(Puzzle.INITIAL_VALVE, 0L);

    State extend(String valve, long bit) {
        return new State(valve, openValveBitmap | bit);
    }
}

record ND(State state, int minute, int pressure, int totalPressure, int finalPressure) {
    static final ND INITIAL = new ND(State.INITIAL, 0, 0, 0, 0);

    ND extend(State state, int flowRate, int distance, int newFlowRate) {
        return new ND(state, minute + distance, pressure + flowRate, totalPressure + (pressure + flowRate) * distance, pressure + flowRate + newFlowRate);
    }

    int finalTotalPressure(int minutes) {
        return totalPressure + finalPressure * (minutes - minute);
    }
}
