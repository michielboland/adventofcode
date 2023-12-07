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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

enum Card {
    ACE("A"), KING("K"), QUEEN("Q"), JACK("J"), TEN("T"), NINE("9"), EIGHT("8"), SEVEN("7"), SIX("6"), FIVE("5"), FOUR("4"), THREE("3"), TWO("2");
    final String label;

    Card(String label) {
        this.label = label;
    }

    static Card from(String s) {
        return Arrays.stream(values()).filter(c -> c.label.equals(s)).findFirst().orElseThrow();
    }
}

enum Type {
    FIVE_OF_A_KIND, FOUR_OF_A_KIND, FULL_HOUSE, THREE_OF_A_KIND, TWO_PAIR, ONE_PAIR, HIGH_CARD
}

public class Day7 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(Game::printWinnings);
    }
}

record Hand(Card[] cards, int bid) implements Comparable<Hand> {
    static Hand from(String s) {
        String[] parts = s.split(" ");
        return new Hand(parts[0].chars().mapToObj(i -> Card.from(String.valueOf((char) i))).toArray(Card[]::new), Integer.parseInt(parts[1]));
    }

    Type type() {
        Map<Card, Integer> map = new HashMap<>();
        Arrays.stream(cards).forEach(c -> map.compute(c, (k, v) -> v == null ? 1 : v + 1));
        int max = map.values().stream().mapToInt(i -> i).max().orElseThrow();
        return switch (map.size()) {
            case 1 -> Type.FIVE_OF_A_KIND;
            case 2 -> switch (max) {
                case 4 -> Type.FOUR_OF_A_KIND;
                case 3 -> Type.FULL_HOUSE;
                default -> throw new IllegalArgumentException();
            };
            case 3 -> switch (max) {
                case 3 -> Type.THREE_OF_A_KIND;
                case 2 -> Type.TWO_PAIR;
                default -> throw new IllegalArgumentException();
            };
            case 4 -> Type.ONE_PAIR;
            case 5 -> Type.HIGH_CARD;
            default -> throw new IllegalArgumentException();
        };
    }

    @Override
    public String toString() {
        return Arrays.stream(cards).map(c -> c.label).collect(Collectors.joining()) + " " + bid;
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

record Game(List<Hand> hands) {
    Game() {
        this(new ArrayList<>());
    }

    void parse(String line) {
        hands.add(Hand.from(line));
    }

    void printWinnings() {
        hands.sort(Comparator.reverseOrder());
        System.out.println(IntStream.range(0, hands.size()).map(i -> hands.get(i).bid() * (i + 1)).sum());
    }
}

class Puzzle {
    void solve(Consumer<Game> solver) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day7/day_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var game = new Game();
            reader.lines().forEach(game::parse);
            solver.accept(game);
        }
    }
}
