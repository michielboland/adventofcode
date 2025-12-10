package year2025.day9;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class Puzzle {
    private final List<Coordinate> redTiles = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day9_input")))).lines().map(Coordinate::parse).toList();
    private final Coordinate magic1;
    private final Coordinate magic2;

    Puzzle() {
        // shortcut - make use of the fact that the red tiles form a circle with a very wide bit taken out
        for (int i = 0; i < redTiles.size() - 2; i++) {
            var a = redTiles.get(i);
            var b = redTiles.get(i + 1);
            var width = Math.abs(a.x() - b.x());
            if (width > 50000) {
                magic1 = redTiles.get(i + 1);
                magic2 = redTiles.get(i + 2);
                if (Math.signum(magic1.y() - 50000) == Math.signum(magic2.y() - 50000)) {
                    throw new IllegalStateException();
                }
                return;
            }
        }
        magic1 = null;
        magic2 = null;
    }

    public static void main(String[] args) {
        new Puzzle().solve();
    }

    private void solve() {
        System.out.println(part1());
        System.out.println(part2());
    }

    private long part1() {
        return maxArea(true);
    }

    private long part2() {
        return maxArea(false);
    }

    private long maxArea(boolean skipCheck) {
        long maxArea = 0;
        for (int i = 0; i < redTiles.size(); i++) {
            for (int j = i + 1; j < redTiles.size(); j++) {
                var rectangle = new Rectangle(redTiles.get(i), redTiles.get(j));
                var area = rectangle.area();
                if (area > maxArea && (skipCheck || feasible(rectangle))) {
                    maxArea = area;
                }
            }
        }
        return maxArea;
    }

    boolean isMagic(Coordinate coordinate) {
        return coordinate.equals(magic1) || coordinate.equals(magic2);
    }

    boolean feasible(Rectangle rectangle) {
        if (isMagic(rectangle.corner()) || isMagic(rectangle.opposite())) {
            var minX = Math.min(rectangle.corner().x(), rectangle.opposite().x());
            var maxX = Math.max(rectangle.corner().x(), rectangle.opposite().x());
            var minY = Math.min(rectangle.corner().y(), rectangle.opposite().y());
            var maxY = Math.max(rectangle.corner().y(), rectangle.opposite().y());
            if (minY == maxY) {
                return false;
            }
            // FIXME compute intersections properly instead of this index nonsense
            for (var y = minY; y <= maxY; y++) {
                if (index(new Coordinate(minX, y)) == 0 || index(new Coordinate(maxX, y)) == 0) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    int index(Coordinate t) {
        var n = redTiles.size();
        int idx = 0;
        for (int i = 0; i < n; i++) {
            var p = redTiles.get(i);
            var q = redTiles.get(i + 1 == n ? 0 : i + 1);
            // special cases
            if ((p.y() == t.y()) && q.y() == t.y()) {
                if ((p.x() - t.x()) * (q.x() - t.x()) <= 0) {
                    return 37;
                }
            }
            if ((p.x() == t.x()) && q.x() == t.x()) {
                if ((p.y() - t.y()) * (q.y() - t.y()) <= 0) {
                    return -37;
                }
            }
            var yFlag = p.y() >= t.y();
            if (yFlag != (q.y() >= t.y())) {
                var ySign = yFlag ? -1 : 1;
                var xFlag = p.x() >= t.x();
                if (xFlag == (q.x() >= t.x())) {
                    if (xFlag) {
                        idx += ySign;
                    }
                } else {
                    // shortcut - don't need to bother for diagonal segments
                    throw new UnsupportedOperationException();
                }
            }
        }
        return idx;
    }
}

record Rectangle(Coordinate corner, Coordinate opposite) {
    long area() {
        return (Math.abs(corner.x() - opposite.x()) + 1) * (Math.abs(corner.y() - opposite.y()) + 1);
    }
}

record Coordinate(long x, long y) {
    static Coordinate parse(String s) {
        var parts = s.split(",");
        return new Coordinate(Integer.parseInt(parts[0]), Integer.parseInt((parts[1])));
    }
}
