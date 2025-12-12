package year2025.day11;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class Puzzle {

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
    }

    private int part1() {
        var queue = new PriorityQueue<ND>();
        queue.add(new ND("you", 0, null));
        int paths = 0;
        do {
            var current = queue.remove();
            if (current.device().equals("out")) {
                paths++;
            } else {
                for (var next : devices.get(current.device())) {
                    queue.add(new ND(next, current.distance() + 1, current));
                }
            }
        } while (!queue.isEmpty());
        return paths;
    }
}

record ND(String device, int distance, ND previous) implements Comparable<ND> {
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

    @Override
    public int compareTo(ND o) {
        int d = Integer.compare(distance, o.distance);
        if (d != 0) {
            return d;
        }
        return device.compareTo(o.device);
    }
}
