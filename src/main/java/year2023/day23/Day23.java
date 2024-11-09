package year2023.day23;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Heading {
    EAST(Coordinate::east), SOUTH(Coordinate::south), WEST(Coordinate::west), NORTH(Coordinate::north), NOWHERE(Function.identity());
    final Function<Coordinate, Coordinate> mover;

    Heading(Function<Coordinate, Coordinate> mover) {
        this.mover = mover;
    }

    static Heading from(int c) {
        return switch (c) {
            case '^' -> NORTH;
            case 'v' -> SOUTH;
            case '<' -> WEST;
            case '>' -> EAST;
            default -> throw new IllegalArgumentException();
        };
    }

    Set<Heading> next() {
        return switch (this) {
            case NORTH -> Set.of(NORTH, EAST, WEST);
            case SOUTH -> Set.of(SOUTH, EAST, WEST);
            case EAST -> Set.of(EAST, NORTH, SOUTH);
            case WEST -> Set.of(WEST, NORTH, SOUTH);
            case NOWHERE -> Set.of(NORTH, SOUTH, EAST, WEST);
        };
    }
}

record Coordinate(long x, long y) implements Comparable<Coordinate> {
    Coordinate north() {
        return new Coordinate(x, y - 1L);
    }

    Coordinate south() {
        return new Coordinate(x, y + 1L);
    }

    Coordinate west() {
        return new Coordinate(x - 1L, y);
    }

    Coordinate east() {
        return new Coordinate(x + 1L, y);
    }

    private long[] toArray() {
        return new long[]{y, x};
    }

    @Override
    public int compareTo(Coordinate o) {
        return Arrays.compare(toArray(), o.toArray());
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}

record TrailMap(Set<Coordinate> paths, Map<Coordinate, Heading> slopes, Coordinate start, Coordinate end,
                Set<Coordinate> nodes) {
    static TrailMap parse(Stream<String> lines) {
        var paths = new TreeSet<Coordinate>();
        var slopes = new TreeMap<Coordinate, Heading>();
        var yc = new AtomicLong();
        lines.forEach(l -> {
            var y = yc.getAndIncrement();
            var xc = new AtomicLong();
            l.chars().forEach(ch -> {
                var x = xc.getAndIncrement();
                if (ch != '#') {
                    var c = new Coordinate(x, y);
                    paths.add(c);
                    if (ch != '.') {
                        slopes.put(c, Heading.from(ch));
                    }
                }
            });
        });
        return new TrailMap(paths, slopes, paths.first(), paths.last(), new TreeSet<>());
    }

    long walk() {
        var queue = new LinkedList<CH>();
        queue.add(new CH(start, Heading.NOWHERE, 0L));
        var distances = new TreeSet<Long>();
        while (!queue.isEmpty()) {
            var ch = queue.removeFirst();
            if (ch.coordinate.equals(end)) {
                distances.add(ch.steps);
            }
            queue.addAll(ch.next()
                    .filter(nch -> paths.contains(nch.coordinate))
                    .filter(nch -> !slopes.containsKey(nch.coordinate) || slopes.get(nch.coordinate) == nch.heading)
                    .collect(Collectors.toSet()));
        }
        return distances.last();
    }

    void findNodes() {
        nodes.add(start);
        nodes.add(end);
        var visited = new TreeSet<Coordinate>();
        var queue = new LinkedList<CH>();
        queue.add(new CH(start, Heading.NOWHERE, 0L));
        while (!queue.isEmpty()) {
            var ch = queue.removeFirst();
            visited.add(ch.coordinate);
            var next = ch.next()
                    .filter(nch -> paths.contains(nch.coordinate))
                    .filter(nch -> !visited.contains(nch.coordinate))
                    .collect(Collectors.toSet());
            if (next.size() > 1) {
                nodes.add(ch.coordinate);
            }
            queue.addAll(next);
        }
    }

    long directDistanceBetweenNodes(Coordinate from, Coordinate to) {
        var queue = new LinkedList<CH>();
        var visited = new TreeSet<Coordinate>();
        queue.add(new CH(from, Heading.NOWHERE, 0L));
        while (!queue.isEmpty()) {
            var ch = queue.removeFirst();
            visited.add(ch.coordinate);
            if (ch.coordinate.equals(to)) {
                return ch.steps;
            }
            queue.addAll(ch.next()
                    .filter(nch -> paths.contains(nch.coordinate))
                    .filter(nch -> !visited.contains(nch.coordinate))
                    .filter(nch -> nch.coordinate.equals(to) || !nodes.contains(nch.coordinate))
                    .collect(Collectors.toSet()));
        }
        return Long.MAX_VALUE;
    }

    record NodeMap(Map<Coordinate, Map<Coordinate, Long>> map) {
        NodeMap() {
            this(new TreeMap<>());
        }

        void add(Coordinate from, Coordinate to, long distance) {
            map.computeIfAbsent(from, l -> new TreeMap<>()).put(to, distance);
            map.computeIfAbsent(to, l -> new TreeMap<>()).put(from, distance);
        }

        SortedSet<Long> distancesToEnd(Coordinate start, Coordinate end) {
            return distancesToEnd(start, end, new TreeSet<>());
        }

        private SortedSet<Long> distancesToEnd(Coordinate node, Coordinate end, Set<Coordinate> visited) {
            Set<Coordinate> neighbors = new TreeSet<>(map.get(node).keySet());
            neighbors.removeAll(visited);
            SortedSet<Long> distances = new TreeSet<>();
            Set<Coordinate> newVisited = new TreeSet<>(visited);
            newVisited.add(node);
            if (neighbors.isEmpty()) {
                if (node.equals(end)) {
                    distances.add(0L);
                }
            } else {
                for (var neighbor : neighbors) {
                    var distanceToNeighbor = map.get(node).get(neighbor);
                    distances.addAll(distancesToEnd(neighbor, end, newVisited).stream().map(d -> d + distanceToNeighbor).collect(Collectors.toSet()));
                }
            }
            return distances;
        }
    }

    long walkDry() {
        findNodes();
        var queue = new LinkedList<Coordinate>();
        queue.add(start);
        Set<Coordinate> workingSet = new TreeSet<>(nodes);
        var nodeMap = new NodeMap();
        while (!queue.isEmpty()) {
            var from = queue.removeFirst();
            workingSet.remove(from);
            for (Coordinate to : workingSet) {
                long d = directDistanceBetweenNodes(from, to);
                if (d != Long.MAX_VALUE) {
                    nodeMap.add(from, to, d);
                    queue.add(to);
                }
            }
        }
        return nodeMap.distancesToEnd(start, end).last();
    }

    record CH(Coordinate coordinate, Heading heading, long steps) {
        Stream<CH> next() {
            return heading.next().stream().map(h -> new CH(h.mover.apply(coordinate), h, steps + 1L));
        }
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day23/day23_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var trailMap = TrailMap.parse(reader.lines());
            System.out.println(trailMap.walk());
            System.out.println(trailMap.walkDry());
        }
    }
}

public class Day23 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}
