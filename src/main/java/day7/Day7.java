package day7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

enum Card {
    ACE('A'), KING('K'), QUEEN('Q'), JACK('J'), TEN('T'), NINE('9'), EIGHT('8'), SEVEN('7'), SIX('6'), FIVE('5'), FOUR('4'), THREE('3'), TWO('2'), JOKER('J');
    final char label;

    Card(char label) {
        this.label = label;
    }

    static Card from(char label, boolean jokers) {
        return jokers && label == 'J' ? JOKER : Arrays.stream(values()).filter(c -> c.label == label).findFirst().orElseThrow();
    }
}

enum Type {
    FIVE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE, THREE_OF_A_KIND, TWO_PAIR, ONE_PAIR, HIGH_CARD
}

public class Day7 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(false);
        new Puzzle().solve(true);
    }
}

record Hand(Card[] cards, int bid) implements Comparable<Hand> {
    static Hand from(String s, boolean jokers) {
        String[] parts = s.split(" ");
        return new Hand(parts[0].chars().mapToObj(i -> Card.from((char) i, jokers)).toArray(Card[]::new), Integer.parseInt(parts[1]));
    }

    Type type() {
        Map<Card, Integer> map = new HashMap<>();
        Arrays.stream(cards).forEach(c -> map.compute(c, (k, v) -> v == null ? 1 : v + 1));
        int max = map.values().stream().mapToInt(i -> i).max().orElseThrow();
        int wildcards = map.getOrDefault(Card.JOKER, 0);
        return switch (map.size()) {
            case 1 -> Type.FIVE_OF_A_KIND;
            case 2 -> switch (max) {
                case 4 -> wildcards == 0 ? Type.FOUR_OF_A_KIND : Type.FIVE_OF_A_KIND;
                case 3 -> wildcards == 0 ? Type.FULL_HOUSE : Type.FIVE_OF_A_KIND;
                default -> throw new IllegalArgumentException();
            };
            case 3 -> switch (max) {
                case 3 -> wildcards == 0 ? Type.THREE_OF_A_KIND : Type.FOUR_OF_A_KIND;
                case 2 -> switch (wildcards) {
                    case 0 -> Type.TWO_PAIR;
                    case 1 -> Type.FULL_HOUSE;
                    case 2 -> Type.FOUR_OF_A_KIND;
                    default -> throw new IllegalArgumentException();
                };
                default -> throw new IllegalArgumentException();
            };
            case 4 -> wildcards == 0 ? Type.ONE_PAIR : Type.THREE_OF_A_KIND;
            case 5 -> wildcards == 0 ? Type.HIGH_CARD : Type.ONE_PAIR;
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public String toString() {
        return Arrays.stream(cards).map(c -> String.valueOf(c.label)).collect(Collectors.joining()) + " " + bid + " " + type();
    }

    @Override
    public int compareTo(Hand o) {
        int typeCompare = type().compareTo(o.type());
        if (typeCompare != 0) {
            return typeCompare;
        }
        return IntStream.range(0, 5).map(i -> cards[i].compareTo(o.cards[i])).filter(i -> i != 0).findFirst().orElseThrow();
    }
}

record Game(List<Hand> hands, boolean jokers) {
    Game(boolean jokers) {
        this(new ArrayList<>(), jokers);
    }

    void parse(String line) {
        hands.add(Hand.from(line, jokers));
    }

    void printWinnings() {
        hands.sort(Comparator.reverseOrder());
        System.out.println(IntStream.range(0, hands.size()).map(i -> hands.get(i).bid() * (i + 1)).sum());
    }
}

class Puzzle {
    void solve(boolean jokers) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day7/day_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var game = new Game(jokers);
            reader.lines().forEach(game::parse);
            game.printWinnings();
        }
    }
}
