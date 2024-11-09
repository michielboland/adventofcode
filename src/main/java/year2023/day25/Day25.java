package year2023.day25;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record Apparatus(Map<String, Set<String>> connections) {
    static Apparatus parse(Stream<String> lines) {
        var apparatus = new Apparatus(new HashMap<>());
        lines.forEach(apparatus::parse);
        return apparatus;
    }

    static String combine(String a, String b) {
        return a.compareTo(b) < 0 ? a + " " + b : b + " " + a;
    }

    void link(String from, String to) {
        connections.computeIfAbsent(from, z -> new HashSet<>()).add(to);
    }

    void unlink(String from, String to) {
        connections.computeIfPresent(from, (k, v) -> {
            v.remove(to);
            return v;
        });
    }

    void addConnection(String a, String b) {
        link(a, b);
        link(b, a);
    }

    void removeConnection(String a, String b) {
        unlink(a, b);
        unlink(b, a);
    }

    void parse(String line) {
        var lr = line.split(": ");
        var a = lr[0];
        Arrays.stream(lr[1].split(" ")).forEach(b -> addConnection(a, b));
    }

    Map<String, ND> dijkstra(String node) {
        var queue = new PriorityQueue<ND>();
        Set<String> visited = new HashSet<>();
        Map<String, ND> distanceMap = new HashMap<>();
        queue.add(new ND(node, 0));
        while (!queue.isEmpty()) {
            var current = queue.remove();
            visited.add(current.node);
            connections.get(current.node).stream()
                    .filter(s -> !visited.contains(s))
                    .filter(s -> !distanceMap.containsKey(s) || distanceMap.get(s).distance > current.distance + 1)
                    .forEach(s -> {
                        distanceMap.put(s, new ND(current.node, current.distance + 1));
                        queue.add(new ND(s, current.distance + 1));
                    });
        }
        return distanceMap;
    }

    void solve() {
        String randomNode = connections.keySet().stream().findFirst().orElseThrow();
        var totalSize = dijkstra(randomNode).size();
        var allNodes = connections.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        record PathComponent(String label, AtomicInteger counter) implements Comparable<PathComponent> {
            @Override
            public int compareTo(PathComponent o) {
                var c = Integer.compare(counter.get(), o.counter.get());
                return c != 0 ? c : label.compareTo(o.label);
            }
        }
        Map<String, PathComponent> pathComponents = new HashMap<>();
        allNodes.forEach(s -> dijkstra(s).forEach((k, v) -> {
            var label = combine(k, v.node);
            pathComponents.computeIfAbsent(label, t -> new PathComponent(t, new AtomicInteger())).counter.incrementAndGet();
        }));
        pathComponents.values().stream().sorted(Comparator.reverseOrder()).limit(3L).map(PathComponent::label).map(s -> s.split(" ")).forEach(s -> removeConnection(s[0], s[1]));
        var reducedSize = dijkstra(randomNode).size();
        System.out.println(reducedSize * (totalSize - reducedSize));
    }

    record ND(String node, int distance) implements Comparable<ND> {

        @Override
        public int compareTo(ND o) {
            int d = Integer.compare(distance, o.distance);
            return d != 0 ? d : node.compareTo(o.node);
        }
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day25/day25_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var apparatus = Apparatus.parse(reader.lines());
            apparatus.solve();
        }
    }
}

public class Day25 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}
