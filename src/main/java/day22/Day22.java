package day22;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

record Coordinate2(int x, int y) implements Comparable<Coordinate2> {
    private int[] toArray() {
        return new int[]{y, x};
    }

    @Override
    public int compareTo(Coordinate2 o) {
        return Arrays.compare(toArray(), o.toArray());
    }
}

record ClosedInterval(int from, int to) {
    boolean intersects(ClosedInterval other) {
        return Math.max(from, other.from) <= Math.min(to, other.to);
    }
}

record Rectangle(Coordinate2 topLeft, Coordinate2 bottomRight) {
    ClosedInterval projectX() {
        return new ClosedInterval(topLeft.x(), bottomRight.x());
    }

    ClosedInterval projectY() {
        return new ClosedInterval(topLeft.y(), bottomRight.y());
    }

    boolean intersects(Rectangle other) {
        return projectX().intersects(other.projectX()) && projectY().intersects(other.projectY());
    }
}

record Coordinate3(int x, int y, int z) implements Comparable<Coordinate3> {
    static Coordinate3 parse(String s) {
        int[] a = Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
        return new Coordinate3(a[0], a[1], a[2]);
    }

    Coordinate2 flatten() {
        return new Coordinate2(x, y);
    }

    private int[] toArray() {
        return new int[]{z, y, x};
    }

    Coordinate3 moveDown(int positions) {
        return new Coordinate3(x, y, z - positions);
    }

    @Override
    public int compareTo(Coordinate3 o) {
        return Arrays.compare(toArray(), o.toArray());
    }
}

record Brick(Coordinate3 bottomBackLeft, Coordinate3 topFrontRight) implements Comparable<Brick> {
    static Brick parse(String s) {
        var split = s.split("~");
        return new Brick(Coordinate3.parse(split[0]), Coordinate3.parse(split[1]));
    }

    int bottom() {
        return bottomBackLeft.z();
    }

    int top() {
        return topFrontRight.z();
    }

    Rectangle flatten() {
        return new Rectangle(bottomBackLeft.flatten(), topFrontRight.flatten());
    }

    boolean isBelow(Brick other) {
        return top() < other.bottom() && flatten().intersects(other.flatten());
    }

    boolean isDirectlyBelow(Brick other) {
        return top() + 1 == other.bottom() && flatten().intersects(other.flatten());
    }

    Brick moveAbove(int top) {
        var positions = bottom() - top - 1;
        return new Brick(bottomBackLeft.moveDown(positions), topFrontRight.moveDown(positions));
    }

    @Override
    public int compareTo(Brick o) {
        return bottomBackLeft.compareTo(o.bottomBackLeft);
    }
}

record Bricks(SortedMap<Coordinate3, Brick> initialPositions, SortedMap<Coordinate3, Brick> restingPositions,
              Map<Brick, Set<Brick>> supports, Map<Brick, Set<Brick>> supportedBy) {
    static Bricks parse(Stream<String> lines) {
        SortedMap<Coordinate3, Brick> initialPositions = new TreeMap<>();
        lines.map(Brick::parse).forEach(brick -> initialPositions.put(brick.bottomBackLeft(), brick));
        return new Bricks(initialPositions, new TreeMap<>(), new TreeMap<>(), new TreeMap<>());
    }

    boolean canBeRemoved(Brick brick) {
        if (!supports.containsKey(brick)) {
            return true;
        }
        for (var supportedByThisBrick : supports.get(brick)) {
            if (supportedBy.get(supportedByThisBrick).size() == 1) {
                return false;
            }
        }
        return true;
    }

    long solve() {
        var i = initialPositions.values().iterator();
        while (i.hasNext()) {
            var brick = i.next();
            i.remove();
            var top = restingPositions.values().stream().filter(b -> b.isBelow(brick)).mapToInt(Brick::top).max().orElse(0);
            var restingBrick = brick.moveAbove(top);
            restingPositions.values().stream().filter(b -> b.isDirectlyBelow(restingBrick)).forEach(supportingBrick -> {
                supports.computeIfAbsent(supportingBrick, unused -> new TreeSet<>()).add(restingBrick);
                supportedBy.computeIfAbsent(restingBrick, unused -> new TreeSet<>()).add(supportingBrick);
            });
            restingPositions.put(restingBrick.bottomBackLeft(), restingBrick);
        }
        return restingPositions.values().stream().filter(this::canBeRemoved).count();
    }

    boolean willFall(Brick brick, Set<Brick> removed) {
        Set<Brick> remaining = new TreeSet<>(supportedBy.get(brick));
        remaining.removeAll(removed);
        return remaining.isEmpty();
    }

    void topple(Brick brick, Set<Brick> removed) {
        removed.add(brick);
        if (!supports.containsKey(brick)) {
            return;
        }
        var next = supports.get(brick).stream().filter(b -> willFall(b, removed)).collect(Collectors.toSet());
        next.removeAll(removed);
        removed.addAll(next);
        next.forEach(b -> topple(b, removed));
    }

    int topple(Brick brick) {
        Set<Brick> removed = new TreeSet<>();
        topple(brick, removed);
        return removed.size() - 1;
    }

    int solve2() {
        return supports.keySet().stream().mapToInt(this::topple).sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day22/day22_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var bricks = Bricks.parse(reader.lines());
            System.out.println(bricks.solve());
            System.out.println(bricks.solve2());
        }
    }
}

public class Day22 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}
