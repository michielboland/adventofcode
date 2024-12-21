package year2024.day20;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Day20 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    private final Grid grid;

    Puzzle() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day20_input"))) {
            grid = Grid.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        var map = grid.distanceMap();
        System.out.println(grid.countGoodCheats(map, 2));
        System.out.println(grid.countGoodCheats(map, 20));
    }
}

record Coordinate(int x, int y) implements Comparable<Coordinate> {
    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }

    Collection<Coordinate> neighbours() {
        return Set.of(
                new Coordinate(x, y - 1),
                new Coordinate(x + 1, y),
                new Coordinate(x, y + 1),
                new Coordinate(x - 1, y)
        );
    }

    Collection<Coordinate> neighbours(int cheatDistance) {
        Collection<Coordinate> neighbours = new TreeSet<>();
        for (int i = 0; i < cheatDistance; i++) {
            neighbours.add(new Coordinate(x + i, y - cheatDistance + i));
            neighbours.add(new Coordinate(x + cheatDistance - i, y + i));
            neighbours.add(new Coordinate(x - i, y + cheatDistance - i));
            neighbours.add(new Coordinate(x - cheatDistance + i, y - i));
        }
        return neighbours;
    }
}

record Grid(int width, int height, Set<Coordinate> walls, Coordinate start, Coordinate finish) {
    static Grid from(Stream<String> lines) {
        Set<Coordinate> walls = new HashSet<>();
        Coordinate start = null;
        Coordinate end = null;
        int y = 0;
        int x = 0;
        for (String line : lines.toList()) {
            x = 0;
            for (char c : line.toCharArray()) {
                switch (c) {
                    case 'S' -> start = new Coordinate(x, y);
                    case 'E' -> end = new Coordinate(x, y);
                    case '#' -> walls.add(new Coordinate(x, y));
                    case '.' -> {
                    }
                    default -> throw new IllegalArgumentException();
                }
                x++;
            }
            ++y;
        }
        return new Grid(x, y, walls, Objects.requireNonNull(start), Objects.requireNonNull(end));
    }

    Map<Coordinate, Integer> distanceMap() {
        Map<Coordinate, Integer> distanceMap = new HashMap<>();
        Coordinate pos = start;
        int distance = 0;
        do {
            distanceMap.put(pos, distance++);
            pos = pos.neighbours().stream().filter(Predicate.not(walls::contains)).filter(Predicate.not(distanceMap::containsKey)).findFirst().orElse(null);
        } while (pos != null);
        return distanceMap;
    }

    long countGoodCheats(Map<Coordinate, Integer> distances, int maxCheatDuration) {
        Map<List<Coordinate>, Integer> cheats = new HashMap<>();
        for (Map.Entry<Coordinate, Integer> e : distances.entrySet()) {
            Coordinate cheatStart = e.getKey();
            int distance = e.getValue();
            for (int cheatDuration = 2; cheatDuration <= maxCheatDuration; cheatDuration++) {
                for (var cheatEnd : cheatStart.neighbours(cheatDuration)) {
                    Integer newDistance = distances.get(cheatEnd);
                    if (newDistance != null) {
                        cheats.put(List.of(cheatStart, cheatEnd), newDistance - distance - cheatDuration);
                    }
                }
            }
        }
        return cheats.entrySet().stream().filter(e -> e.getValue() >= 100).count();
    }
}
