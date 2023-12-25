package day25;

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

    int dijkstra(String node) {
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
        return visited.size();
    }

    void solve() {
        String randomNode = connections.keySet().stream().findFirst().orElseThrow();
        var totalSize = dijkstra(randomNode);
        CutThese.LINKS.forEach(this::removeConnection);
        var reducedSize = dijkstra(randomNode);
        System.out.println(reducedSize * (totalSize - reducedSize));
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("graph day25 {").append(System.lineSeparator());
        connections.forEach((key, value) -> value.stream().filter(b -> key.compareTo(b) < 0).forEach(b -> sb.append("  ").append(key).append(" -- ").append(b).append(";").append(System.lineSeparator())));
        sb.append("}");
        return sb.toString();
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
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day25/day25_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var apparatus = Apparatus.parse(reader.lines());
            //System.out.println(apparatus);
            apparatus.solve();
        }
    }
}

public class Day25 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}
