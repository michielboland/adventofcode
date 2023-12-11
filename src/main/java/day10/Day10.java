package day10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

enum Heading {
    NORTH(Coordinate::north), EAST(Coordinate::east), SOUTH(Coordinate::south), WEST(Coordinate::west), NOWHERE(Function.identity());
    final Function<Coordinate, Coordinate> head;

    Heading(Function<Coordinate, Coordinate> head) {
        this.head = head;
    }
}

enum TileType {
    NORTH_SOUTH('|', h -> switch (h) {
        case SOUTH -> Heading.SOUTH;
        case NORTH -> Heading.NORTH;
        default -> Heading.NOWHERE;
    }), EAST_WEST('-', h -> switch (h) {
        case EAST -> Heading.EAST;
        case WEST -> Heading.WEST;
        default -> Heading.NOWHERE;
    }), NORTH_EAST('L', h -> switch (h) {
        case SOUTH -> Heading.EAST;
        case WEST -> Heading.NORTH;
        default -> Heading.NOWHERE;
    }), NORTH_WEST('J', h -> switch (h) {
        case SOUTH -> Heading.WEST;
        case EAST -> Heading.NORTH;
        default -> Heading.NOWHERE;
    }), SOUTH_WEST('7', h -> switch (h) {
        case NORTH -> Heading.WEST;
        case EAST -> Heading.SOUTH;
        default -> Heading.NOWHERE;
    }), SOUTH_EAST('F', h -> switch (h) {
        case NORTH -> Heading.EAST;
        case WEST -> Heading.SOUTH;
        default -> Heading.NOWHERE;
    }), GROUND('.'), START('S');
    final char character;
    final Function<Heading, Heading> nextHeading;

    TileType(char character, Function<Heading, Heading> nextHeading) {
        this.character = character;
        this.nextHeading = nextHeading;
    }

    TileType(char character) {
        this.character = character;
        nextHeading = h -> Heading.NOWHERE;
    }

    static TileType from(int character) {
        return Arrays.stream(values()).filter(t -> t.character == character).findFirst().orElseThrow();
    }
}

record Tile(Coordinate coordinate, TileType type) {
    static Tile blank(Coordinate coordinate) {
        return new Tile(coordinate, TileType.GROUND);
    }

    static Tile at(int x, int y, int character) {
        return new Tile(new Coordinate(x, y), TileType.from(character));
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
}

record Grid(Map<Coordinate, Tile> tiles, Tile start) {
    static void parse(Map<Coordinate, Tile> tiles, int y, String line) {
        final AtomicInteger x = new AtomicInteger();
        line.chars().mapToObj(c -> Tile.at(x.getAndIncrement(), y, c)).forEach(tile -> tiles.put(tile.coordinate(), tile));
    }

    static Grid parse(Stream<String> lines) {
        final AtomicInteger y = new AtomicInteger();
        Map<Coordinate, Tile> tiles = new TreeMap<>();
        lines.forEach(line -> parse(tiles, y.getAndIncrement(), line));
        return new Grid(tiles, findStart(tiles));
    }

    static Tile findStart(Map<Coordinate, Tile> tiles) {
        List<Tile> startTiles = tiles.values().stream().filter(t -> t.type() == TileType.START).toList();
        if (startTiles.size() != 1) {
            throw new IllegalStateException();
        }
        return startTiles.get(0);
    }

    static boolean isInside(Coordinate t, List<Coordinate> path) {
        if (path.isEmpty()) {
            return false;
        }
        if (path.contains(t)) {
            return false;
        }
        List<Coordinate> loop = new ArrayList<>(path);
        loop.add(path.get(0));
        int index = 0;
        for (int i = 0; i + 1 < loop.size(); i++) {
            var p = loop.get(i);
            var q = loop.get(i + 1);
            var yFlag = p.y() >= t.y();
            if (yFlag != (q.y() >= t.y())) {
                var ySign = yFlag ? -1 : 1;
                var xFlag = p.x() >= t.x();
                if (xFlag == q.x() >= t.x()) {
                    if (xFlag) {
                        index += ySign;
                    }
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }
        return index != 0;
    }

    List<Tile> walk(Heading heading) {
        List<Tile> tiles = new ArrayList<>();
        Tile tile = start;
        do {
            if (heading == Heading.NOWHERE) {
                return Collections.emptyList();
            }
            tiles.add(tile);
            tile = tile(heading.head.apply(tile.coordinate()));
            if (tile == start) {
                return tiles;
            }
            heading = tile.type().nextHeading.apply(heading);
        } while (true);
    }

    List<Coordinate> walkAllHeadings() {
        List<Coordinate> path = Arrays.stream(Heading.values()).map(this::walk).filter(Predicate.not(List::isEmpty)).findFirst().orElseThrow().stream().map(Tile::coordinate).toList();
        System.out.println("max distance: " + (path.size() >> 1));
        return path;
    }

    void countInsidePoints(List<Coordinate> path) {
        List<Coordinate> inside = tiles.values().stream().map(Tile::coordinate).filter(t -> isInside(t, path)).toList();
        System.out.println("inside points: " + inside.size());
    }

    static int countInteriorPoints(List<Coordinate> path) {
        List<Coordinate> loop = new ArrayList<>(path);
        loop.add(path.get(0));
        int doubleArea = 0;
        for (int i = 0; i + 1 < loop.size(); i++) {
            var a = loop.get(i);
            var b = loop.get(i + 1);
            doubleArea += a.x() * b.y() - a.y() * b.x();
        }
        return (Math.abs(doubleArea) - path.size()) / 2 + 1;
    }

    void countInsidePointsAlternate(List<Coordinate> path) {
        System.out.println("inside points (alternate method): " + countInteriorPoints(path));
    }

    Tile tile(Coordinate coordinate) {
        return tiles.computeIfAbsent(coordinate, Tile::blank);
    }
}

public class Day10 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

// parse 40 ms
// walkAllHeadings 16 ms
// countInsidePoints 1027 ms
// countInsidePointsAlternate 8 ms

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day10/day10_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var grid = Grid.parse(reader.lines());
            List<Coordinate> coordinates = grid.walkAllHeadings();
            grid.countInsidePoints(coordinates);
            grid.countInsidePointsAlternate(coordinates);
        }
    }
}
