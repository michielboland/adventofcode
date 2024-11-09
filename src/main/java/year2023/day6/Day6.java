package year2023.day6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day6 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(false);
        new Puzzle().solve(true);
    }
}

record Record(long time, long distance) {
    long waysToBeat() {
        long d = time * time - 4 * distance - 1;
        if (d <= 0) {
            return 0;
        }
        double left = Math.ceil((time - Math.sqrt(d)) / 2.0);
        double right = Math.floor((time + Math.sqrt(d)) / 2.0);
        return (long) (right - left + 1);
    }
}

record Races(Set<Record> records) {
    private static final Pattern DIGITS = Pattern.compile("(\\d+)");

    Races() {
        this(new HashSet<>());
    }

    void parse(String times, String distances, boolean badKerning) {
        if (badKerning) {
            times = times.replace(" ", "");
            distances = distances.replace(" ", "");
        }
        for (Matcher tm = DIGITS.matcher(times), dm = DIGITS.matcher(distances); tm.find() && dm.find(); ) {
            records.add(new Record(Long.parseLong(tm.group(1)), Long.parseLong(dm.group(1))));
        }
    }

    void solve() {
        System.out.println(records.stream().mapToLong(Record::waysToBeat).reduce(1L, (a, b) -> a * b));
    }
}

class Puzzle {
    void solve(boolean badKerning) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day6/day6_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var races = new Races();
            races.parse(reader.readLine(), reader.readLine(), badKerning);
            races.solve();
        }
    }
}
