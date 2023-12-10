package day10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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

    int walk(Heading heading) {
        int steps = 1;
        Tile tile = start;
        do {
            if (heading == Heading.NOWHERE) {
                return 0;
            }
            tile = tile(heading.head.apply(tile.coordinate()));
            if (tile == start) {
                return steps;
            }
            ++steps;
            heading = tile.type().nextHeading.apply(heading);
        } while (true);
    }

    void walkAllHeadings() {
        System.out.println(Arrays.stream(Heading.values()).mapToInt(this::walk).filter(n -> n > 1).findFirst().orElseThrow() >> 1);
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

// parse 60 ms
// walkAllHeadings 12 ms

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day10/day10_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var grid = Grid.parse(reader.lines());
            grid.walkAllHeadings();
        }
    }
}
