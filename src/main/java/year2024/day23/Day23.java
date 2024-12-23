package year2024.day23;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Day23 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    private final Set<Set<String>> computerPairs;
    private final Set<String> computers; // slight speedup, not really needed

    Puzzle() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day23_input"))) {
            computerPairs = new BufferedReader(new InputStreamReader(input)).lines().map(Puzzle::pair).collect(Collectors.toSet());
            computers = computerPairs.stream().flatMap(Set::stream).collect(Collectors.toSet());
        }
    }

    static Set<String> pair(String line) {
        return Arrays.stream(line.split("-")).collect(Collectors.toSet());
    }

    long countTripletsWithT(Set<Set<String>> triplets) {
        return triplets.stream().filter(s -> s.stream().anyMatch(t -> t.startsWith("t"))).count();
    }

    Set<Set<String>> embiggen(Set<Set<String>> cliques) {
        Set<Set<String>> embiggened = new HashSet<>();
        for (var clique : cliques) {
            var others = new HashSet<>(computers);
            others.removeAll(clique);
            for (var other : others) {
                if (clique.stream().allMatch(s -> computerPairs.contains(Set.of(s, other)))) {
                    var biggerClique = new HashSet<>(clique);
                    biggerClique.add(other);
                    embiggened.add(biggerClique);
                }
            }
        }
        return embiggened;
    }

    void solve() {
        var cliques = embiggen(computerPairs);
        System.out.println(countTripletsWithT(cliques));
        for (var bigger = embiggen(cliques); !bigger.isEmpty(); bigger = embiggen(bigger)) {
            cliques = bigger;
        }
        System.out.println(cliques.iterator().next().stream().sorted(String::compareTo).collect(Collectors.joining(",")));
    }
}
