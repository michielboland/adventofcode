package year2022.day2;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

enum Hand {
    ROCK(0), PAPER(1), SCISSORS(2);

    private final int value;

    Hand(int value) {
        this.value = value;
    }

    int score() {
        return value + 1;
    }

    static Hand from(int value) {
        return Arrays.stream(values()).filter(hand -> hand.value == value % 3).findFirst().orElseThrow();
    }

    Hand winner() {
        return from(value + 1);
    }

    Hand loser() {
        return from(value + 2);
    }

    boolean beats(Hand other) {
        return value % 3 == (other.value + 1) % 3;
    }
}

record Round(Hand me, Hand opponent) {
    private static final Map<Character, Hand> OPPONENT_MAP = Map.of('A', Hand.ROCK, 'B', Hand.PAPER, 'C', Hand.SCISSORS);
    private static final Map<Character, Hand> ELF_MAP = Map.of('X', Hand.ROCK, 'Y', Hand.PAPER, 'Z', Hand.SCISSORS);
    private static final Map<Character, Function<Hand, Hand>> STRATEGY_MAP = Map.of('X', Hand::loser, 'Y', Function.identity(), 'Z', Hand::winner);

    static Round guess(String line) {
        return new Round(ELF_MAP.get(line.charAt(2)), OPPONENT_MAP.get(line.charAt(0)));
    }

    static Round withStrategy(String line) {
        return new Round(STRATEGY_MAP.get(line.charAt(2)), OPPONENT_MAP.get(line.charAt(0)));
    }

    public Round(Function<Hand, Hand> strategy, Hand hand) {
        this(strategy.apply(hand), hand);
    }

    int score() {
        return me.score() + (me == opponent ? 3 : me.beats(opponent) ? 6 : 0);
    }
}

record Tournament(List<Round> rounds) {
    static Tournament guess(List<String> input) {
        return new Tournament(input.stream().map(Round::guess).toList());
    }

    static Tournament withStrategy(List<String> input) {
        return new Tournament(input.stream().map(Round::withStrategy).toList());
    }

    int score() {
        return rounds.stream().mapToInt(Round::score).sum();
    }
}

public class Puzzle {
    private final List<String> input;

    Puzzle() throws Exception {
        input = Arrays.stream(Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day2_input")).toURI())).split("\n")).toList();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(part1());
        System.out.println(part2());
    }

    int part1() {
        return Tournament.guess(input).score();
    }

    int part2() {
        return Tournament.withStrategy(input).score();
    }
}
