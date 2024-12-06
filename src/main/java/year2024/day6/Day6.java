package year2024.day6;

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
    NORTH(Coordinate::north), EAST(Coordinate::east), SOUTH(Coordinate::south), WEST(Coordinate::west);
    final Function<Coordinate, Coordinate> head;

    Heading(Function<Coordinate, Coordinate> head) {
        this.head = head;
    }

    Heading rotate() {
        return switch (this) {
            case NORTH -> EAST;
            case EAST -> SOUTH;
            case SOUTH -> WEST;
            case WEST -> NORTH;
        };
    }
}

enum TileType {
    GUARD('^'),
    BLANK('.'),
    OBSTACLE('#'),
    VISITED('X');

    final char character;

    TileType(char c) {
        character = c;
    }

    static TileType from(int character) {
        return Arrays.stream(values()).filter(t -> t.character == character).findFirst().orElseThrow();
    }
}

public class Day6 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {

    final Grid grid;
    final AtomicInteger positionCounter = new AtomicInteger(1);

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day6_input"))) {
            grid = Grid.parse(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        var guard = grid.findStart();
        var heading = Heading.NORTH;
        while (true) {
            Tile next = grid.tiles().get(guard.coordinate().move(heading));
            if (next == null) {
                break;
            }
            switch (next.type()) {
                case BLANK, VISITED -> guard = move(guard, next);
                case OBSTACLE -> heading = heading.rotate();
            }
        }
        System.out.println(positionCounter.get());
    }

    Tile move(Tile tile, Tile next) {
        if (next.type() == TileType.BLANK) {
            positionCounter.incrementAndGet();
        }
        return grid.move(tile, next);
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
        return heading.head.apply(this);
    }

    @Override
    public int compareTo(Coordinate o) {
        return y == o.y ? Integer.compare(x, o.x) : Integer.compare(y, o.y);
    }
}

record Tile(Coordinate coordinate, TileType type) {
    static Tile blank(Coordinate coordinate) {
        return new Tile(coordinate, TileType.BLANK);
    }

    static Tile at(int x, int y, int character) {
        return new Tile(new Coordinate(x, y), TileType.from(character));
    }
}

record Grid(Map<Coordinate, Tile> tiles) {
    static void parse(Map<Coordinate, Tile> tiles, int y, String line) {
        final AtomicInteger x = new AtomicInteger();
        line.chars().mapToObj(c -> Tile.at(x.getAndIncrement(), y, c)).forEach(tile -> tiles.put(tile.coordinate(), tile));
    }

    static Grid parse(Stream<String> lines) {
        final AtomicInteger y = new AtomicInteger();
        Map<Coordinate, Tile> tiles = new TreeMap<>();
        lines.forEach(line -> parse(tiles, y.getAndIncrement(), line));
        return new Grid(tiles);
    }

    Tile findStart() {
        List<Tile> startTiles = tiles.values().stream().filter(t -> t.type() == TileType.GUARD).toList();
        if (startTiles.size() != 1) {
            throw new IllegalStateException();
        }
        return startTiles.get(0);
    }

    Tile move(Tile tile, Tile next) {
        tiles.put(tile.coordinate(), new Tile(tile.coordinate(), TileType.VISITED));
        Tile newNext = new Tile(next.coordinate(), tile.type());
        tiles.put(next.coordinate(), newNext);
        return newNext;
    }
}
