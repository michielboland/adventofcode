package year2024.day6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    NEW_OBSTACLE('O'),
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

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day6_input"))) {
            grid = Grid.parse(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        System.out.println(grid.walk(true));
        System.out.println(grid.loopCount());
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
    static Tile at(int x, int y, int character) {
        return new Tile(new Coordinate(x, y), TileType.from(character));
    }
}

class Holder {
    Coordinate coordinate;

    void put(final Coordinate coordinate) {
        this.coordinate = coordinate;
    }
}

record Grid(Map<Coordinate, TileType> tiles, Coordinate start) {
    static void parse(Map<Coordinate, TileType> tiles, int y, String line, Holder holder) {
        final AtomicInteger x = new AtomicInteger();
        line.chars().mapToObj(c -> Tile.at(x.getAndIncrement(), y, c)).forEach(tile -> {
            if (tile.type() == TileType.GUARD) {
                holder.put(tile.coordinate());
            }
            tiles.put(tile.coordinate(), tile.type());
        });
    }

    static Grid parse(Stream<String> lines) {
        final AtomicInteger y = new AtomicInteger();
        Map<Coordinate, TileType> tiles = new TreeMap<>();
        var holder = new Holder();
        lines.forEach(line -> parse(tiles, y.getAndIncrement(), line, holder));
        return new Grid(tiles, holder.coordinate);
    }

    int walk(boolean mark) {
        Map<Coordinate, Set<Heading>> loopDetect = new HashMap<>();
        var guard = start();
        var heading = Heading.NORTH;
        int i = 1;
        while (true) {
            loopDetect.computeIfAbsent(guard, c -> new HashSet<>());
            var set = loopDetect.get(guard);
            if (set.contains(heading)) {
                return -1;
            }
            set.add(heading);
            var nextPosition = guard.move(heading);
            TileType next = tiles.get(nextPosition);
            if (next == null) {
                return i;
            }
            switch (next) {
                case BLANK, GUARD, VISITED -> {
                    if (next == TileType.BLANK) {
                        ++i;
                    }
                    if (mark) {
                        tiles.put(nextPosition, TileType.VISITED);
                    }
                    guard = nextPosition;
                }
                case OBSTACLE, NEW_OBSTACLE -> heading = heading.rotate();
                default -> throw new IllegalStateException();
            }
        }
    }

    int looped(Coordinate c) {
        TileType orig = tiles.put(c, TileType.NEW_OBSTACLE);
        var l = walk(false);
        tiles.put(c, orig);
        return l == -1 ? 1 : 0;
    }

    int loopCount() {
        return tiles.keySet().stream()
                .filter(c -> tiles.get(c) == TileType.VISITED)
                .mapToInt(this::looped)
                .sum();
    }
}
