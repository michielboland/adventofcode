package day4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day4 {
    public static void main(String[] args) throws IOException {
        new Puzzle1().solve();
    }
}

class Score {
    int value = 0;
    void increase() {
        value = value == 0 ? 1 : value << 1;
    }
}

record Card(int id, Set<Integer> winningNumbers, Set<Integer> numbers) {
    int score() {
        Set<Integer> copy = new HashSet<>(numbers);
        copy.retainAll(winningNumbers);
        var score = new Score();
        copy.forEach(z -> score.increase());
        return score.value;
    }
}

class ScratchCards {
    private static final Pattern PATTERN = Pattern.compile("Card +(\\d+): (.+) \\| (.+)");
    private static final Pattern DIGITS = Pattern.compile("(\\d+)");
    private final List<Card> cards = new ArrayList<>();

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
        cards.add(new Card(Integer.parseInt(matcher.group(1)), numbers(matcher.group(2)), numbers(matcher.group(3))));
    }

    void solve() {
        System.out.println(cards.stream().mapToInt(Card::score).sum());
    }
}

class Puzzle1 {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day4/day4_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            ScratchCards cards = new ScratchCards();
            reader.lines().forEach(cards::parse);
            cards.solve();
        }
    }
}
