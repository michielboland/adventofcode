package year2024.day15;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
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
        new Puzzle(false).solve();
        new Puzzle(true).solve();
    }
}

class Puzzle {
    final Grid initialGrid;

    Puzzle(boolean wide) throws Exception {
        initialGrid = Grid.from(Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day15_input")).toURI())), wide);
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
            int instructionPointer, boolean wide) {
    static Grid from(String input, boolean wide) {
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
                }
                if (wide && c == '#') {
                    walls.add(coordinate.east());
                }
                x += wide ? 2 : 1;
            }
            y++;
        }
        List<Heading> headings = new ArrayList<>();
        for (String line : parts[1].split("\n")) {
            for (char c : line.toCharArray()) {
                headings.add(Heading.from(c));
            }
        }
        return new Grid(position, boxes, walls, headings, 0, wide);
    }

    boolean isBoxNextToWall(Coordinate box, Heading heading) {
        return walls.contains(box.move(heading)) || wide && walls.contains(box.move(heading).east());
    }

    Set<Coordinate> movableBoxes(Coordinate initialPos, Heading heading) {
        Set<Coordinate> visited = new HashSet<>();
        Deque<Coordinate> queue = new ArrayDeque<>();
        if (!boxes.contains(initialPos)) {
            throw new IllegalStateException();
        }
        queue.addLast(initialPos);
        while (!queue.isEmpty()) {
            var pos = queue.removeFirst();
            if (visited.contains(pos)) {
                continue;
            }
            visited.add(pos);
            var newPos = pos.move(heading);
            if (boxes.contains(newPos)) {
                queue.addLast(newPos);
            }
            if (wide) {
                if (boxes.contains(newPos.west())) {
                    queue.addLast(newPos.west());
                }
                if (boxes.contains(newPos.east())) {
                    queue.addLast(newPos.east());
                }
            }
        }
        return visited.stream().noneMatch(c -> isBoxNextToWall(c, heading)) ? visited : Collections.emptySet();
    }

    boolean isRobotNextToWall(Heading heading) {
        return walls.contains(position.move(heading));
    }

    Coordinate boxNextToRobot(Heading heading) {
        Coordinate next = position.move(heading);
        if (boxes.contains(next)) {
            return next;
        }
        if (wide) {
            Coordinate west = next.west();
            if (boxes.contains(west)) {
                return west;
            }
        }
        return null;
    }

    Grid move() {
        if (instructionPointer >= headings.size()) {
            return null;
        }
        var heading = headings.get(instructionPointer);
        boolean robotCanMove;
        Set<Coordinate> movableBoxes;
        if (isRobotNextToWall(heading)) {
            robotCanMove = false;
            movableBoxes = Collections.emptySet();
        } else {
            var possibleBox = boxNextToRobot(heading);
            if (possibleBox == null) {
                robotCanMove = true;
                movableBoxes = Collections.emptySet();
            } else {
                movableBoxes = movableBoxes(possibleBox, heading);
                robotCanMove = !movableBoxes.isEmpty();
            }
        }
        Set<Coordinate> newBoxes;
        if (movableBoxes.isEmpty()) {
            newBoxes = boxes;
        } else {
            newBoxes = new HashSet<>();
            newBoxes.addAll(boxes.stream().filter(Predicate.not(movableBoxes::contains)).collect(Collectors.toSet()));
            newBoxes.addAll(boxes.stream().filter(movableBoxes::contains).map(c -> c.move(heading)).collect(Collectors.toSet()));
        }
        return new Grid(robotCanMove ? position.move(heading) : position, newBoxes, walls, headings, instructionPointer + 1, wide);
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
                    c = wide ? '[' : 'O';
                } else if (wide && boxes.contains(xy.west())) {
                    c = ']';
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
