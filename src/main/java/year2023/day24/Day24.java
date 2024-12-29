package year2023.day24;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

record Coordinate3(long x, long y, long z) {
    static Coordinate3 from(String s) {
        var a = Arrays.stream(s.split(", +")).mapToLong(Long::valueOf).toArray();
        return new Coordinate3(a[0], a[1], a[2]);
    }

    @Override
    public String toString() {
        return x + ", " + y + ", " + z;
    }
}

record Hailstone(Coordinate3 p, Coordinate3 v) {
    private static final long min = 200000000000000L;
    private static final long max = 400000000000000L;

    static Hailstone from(String s) {
        var a = Arrays.stream(s.split(" @ +")).map(Coordinate3::from).toArray(Coordinate3[]::new);
        return new Hailstone(a[0], a[1]);
    }

    boolean willReach(double x, double y) {
        double dx = x - p.x();
        double dy = y - p.y();
        return switch (dx > 0 ? 1 : dx < 0 ? -1 : 0) {
            case 0 -> switch (dy > 0 ? 1 : dy < 0 ? -1 : 0) {
                case 0 -> false;
                case 1 -> v.y() > 0;
                default -> v.y() < 0;
            };
            case 1 -> v.x() > 0;
            default -> v.x() < 0;
        };
    }

    boolean intersectsXY(Hailstone o) {
        double d = -v.y() * o.v.x() + o.v.y() * v.x();
        if (d != 0) {
            double a = v.y() * p.x() - v.x() * p.y();
            double b = o.v.y() * o.p.x() - o.v.x() * o.p.y();
            double x = (-o.v.x() * a + v.x() * b) / d;
            double y = (-o.v.y() * a + v.y() * b) / d;
            if (willReach(x, y) && o.willReach(x, y)) {
                return x >= min && x <= max && y >= min && y <= max;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return p + " @ " + v;
    }
}

record WeatherMap(List<Hailstone> hailstones) {
    static WeatherMap parse(Stream<String> lines) {
        return new WeatherMap(lines.map(Hailstone::from).toList());
    }

    long solve() {
        return IntStream.range(0, hailstones.size())
                .mapToLong(i -> IntStream.range(i + 1, hailstones.size())
                        .filter(j -> hailstones.get(i).intersectsXY(hailstones.get(j)))
                        .count())
                .sum();
    }

    long solve2() {
        /*
         * This solution is based on the fact that for each hailstone, the difference between the rock's initial position
         * and the hailstone's initial position must be a (negative) multiple of the difference between the rock's velocity
         * and the hailstone's velocity. So the cross product of these two differences must be zero. This gets rid of
         * the time factor.
         * The cross product gives three second-degree equations for the six unknowns (px, py, pz, vx, vy, vz), where p indicates
         * the rock position and v the rock velocity.
         * We can get rid of the quadratic terms by subtracting equations for different hailstones.
         * This leaves three sets of four equations with four unknowns, which we then solve using Gauss elimination.
         * Note we only use the full first set of four and only half of the second set. (You could use the other solutions
         * to verify that they equal the ones we already found.)
         */
        List<List<Long>> rowsYZ = new ArrayList<>();
        List<List<Long>> rowsZX = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            var h1 = hailstones.get(i);
            var h2 = hailstones.get(i + 1);
            var p1 = h1.p();
            var v1 = h1.v();
            var p2 = h2.p();
            var v2 = h2.v();
            rowsYZ.add(List.of(
                    v2.z() - v1.z(),
                    v1.y() - v2.y(),
                    p1.z() - p2.z(),
                    p2.y() - p1.y(),
                    p2.y() * v2.z() - p1.y() * v1.z()
                            + p1.z() * v1.y() - p2.z() * v2.y()
            ));
            rowsZX.add(List.of(
                    v2.x() - v1.x(),
                    v1.z() - v2.z(),
                    p1.x() - p2.x(),
                    p2.z() - p1.z(),
                    p2.z() * v2.x() - p1.z() * v1.x()
                            + p1.x() * v1.z() - p2.x() * v2.z()
            ));
        }
        var yz = new Solver().solve(rowsYZ);
        var zx = new Solver().solve(rowsZX);
        /*
         * The YZ solution has (py, pz, vy, vz)
         * The ZX solution has (pz, px, vz, vx)
         */
        return zx.get(1) + yz.get(0) + yz.get(1);
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day24/day24_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var map = WeatherMap.parse(reader.lines());
            System.out.println(map.solve());
            System.out.println(map.solve2());
        }
    }
}

class Solver {
    private void pivot(List<ArrayList<BigInteger>> matrix, int from, int to) {
        var cell = matrix.get(to).get(from);
        if (cell.equals(BigInteger.ZERO)) {
            return;
        }
        var pivot = matrix.get(from).get(from);
        var gcd = cell.abs().gcd(pivot);
        var row = matrix.get(to);
        var addendum = matrix.get(from);
        for (int i = 0; i < row.size(); i++) {
            row.set(i, row.get(i).multiply(pivot.divide(gcd).negate()).add(addendum.get(i).multiply(cell.divide(gcd))));
        }
    }

    private void rowEchelon(List<ArrayList<BigInteger>> matrix) {
        for (int row = 0; row < matrix.size(); row++) {
            if (matrix.get(row).get(row).equals(BigInteger.ZERO)) {
                // not needed for my current input but just to make sure that it works for all inputs
                for (int n = row + 1; n < matrix.size(); n++) {
                    var replacement = matrix.get(n).get(row);
                    if (!replacement.equals(BigInteger.ZERO)) {
                        var row1 = matrix.get(row);
                        var addendum = matrix.get(n);
                        for (int i = 0; i < row1.size(); i++) {
                            row1.set(i, row1.get(i).multiply(BigInteger.ONE).add(addendum.get(i).multiply(BigInteger.ONE)));
                        }
                        break;
                    }
                }
            }
            for (int nextRow = row + 1; nextRow < matrix.size(); nextRow++) {
                pivot(matrix, row, nextRow);
            }
        }
    }

    private void reducedRowEchelon(List<ArrayList<BigInteger>> matrix) {
        for (int row = matrix.size() - 1; row >= 0; row--) {
            if (matrix.get(row).get(row).compareTo(BigInteger.ZERO) < 0) {
                matrix.get(row).replaceAll(BigInteger::negate);
            }
            for (int nextRow = row - 1; nextRow >= 0; nextRow--) {
                pivot(matrix, row, nextRow);
            }
        }
    }

    List<Long> solve(List<List<Long>> matrix) {
        // convert to BigInteger to avoid rounding errors, even though the solutions are all integers
        List<ArrayList<BigInteger>> copy = new ArrayList<>();
        for (var row : matrix) {
            var rowCopy = new ArrayList<>(row.stream().map(BigInteger::valueOf).toList());
            copy.add(rowCopy);
        }
        rowEchelon(copy);
        reducedRowEchelon(copy);
        List<Long> solution = new ArrayList<>();
        for (int row = 0; row < matrix.size(); row++) {
            var diagonal = copy.get(row).get(row);
            var rhs = copy.get(row).get(matrix.size());
            if (!rhs.mod(diagonal).equals(BigInteger.ZERO)) {
                throw new IllegalStateException();
            }
            solution.add(rhs.divide(diagonal).longValue());
        }
        return solution;
    }
}

public class Day24 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}
