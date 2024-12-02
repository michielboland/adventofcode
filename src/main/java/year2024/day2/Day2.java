package year2024.day2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
        }
    }
}

record Report(List<Integer> levels) {
    static Report from(String line) {
        return new Report(Arrays.stream(line.split("\\s+")).map(Integer::valueOf).toList());
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
}

record Reports(List<Report> reports) {
    static Reports parse(Stream<String> lines) {
        return new Reports(lines.map(Report::from).toList());
    }

    long numberOfSafeReports() {
        return reports.stream()
                .peek(r -> System.out.println(r + " safe: " + r.isSafe()))
                .filter(Report::isSafe).count();
    }
}
