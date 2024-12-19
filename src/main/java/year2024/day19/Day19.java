package year2024.day19;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Day19 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Set<String> towels;
    final List<String> patterns;
    final int maxLen;

    Puzzle() throws Exception {
        var input = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day19_input")).toURI()));
        var parts = input.split("\n\n");
        towels = Arrays.stream(parts[0].split(", ")).collect(Collectors.toSet());
        patterns = Arrays.stream(parts[1].split("\n")).toList();
        maxLen = towels.stream().mapToInt(String::length).max().orElseThrow();
    }

    long solutions(String s) {
        if (towels.contains(s)) {
            return 1;
        }
        int l = s.length();
        int i = (l - maxLen) / 2;
        long total = 0;
        for (int j = 0; j < maxLen; j++) {
            int k = i + j;
            if (k <= 0 || k >= l) {
                continue;
            }
            var left = s.substring(0, k);
            var right = s.substring(k);
            total += solutions(left) * solutions(right);
        }
        return total;
    }

    void solve() {
        // 228 is too low
        System.out.println(patterns.stream().filter(p -> solutions(p) > 0).count());
    }
}
