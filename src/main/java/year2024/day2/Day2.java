package year2024.day2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day2 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day2_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var reports = Reports.parse(reader.lines());
            System.out.println(reports.numberOfSafeReports());
            System.out.println(reports.numberOfSafeReportsAfterDisardingOne());
        }
    }
}

record Report(List<Integer> levels) {
    static Report from(String line) {
        return new Report(Arrays.stream(line.split("\\s+")).map(Integer::valueOf).toList());
    }

    private Report discard(int which) {
        return new Report(
                IntStream.range(0, levels.size())
                        .filter(i -> i != which)
                        .mapToObj(levels::get)
                        .toList()
        );
    }

    private Stream<Report> discardOne() {
        return IntStream.range(0, levels.size())
                .mapToObj(this::discard);
    }

    boolean isSafe() {
        int signum = 0;
        for (var i = 0; i < levels.size(); i++) {
            if (i == 1) {
                signum = levels.get(i).compareTo(levels.get(0));
                if (signum == 0) {
                    return false;
                }
            }
            if (i > 0) {
                int delta = signum * (levels.get(i) - levels.get(i - 1));
                if (delta < 1 || delta > 3) {
                    return false;
                }
            }
        }
        return true;
    }

    boolean isSafeAfterDiscardingOne() {
        if (isSafe()) {
            return true;
        }
        return discardOne().anyMatch(Report::isSafe);
    }
}

record Reports(List<Report> reports) {
    static Reports parse(Stream<String> lines) {
        return new Reports(lines.map(Report::from).toList());
    }

    long numberOfSafeReports() {
        return reports.stream()
                .filter(Report::isSafe).count();
    }

    long numberOfSafeReportsAfterDisardingOne() {
        return reports.stream()
                .filter(Report::isSafeAfterDiscardingOne).count();
    }
}
