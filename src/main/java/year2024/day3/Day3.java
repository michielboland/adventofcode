package year2024.day3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day3 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    void solve() throws IOException {
        var pattern = Pattern.compile("\\((\\d{1,3}),(\\d{1,3})\\).*");
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day3_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            long sum = reader.lines()
                    .flatMap(s -> Arrays.stream(s.split("mul")))
                    .map(pattern::matcher)
                    .filter(Matcher::matches)
                    .peek(m -> System.out.println(m.group(0)))
                    .mapToLong(m -> Long.parseLong(m.group(1)) * Long.parseLong(m.group(2)))
                    .sum();
            System.out.println(sum);
        }
    }
}