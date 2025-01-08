package year2022.day8;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class Puzzle {
    private final Grid grid;

    Puzzle() throws Exception {
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day8_input"))))) {
            grid = Grid.from(reader.lines());
        }
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(part1());
    }

    int part1() {
        return grid.visibleTrees().size();
    }
}

enum Heading {
    NORTH(Coordinate::north), SOUTH(Coordinate::south), WEST(Coordinate::west), EAST(Coordinate::east);

    final Function<Coordinate, Coordinate> mover;

    Heading(Function<Coordinate, Coordinate> mover) {
        this.mover = mover;
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

record Viewpoint(Coordinate position, Heading heading) {
    static Collection<Viewpoint> viewpoints(int width, int height) {
        Collection<Viewpoint> viewpoints = new ArrayList<>();
        for (int x = 0; x < width; x++) {
            viewpoints.add(new Viewpoint(new Coordinate(x, 0), Heading.SOUTH));
            viewpoints.add(new Viewpoint(new Coordinate(x, width - 1), Heading.NORTH));
        }
        for (int y = 0; y < height; y++) {
            viewpoints.add(new Viewpoint(new Coordinate(0, y), Heading.EAST));
            viewpoints.add(new Viewpoint(new Coordinate(height - 1, y), Heading.WEST));
        }
        return viewpoints;
    }

    Viewpoint move() {
        return new Viewpoint(heading.mover.apply(position), heading);
    }
}

record Grid(Map<Coordinate, Integer> trees, Collection<Viewpoint> viewpoints) {
    static Grid from(Stream<String> lines) {
        Map<Coordinate, Integer> trees = new HashMap<>();
        int width = 0;
        int y = 0;
        for (String line : lines.toList()) {
            if (line.length() > width) {
                width = line.length();
            }
            for (int x = 0; x < line.length(); x++) {
                trees.put(new Coordinate(x, y), line.charAt(x) - '0');
            }
            ++y;
        }
        return new Grid(trees, Viewpoint.viewpoints(width, y));
    }

    Set<Coordinate> visibleTrees() {
        Map<Coordinate, Set<Heading>> visibleTrees = new HashMap<>();
        viewpoints.forEach(viewpoint -> {
            int max = -1;
            for (var v = viewpoint; trees.containsKey(v.position()); v = v.move()) {
                int treeHeight = trees.get(v.position());
                if (treeHeight > max) {
                    max = treeHeight;
                    visibleTrees.computeIfAbsent(v.position(), c -> new HashSet<>()).add(v.heading());
                }
            }
        });
        return visibleTrees.keySet();
    }
}
