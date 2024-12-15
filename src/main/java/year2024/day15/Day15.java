package year2024.day15;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

enum Heading {
    EAST('>', Coordinate::east),
    SOUTH('v', Coordinate::south),
    WEST('<', Coordinate::west),
    NORTH('^', Coordinate::north);
    final char c;
    final Function<Coordinate, Coordinate> mover;

    Heading(char c, Function<Coordinate, Coordinate> mover) {
        this.c = c;
        this.mover = mover;
    }

    static Heading from(char c) {
        return Arrays.stream(values()).filter(t -> t.c == c).findFirst().orElseThrow();
    }
}

public class Day15 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Grid initialGrid;

    Puzzle() throws Exception {
        initialGrid = Grid.from(Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day15_input")).toURI())));
    }

    void solve() {
        var grid = initialGrid;
        while (true) {
            //System.out.println(grid);
            var newGrid = grid.move();
            if (newGrid == null) {
                break;
            }
            grid = newGrid;
        }
        System.out.println(grid.gpsSum());
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

    Coordinate move(Heading heading) {
        return heading.mover.apply(this);
    }
}

record Grid(Coordinate position, Set<Coordinate> boxes, Set<Coordinate> walls, List<Heading> headings,
            int instructionPointer) {
    static Grid from(String input) {
        var parts = input.split("\n\n");
        int y = 0;
        Coordinate position = null;
        Set<Coordinate> boxes = new HashSet<>();
        Set<Coordinate> walls = new HashSet<>();
        for (String line : parts[0].split("\n")) {
            int x = 0;
            for (char c : line.toCharArray()) {
                Coordinate coordinate = new Coordinate(x, y);
                switch (c) {
                    case '@' -> position = coordinate;
                    case '#' -> walls.add(coordinate);
                    case 'O' -> boxes.add(coordinate);
                    case '.' -> {
                    }
                    default -> throw new IllegalArgumentException();
                }
                x++;
            }
            y++;
        }
        List<Heading> headings = new ArrayList<>();
        for (String line : parts[1].split("\n")) {
            for (char c : line.toCharArray()) {
                headings.add(Heading.from(c));
            }
        }
        return new Grid(position, boxes, walls, headings, 0);
    }

    static Set<Coordinate> swapBox(Set<Coordinate> boxes, Coordinate from, Coordinate to) {
        Set<Coordinate> newBoxes = boxes.stream().filter(c -> !c.equals(from)).collect(Collectors.toSet());
        newBoxes.add(to);
        return newBoxes;
    }

    Grid move() {
        if (instructionPointer >= headings.size()) {
            return null;
        }
        var heading = headings.get(instructionPointer);
        var newPosition = position.move(heading);
        Set<Coordinate> newBoxes = boxes;
        if (walls.contains(newPosition)) {
            newPosition = position;
        } else if (boxes.contains(newPosition)) {
            var possibleSpace = newPosition.move(heading);
            while (boxes.contains(possibleSpace)) {
                possibleSpace = possibleSpace.move(heading);
            }
            if (walls.contains(possibleSpace)) {
                newPosition = position;
            } else {
                newBoxes = swapBox(boxes, newPosition, possibleSpace);
            }
        }
        return new Grid(newPosition, newBoxes, walls, headings, instructionPointer + 1);
    }

    long gpsSum() {
        return boxes.stream().mapToLong(c -> c.x() + 100L * c.y()).sum();
    }

    @Override
    public String toString() {
        int width = walls.stream().mapToInt(Coordinate::x).max().orElseThrow() + 1;
        int height = walls.stream().mapToInt(Coordinate::y).max().orElseThrow() + 1;
        var sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            char[] chars = new char[width];
            for (int x = 0; x < width; x++) {
                var xy = new Coordinate(x, y);
                char c;
                if (xy.equals(position)) {
                    c = '@';
                } else if (walls.contains(xy)) {
                    c = '#';
                } else if (boxes.contains(xy)) {
                    c = 'O';
                } else {
                    c = '.';
                }
                chars[x] = c;
            }
            sb.append(chars);
            sb.append('\n');
        }
        return sb.toString();
    }
}
