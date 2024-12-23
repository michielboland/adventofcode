package year2024.day23;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day23 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final LanParty lanParty;

    Puzzle() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day23_input"))) {
            lanParty = new LanParty(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        System.out.println(lanParty.countTripletsWithT());
    }
}

record LanParty(Set<Set<String>> computerPairs) {
    LanParty(Stream<String> lines) {
        this(lines.map(LanParty::pair).collect(Collectors.toSet()));
    }

    static Set<String> pair(String line) {
        return Arrays.stream(line.split("-")).collect(Collectors.toSet());
    }

    public int countTripletsWithT() {
        Set<String> computersWithT = computerPairs.stream().flatMap(Set::stream).filter(s -> s.startsWith("t")).collect(Collectors.toSet());
        Set<Set<String>> tripletsWithT = new HashSet<>();
        for (var pair : computerPairs) {
            for (var t : computersWithT) {
                if (pair.stream().allMatch(s -> !s.equals(t) && computerPairs.contains(Set.of(s, t)))) {
                    var triplet = new HashSet<>(pair);
                    triplet.add(t);
                    tripletsWithT.add(triplet);
                }
            }
        }
        return tripletsWithT.size();
    }
}
