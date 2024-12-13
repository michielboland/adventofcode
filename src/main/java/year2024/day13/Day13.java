package year2024.day13;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class Day13 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final List<ClawMachine> clawMachines = new ArrayList<>();

    Puzzle() throws Exception {
        String input = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day13_input")).toURI()));
        for (String instruction : input.split("\\n\\n")) {
            clawMachines.add(ClawMachine.from(instruction));
        }
    }

    void solve() {
        System.out.println(clawMachines.stream().mapToLong(c -> c.tokens(0L)).sum());
        System.out.println(clawMachines.stream().mapToLong(c -> c.tokens(10000000000000L)).sum());
    }
}

record Coordinate(long x, long y) {
    static final Coordinate ZERO = new Coordinate(0L, 0L);

    static Coordinate from(String x, String y) {
        return new Coordinate(Long.parseLong(x), Long.parseLong(y));
    }

    Coordinate mod(long d) {
        return new Coordinate(x % d, y % d);
    }

    Coordinate divide(long d) {
        return new Coordinate(x / d, y / d);
    }
}

record ClawMachine(Coordinate buttonA, Coordinate buttonB, Coordinate prize) {
    static final long BUTTON_A_TOKENS = 3;
    static final long BUTTON_B_TOKENS = 1;
    static final Pattern PATTERN = Pattern.compile("\\+(\\d+),.*\\+(\\d+)\\n.*\\+(\\d+),.*\\+(\\d+)\\n.*=(\\d+),.*=(\\d+)");

    static ClawMachine from(String instruction) {
        var matcher = PATTERN.matcher(instruction);
        if (!matcher.find()) {
            System.err.println(instruction);
            throw new IllegalArgumentException();
        }
        return new ClawMachine(
                Coordinate.from(matcher.group(1), matcher.group(2)),
                Coordinate.from(matcher.group(3), matcher.group(4)),
                Coordinate.from(matcher.group(5), matcher.group(6))
        );
    }

    long determinant() {
        return buttonA.x() * buttonB.y() - buttonA.y() * buttonB.x();
    }

    long tokens(long prizeDistance) {
        long px = prize.x() + prizeDistance;
        long py = prize.y() + prizeDistance;
        Coordinate multiple = new Coordinate(buttonB.y() * px - buttonB.x() * py, -buttonA.y() * px + buttonA.x() * py);
        var d = determinant();
        if (multiple.mod(d).equals(Coordinate.ZERO)) {
            var ab = multiple.divide(d);
            return BUTTON_A_TOKENS * ab.x() + BUTTON_B_TOKENS * ab.y();
        } else {
            return 0L;
        }
    }
}
