package year2023.day4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Day4 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(ScratchCards::solve);
        new Puzzle().solve(ScratchCards::solve2);
    }
}

class Score {
    int value = 0;
    void increase() {
        value = value == 0 ? 1 : value << 1;
    }
}

record Card(int id, int matches, int instances) {
    Card(int id, Set<Integer> winningNumbers, Set<Integer> numbers) {
        this(id, matches(winningNumbers, numbers), 1);
    }

    static int matches(Set<Integer> winningNumbers, Set<Integer> numbers) {
        Set<Integer> copy = new HashSet<>(numbers);
        copy.retainAll(winningNumbers);
        return copy.size();
    }

    int score() {
        var score = new Score();
        IntStream.range(0, matches).forEach(z -> score.increase());
        return score.value;
    }
}

class ScratchCards {
    private static final Pattern PATTERN = Pattern.compile("Card +(\\d+): (.+) \\| (.+)");
    private static final Pattern DIGITS = Pattern.compile("(\\d+)");
    private final Map<Integer, Card> cards = new TreeMap<>();

    Set<Integer> numbers(String line) {
        Set<Integer> numbers = new HashSet<>();
        for (Matcher m = DIGITS.matcher(line); m.find(); ) {
            numbers.add(Integer.parseInt(m.group(1)));
        }
        return numbers;
    }

    void parse(String line) {
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        int id = Integer.parseInt(matcher.group(1));
        cards.put(id, new Card(id, numbers(matcher.group(2)), numbers(matcher.group(3))));
    }

    void solve() {
        System.out.println(cards.values().stream().mapToInt(Card::score).sum());
    }

    void solve2() {
        cards.values().stream().map(Card::id).forEach(i -> {
            var card = cards.get(i);
            IntStream.range(0, card.matches()).forEach(j -> cards.computeIfPresent(i + j + 1, (k, v) -> new Card(v.id(), v.matches(), v.instances() + card.instances())));
        });
        System.out.println(cards.values().stream().mapToInt(Card::instances).sum());
    }
}

class Puzzle {
    void solve(Consumer<ScratchCards> solver) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day4/day4_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            ScratchCards cards = new ScratchCards();
            reader.lines().forEach(cards::parse);
            solver.accept(cards);
        }
    }
}
