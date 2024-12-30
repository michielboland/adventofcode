package year2023.day25;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record Apparatus(Map<String, Set<String>> connections) {
    static Apparatus parse(Stream<String> lines) {
        var apparatus = new Apparatus(new HashMap<>());
        lines.forEach(apparatus::parse);
        return apparatus;
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

    Set<Set<String>> edgesFrom(String node) {
        Set<Set<String>> edges = new HashSet<>();
        var queue = new PriorityQueue<ND>();
        Set<String> visited = new HashSet<>();
        queue.add(new ND(node, 0, null));
        while (!queue.isEmpty()) {
            var current = queue.remove();
            if (!visited.contains(current.node)) {
                visited.add(current.node);
                if (current.previous == null) {
                    edges.add(Set.of(current.node));
                } else {
                    edges.add(Set.of(current.node, current.previous.node));
                }
            }
            for (String s : connections.get(current.node)) {
                if (!visited.contains(s)) {
                    queue.add(new ND(s, current.distance + 1, current));
                }
            }
        }
        return edges;
    }

    void solve() {
        var top3 = connections.keySet().stream()
                .flatMap(s1 -> edgesFrom(s1).stream())
                .collect(Collectors.groupingBy(s1 -> s1, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Set<String>, Long>comparingByValue().reversed())
                .limit(3L)
                .map(Map.Entry::getKey)
                .toList();
        for (var s : top3) {
            var pair = new TreeSet<>(s);
            removeConnection(pair.first(), pair.last());
        }
        var topEdge = new TreeSet<>(top3.get(0));
        System.out.println(edgesFrom(topEdge.first()).size() * edgesFrom(topEdge.last()).size());
    }

    record ND(String node, int distance, ND previous) implements Comparable<ND> {

        @Override
        public int compareTo(ND o) {
            int d = Integer.compare(distance, o.distance);
            return d != 0 ? d : node.compareTo(o.node);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ND nd = (ND) o;
            return distance == nd.distance && Objects.equals(node, nd.node);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, distance);
        }

        @Override
        public String toString() {
            return "ND{node='" + node + "', distance=" + distance + '}';
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
