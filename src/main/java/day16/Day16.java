package day16;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Tile {
    SPACE('.', h -> h), MIRROR_A('/', h -> switch (h) {
        case EAST -> Heading.NORTH;
        case WEST -> Heading.SOUTH;
        case NORTH -> Heading.EAST;
        case SOUTH -> Heading.WEST;
        default -> throw new IllegalArgumentException();
    }), MIRROR_B('\\', h -> switch (h) {
        case EAST -> Heading.SOUTH;
        case WEST -> Heading.NORTH;
        case NORTH -> Heading.WEST;
        case SOUTH -> Heading.EAST;
        default -> throw new IllegalArgumentException();
    }), SPLITTER_V('|', h -> switch (h) {
        case NORTH, SOUTH -> h;
        case EAST, WEST -> Heading.NORTH_AND_SOUTH;
        default -> throw new IllegalArgumentException();
    }), SPLITTER_H('-', h -> switch (h) {
        case NORTH, SOUTH -> Heading.EAST_AND_WEST;
        case EAST, WEST -> h;
        default -> throw new IllegalArgumentException();
    });
    final char symbol;
    final Function<Heading, Heading> nextHeading;

    Tile(char symbol, Function<Heading, Heading> nextHeading) {
        this.symbol = symbol;
        this.nextHeading = nextHeading;
    }

    static Tile from(int c) {
        return Arrays.stream(values()).filter(t -> t.symbol == c).findFirst().orElseThrow();
    }
}

enum Heading {
    NORTH(Coordinate::north), SOUTH(Coordinate::south), EAST(Coordinate::east), WEST(Coordinate::west), NORTH_AND_SOUTH(Set.of(NORTH, SOUTH)), EAST_AND_WEST(Set.of(EAST, WEST));
    final Function<Coordinate, Coordinate> head;
    final Set<Heading> split;

    Heading(Function<Coordinate, Coordinate> head, Set<Heading> split) {
        this.head = head;
        this.split = split;
    }

    Heading(Function<Coordinate, Coordinate> head) {
        this(head, Collections.emptySet());
    }

    Heading(Set<Heading> split) {
        this(Function.identity(), split);
    }
}

public class Day16 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Coordinate(int x, int y) {
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
}

record CH(Coordinate coordinate, Heading heading) {
}

record Grid(Map<Coordinate, Tile> tileMap, Set<CH> visitedCoordinateHeadings) {
    static void parse(int c, Map<Coordinate, Tile> tileMap, int x, int y) {
        tileMap.put(new Coordinate(x, y), Tile.from(c));
    }

    static void parse(String line, Map<Coordinate, Tile> tileMap, int y) {
        var xc = new AtomicInteger();
        line.chars().forEach(c -> parse(c, tileMap, xc.getAndIncrement(), y));
    }

    static Grid parse(Stream<String> lines) {
        Map<Coordinate, Tile> tileMap = new HashMap<>();
        var yc = new AtomicInteger();
        lines.forEach(line -> parse(line, tileMap, yc.getAndIncrement()));
        return new Grid(tileMap, new HashSet<>());
    }

    boolean notVisited(Coordinate coordinate, Heading heading) {
        return tileMap.get(coordinate) != null && visitedCoordinateHeadings.add(new CH(coordinate, heading));
    }

    void illuminate(Coordinate coordinate, Heading heading) {
        while (notVisited(coordinate, heading)) {
            var tile = tileMap.get(coordinate);
            var nextHeading = tile.nextHeading.apply(heading);
            if (!nextHeading.split.isEmpty()) {
                for (var h : nextHeading.split) {
                    illuminate(h.head.apply(coordinate), h);
                }
                return;
            }
            coordinate = nextHeading.head.apply(coordinate);
            heading = nextHeading;
        }
    }

    int illuminatedTiles(int x, int y, Heading heading) {
        visitedCoordinateHeadings.clear();
        illuminate(new Coordinate(x, y), heading);
        return visitedCoordinateHeadings.stream().map(CH::coordinate).collect(Collectors.toSet()).size();
    }

    int maxIllumination() {
        var w = tileMap.keySet().stream().mapToInt(Coordinate::x).max().orElseThrow();
        var h = tileMap.keySet().stream().mapToInt(Coordinate::y).max().orElseThrow();
        Set<Integer> values = new TreeSet<>();
        for (int x = 0; x <= w; x++) {
            values.add(illuminatedTiles(x, 0, Heading.SOUTH));
            values.add(illuminatedTiles(x, h, Heading.NORTH));
        }
        for (int y = 0; y <= h; y++) {
            values.add(illuminatedTiles(0, y, Heading.EAST));
            values.add(illuminatedTiles(w, y, Heading.WEST));
        }
        return values.stream().max(Integer::compareTo).orElseThrow();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day16/day16_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var grid = Grid.parse(reader.lines());
            System.out.println(grid.illuminatedTiles(0, 0, Heading.EAST));
            System.out.println(grid.maxIllumination());
        }
    }
}
