package year2023.day1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class Puzzle {
    private final String words = "one|two|three|four|five|six|seven|eight|nine";
    private final String reversedWords = reverse(words);
    private final Map<String, Integer> digits = new HashMap<>();
    private final Map<String, Integer> reverseDigits = new HashMap<>();
    private final List<String> lines;

    static String reverse(String s) {
        return new StringBuilder().append(s).reverse().toString();
    }

    Puzzle() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day1_input")); var reader = new BufferedReader(new InputStreamReader(input))) {
            lines = reader.lines().toList();
        }
        var wordList = Arrays.stream(words.split("\\|")).toList();
        for (int i = 0; i < wordList.size(); i++) {
            digits.put(wordList.get(i), i + 1);
        }
        var reverseWordList = Arrays.stream(reversedWords.split("\\|")).toList();
        for (int i = 0; i < reverseWordList.size(); i++) {
            reverseDigits.put(reverseWordList.get(i), 9 - i);
        }
    }

    public static void main(String[] args) throws Exception {
        var puzzle = new Puzzle();
        System.out.println(puzzle.solveBadly());
        System.out.println(puzzle.solveBetter());
    }

    int digit(String digitString) {
        return digits.containsKey(digitString) ? digits.get(digitString) : Integer.parseInt(digitString);
    }

    int reverseDigit(String digitString) {
        return reverseDigits.containsKey(digitString) ? reverseDigits.get(digitString) : Integer.parseInt(digitString);
    }

    int solve(String pattern, String reversePattern) {
        var left = lines.stream().map(s -> digit(s.replaceAll(pattern, "$1"))).toList();
        var right = lines.stream().map(Puzzle::reverse).map(s -> reverseDigit(s.replaceAll(reversePattern, "$1"))).toList();
        return IntStream.range(0, left.size()).map(i -> 10 * left.get(i) + right.get(i)).sum();
    }

    int solveBadly() {
        String firstDigit = ".*?([1-9]).*";
        return solve(firstDigit, firstDigit);
    }

    int solveBetter() {
        String firstDigitReally = ".*?([1-9]|" + words + ").*";
        String firstDigitReallyReversed = ".*?([1-9]|" + reversedWords + ").*";
        return solve(firstDigitReally, firstDigitReallyReversed);
    }
}
