package year2025.day11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Puzzle {

    static final String SVR = "svr";
    static final String FFT = "fft";
    static final String DAC = "dac";
    static final String OUT = "out";
    static final String YOU = "you";
    static final Set<String> REQUIRED = Set.of(FFT, DAC);

    private final Map<String, List<String>> devices = new HashMap<>();

    Puzzle() {
        new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day11_input")))).lines().forEach(s -> {
            var parts = s.split(": ");
            var label = parts[0];
            var outputs = parts[1].split(" ");
            devices.put(label, Arrays.stream(outputs).toList());
        });
    }

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
        System.out.println(part2());
    }

    @SuppressWarnings("unused")
    void graphviz() {
        System.out.println("digraph test {");
        for (String s : devices.keySet()) {
            for (String t : devices.get(s)) {
                System.out.println("  " + s + " -> " + t);
            }
        }
        System.out.println("}");
    }

    private int part1() {
        return paths(YOU, OUT, Set.of());
    }

    private long part2() {
        var routes = routes();
        var queue = new ArrayDeque<ND>();
        long total = 0;
        queue.add(new ND(SVR, 0, null));
        do {
            var current = queue.remove();
            if (current.device().equals(OUT)) {
                total += examine(current);
            }
            for (var route : routes) {
                if (route.from().equals(current.device())) {
                    queue.add(new ND(route.to(), route.paths(), current));
                }
            }
        } while (!queue.isEmpty());
        return total;
    }

    static long examine(ND nd) {
        long n = 1;
        var devices = new HashSet<String>();
        do {
            devices.add(nd.device());
            if (nd.distance() != 0) {
                n *= nd.distance();
            }
            nd = nd.previous();
        } while (nd != null);
        if (devices.containsAll(REQUIRED)) {
            return n;
        }
        return 0;
    }

    private List<Route> routes() {
        Map<String, Integer> reverse = new HashMap<>();
        for (String from : devices.keySet()) {
            for (String to : devices.get(from)) {
                reverse.compute(to, (a, b) -> b == null ? 1 : b + 1);
            }
        }
        var all = reverse.entrySet().stream()
                .filter(e -> e.getValue() > 7)
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
        all.addAll(Set.of(SVR, FFT, DAC, OUT));
        var routes = new ArrayList<Route>();
        for (var from : all) {
            for (var to : all) {
                var p = paths(from, to, all);
                if (p > 0) {
                    routes.add(new Route(from, to, p));
                }
            }
        }
        return routes;
    }

    private int paths(String from, String to, Set<String> avoid) {
        if (from.equals(to) || !devices.containsKey(from)) {
            return 0;
        }
        var queue = new ArrayDeque<ND>();
        queue.add(new ND(from, 0, null));
        int paths = 0;
        do {
            var current = queue.remove();
            var device = current.device();
            for (var next : devices.get(device)) {
                if (next.equals(to)) {
                    paths++;
                } else if (!avoid.contains(next)) {
                    queue.add(new ND(next, current.distance() + 1, current));
                }
            }
        } while (!queue.isEmpty());
        return paths;
    }
}

record ND(String device, int distance, ND previous) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ND nd = (ND) o;
        return distance == nd.distance && Objects.equals(device, nd.device);
    }

    @Override
    public int hashCode() {
        return Objects.hash(device, distance);
    }
}

record Route(String from, String to, int paths) {
}
