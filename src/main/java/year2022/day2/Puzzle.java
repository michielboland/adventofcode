package year2022.day2;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

enum Hand {
    ROCK(1), PAPER(2), SCISSORS(3);

    final int score;

    Hand(int score) {
        this.score = score;
    }

    boolean beats(Hand other) {
        return score % 3 == (other.score + 1) % 3;
    }
}

record Round(Hand me, Hand opponent) {
    Round(String line, Map<Character, Hand> opponentMap, Map<Character, Hand> elfMap) {
        this(elfMap.get(line.charAt(2)), opponentMap.get(line.charAt(0)));
    }

    int score() {
        return me.score + (me == opponent ? 3 : me.beats(opponent) ? 6 : 0);
    }
}

record Tournament(List<Round> rounds) {
    static Tournament from(List<String> input, Map<Character, Hand> opponentMap, Map<Character, Hand> elfMap) {
        return new Tournament(input.stream().map(line -> new Round(line, opponentMap, elfMap)).toList());
    }

    int score() {
        return rounds.stream().mapToInt(Round::score).sum();
    }
}

public class Puzzle {
    private final Map<Character, Hand> opponentMap = Map.of('A', Hand.ROCK, 'B', Hand.PAPER, 'C', Hand.SCISSORS);
    private final Map<Character, Hand> elfMap = Map.of('X', Hand.ROCK, 'Y', Hand.PAPER, 'Z', Hand.SCISSORS);
    private final List<String> input;

    Puzzle() throws Exception {
        input = Arrays.stream(Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day2_input")).toURI())).split("\n")).toList();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(part1());
    }

    int part1() {
        return Tournament.from(input, opponentMap, elfMap).score();
    }
}
