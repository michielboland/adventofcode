package year2024.day16;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

enum Heading {
    EAST(Coordinate::east), SOUTH(Coordinate::south), WEST(Coordinate::west), NORTH(Coordinate::north);
    final Function<Coordinate, Coordinate> mover;

    Heading(Function<Coordinate, Coordinate> mover) {
        this.mover = mover;
    }

    Collection<Heading> rotate() {
        return switch (this) {
            case EAST, WEST -> Set.of(NORTH, SOUTH);
            case NORTH, SOUTH -> Set.of(EAST, WEST);
        };
    }
}

public class Day16 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Grid grid;

    Puzzle() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day16_input"))) {
            grid = Grid.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        System.out.println(grid.shortestPath());
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

    Coordinate move(Heading heading) {
        return heading.mover.apply(this);
    }

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }
}

record Reindeer(Coordinate position, Heading heading) implements Comparable<Reindeer> {
    @Override
    public int compareTo(Reindeer o) {
        return position.equals(o.position) ? Integer.compare(heading.ordinal(), o.heading.ordinal()) : position.compareTo(o.position);
    }
}

record BestPaths(int lowestScore, int viewingSpots) {
}

record Grid(Set<Coordinate> walls, Coordinate start, Coordinate end) {
    static Grid from(Stream<String> lines) {
        Set<Coordinate> walls = new HashSet<>();
        Coordinate start = null;
        Coordinate end = null;
        int y = 0;
        for (String line : lines.toList()) {
            int x = 0;
            for (char c : line.toCharArray()) {
                switch (c) {
                    case '#' -> walls.add(new Coordinate(x, y));
                    case 'S' -> start = new Coordinate(x, y);
                    case 'E' -> end = new Coordinate(x, y);
                }
                x++;
            }
            y++;
        }
        return new Grid(Set.copyOf(walls), Objects.requireNonNull(start), Objects.requireNonNull(end));
    }

    Collection<ND> neighbours(ND nd) {
        Collection<ND> neighbours = new HashSet<>();
        Reindeer reindeer = nd.node;
        Heading currentHeading = reindeer.heading();
        for (Heading heading : currentHeading.rotate()) {
            neighbours.add(new ND(new Reindeer(reindeer.position(), heading), nd.distance + 1000, nd));
        }
        var nextPosition = reindeer.position().move(currentHeading);
        if (!walls.contains(nextPosition)) {
            neighbours.add(new ND(new Reindeer(nextPosition, currentHeading), nd.distance + 1, nd));
        }
        return neighbours;
    }

    BestPaths shortestPath() {
        int shortest = -1;
        var queue = new PriorityQueue<ND>();
        Set<Coordinate> viewingSpots = new HashSet<>();
        Set<Reindeer> visited = new HashSet<>();
        queue.add(new ND(new Reindeer(start, Heading.EAST), 0, null));
        while (!queue.isEmpty()) {
            var current = queue.remove();
            if (current.node.position().equals(end)) {
                if (shortest == -1) {
                    shortest = current.distance;
                }
                if (shortest == current.distance) {
                    viewingSpots.addAll(current.path());
                }
            }
            visited.add(current.node);
            for (var next : neighbours(current)) {
                if (!visited.contains(next.node)) {
                    queue.add(next);
                }
            }
        }
        return new BestPaths(shortest, viewingSpots.size());
    }

    record ND(Reindeer node, int distance, ND previous) implements Comparable<ND> {

        @Override
        public int compareTo(ND o) {
            return distance == o.distance ? node.compareTo(o.node) : Integer.compare(distance, o.distance);
        }

        Collection<Coordinate> path() {
            Collection<Coordinate> path = new HashSet<>();
            var nd = this;
            while (nd != null) {
                path.add(nd.node.position());
                nd = nd.previous;
            }
            return path;
        }
    }
}
