package year2024;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Day1 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day1_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var listPair = ListPair.parse(reader.lines());
            listPair.print();
        }
    }
}

record Pair(int left, int right) {

    static Pair from(String s) {
        var l = Arrays.stream(s.split("\\s+")).map(Integer::valueOf).toList();
        return new Pair(l.get(0), l.get(1));
    }
}

record ListPair(List<Integer> left, List<Integer> right){

    static ListPair parse(Stream<String> lines) {
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (Pair pair : lines.map(Pair::from).toList()) {
            left.add(pair.left());
            right.add(pair.right());
        }
        left.sort(Integer::compareTo);
        right.sort(Integer::compareTo);
        return new ListPair(left, right);
    }

    int difference() {
        int difference = 0;
        for (var i = 0; i < left.size(); i++) {
            difference += Math.abs(left.get(i) - right.get(i));
        }
        return difference;
    }

    long similarityScore() {
        long similarityScore = 0;
        for (var id : left) {
            similarityScore += id * right.stream().filter(id::equals).count();
        }
        return similarityScore;
    }

    void print() {
        System.out.println("difference = " + difference());
        System.out.println("similarity score = " + similarityScore());
    }
}
