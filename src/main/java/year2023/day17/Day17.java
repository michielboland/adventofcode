package year2023.day17;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

enum Heading {
    NORTH(Coordinate::north), SOUTH(Coordinate::south), EAST(Coordinate::east), WEST(Coordinate::west), NOWHERE(Function.identity());
    final Function<Coordinate, Coordinate> nextFunction;

    Heading(Function<Coordinate, Coordinate> nextFunction) {
        this.nextFunction = nextFunction;
    }

    Set<Heading> next() {
        return switch (this) {
            case NORTH, SOUTH -> Set.of(EAST, WEST);
            case EAST, WEST -> Set.of(NORTH, SOUTH);
            case NOWHERE -> Set.of(NORTH, SOUTH, EAST, WEST);
        };
    }
}

public class Day17 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(false);
        new Puzzle().solve(true);
    }
}

record Coordinate(int x, int y) implements Comparable<Coordinate> {
    Coordinate north() {
        return new Coordinate(x, y - 1);
    }

    Coordinate south() {
        return new Coordinate(x, y + 1);
    }

    Coordinate west() {
        return new Coordinate(x - 1, y);
    }

    Coordinate east() {
        return new Coordinate(x + 1, y);
    }

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

class Node implements Comparable<Node> {
    final Coordinate coordinate;
    final int heatLoss;
    final Map<Heading, Integer> distanceMap = new HashMap<>();

    Node(Coordinate coordinate, int heatLoss) {
        this.coordinate = coordinate;
        this.heatLoss = heatLoss;
    }

    @Override
    public int compareTo(Node o) {
        return coordinate.compareTo(o.coordinate);
    }
}

record Target(Node node, Heading heading, int distance) implements Comparable<Target> {
    @Override
    public int compareTo(Target o) {
        int d = Integer.compare(distance, o.distance);
        if (d != 0) {
            return d;
        }
        int c = node.compareTo(o.node);
        if (c != 0) {
            return c;
        }
        return Integer.compare(heading.ordinal(), o.heading.ordinal());
    }
}

record Grid(Map<Coordinate, Node> nodeMap, Queue<Target> queue, Node initial, Node destination, boolean ultra) {
    static void add(int heatLoss, Map<Coordinate, Node> NodeMap, int x, int y) {
        Coordinate coordinate = new Coordinate(x, y);
        NodeMap.put(coordinate, new Node(coordinate, heatLoss));
    }

    static void parse(String line, Map<Coordinate, Node> NodeMap, int y) {
        var xc = new AtomicInteger();
        line.chars().forEach(c -> add(c - '0', NodeMap, xc.getAndIncrement(), y));
    }

    static Grid parse(Stream<String> lines, boolean ultra) {
        SortedMap<Coordinate, Node> nodeMap = new TreeMap<>();
        var yc = new AtomicInteger();
        lines.forEach(line -> parse(line, nodeMap, yc.getAndIncrement()));
        return new Grid(nodeMap, new PriorityQueue<>(), nodeMap.get(nodeMap.firstKey()), nodeMap.get(nodeMap.lastKey()), ultra);
    }

    void process(Target target) {
        Node current = target.node();
        for (Heading next : target.heading().next()) {
            int heatLoss = 0;
            Node node = target.node();
            for (int i = 0; i < 3 || ultra && i < 10; i++) {
                node = nodeMap.get(next.nextFunction.apply(node.coordinate));
                if (node == null) {
                    break;
                }
                heatLoss += node.heatLoss;
                if (i < 3 && ultra) {
                    continue;
                }
                int distanceToNeighbor = current.distanceMap.get(target.heading()) + heatLoss;
                if (!node.distanceMap.containsKey(next) || distanceToNeighbor < node.distanceMap.get(next)) {
                    node.distanceMap.put(next, distanceToNeighbor);
                    queue.add(new Target(node, next, distanceToNeighbor));
                }
            }
        }
    }

    int solve() {
        initial.distanceMap.put(Heading.NOWHERE, 0);
        queue.add(new Target(initial, Heading.NOWHERE, 0));
        do {
            Target currentTarget = queue.remove();
            Node current = currentTarget.node();
            if (current.distanceMap.isEmpty()) {
                throw new IllegalStateException();
            }
            if (current == destination) {
                return current.distanceMap.values().stream().mapToInt(i -> i).min().orElseThrow();
            }
            process(currentTarget);
        } while (true);
    }
}

class Puzzle {
    void solve(boolean ultra) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day17/day17_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var grid = Grid.parse(reader.lines(), ultra);
            System.out.println(grid.solve());
        }
    }
}
