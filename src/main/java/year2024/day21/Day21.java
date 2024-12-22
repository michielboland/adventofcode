package year2024.day21;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

enum Key {
    ACTIVATE('A', "<0,^3,<^,v>"),
    ZERO('0', "^2,>A"),
    ONE('1', "^4,>2"),
    TWO('2', "<1,^5,>3,v0"),
    THREE('3', "<2,^6,vA"),
    FOUR('4', "^7,>5,v1"),
    FIVE('5', "<4,^8,>6,v2"),
    SIX('6', "<5,^9,v3"),
    SEVEN('7', ">8,v4"),
    EIGHT('8', "<7,>9,v5"),
    NINE('9', "<8,v6"),
    UP('^', ">A,vv"),
    DOWN('v', "<<,^^,>>"),
    LEFT('<', ">v"),
    RIGHT('>', "^A,<v");

    static {
        Arrays.stream(values()).forEach(Key::init);
    }

    private final char symbol;
    private final String instructions;
    private final Collection<Neighbour> neighbours = new HashSet<>();

    Key(char symbol, String instructions) {
        this.symbol = symbol;
        this.instructions = instructions;
    }

    static Key from(char symbol) {
        return Arrays.stream(values()).filter(v -> v.symbol == symbol).findFirst().orElseThrow();
    }

    private void init() {
        neighbours.addAll(Arrays.stream(instructions.split(",")).map(Neighbour::new).toList());
    }

    char symbol() {
        return symbol;
    }

    Collection<Neighbour> neighbours() {
        return neighbours;
    }
}

public class Day21 {
    public static void main(String[] args) throws Exception {
        var puzzle = new Puzzle();
        puzzle.solve(2);
        puzzle.solve(25);
    }
}

class Puzzle {
    private final List<String> sequences;

    Puzzle() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day21_input"))) {
            sequences = new BufferedReader(new InputStreamReader(input)).lines().toList();
        }
    }

    void solve(int repeats) {
        var keyPad = new KeyPad(repeats);
        System.out.println(sequences.stream().mapToLong(keyPad::complexity).sum());
    }
}

class KeyPad {
    private static final Map<String, String> INSTRUCTION_CACHE = new HashMap<>();
    private final int repeats;

    KeyPad(int repeats) {
        this.repeats = repeats;
    }

    String instructions(String sequence) {
        var cached = INSTRUCTION_CACHE.get(sequence);
        if (cached != null) {
            return cached;
        }
        Key state;
        var sb = new StringBuilder();
        Key previousState = Key.ACTIVATE;
        for (char c : sequence.toCharArray()) {
            state = Key.from(c);
            sb.append(path(previousState, state));
            previousState = state;
        }
        String instructions = sb.toString();
        INSTRUCTION_CACHE.put(sequence, instructions);
        return instructions;
    }

    Map<String, Long> sequenceMap(String instructions) {
        if (instructions.equals("A")) {
            return Map.of("A", 1L);
        }
        Map<String, Long> map = new HashMap<>();
        for (String part : instructions.split("A")) {
            map.compute(part + "A", (k, v) -> v == null ? 1L : v + 1L);
        }
        return map;
    }

    Map<String, Long> iterate(Map<String, Long> sequenceMap) {
        Map<String, Long> map = new HashMap<>();
        sequenceMap.forEach((sequence, a) ->
                sequenceMap(instructions(sequence)).forEach((instruction, b) ->
                        map.compute(instruction, (unused, c) -> c == null ? a * b : c + a * b)));
        return map;
    }

    Map<String, Long> repeatInstructions(String sequence) {
        var instructions = instructions(sequence);
        Map<String, Long> sequenceMap = sequenceMap(instructions);
        for (int i = 0; i < repeats; i++) {
            sequenceMap = iterate(sequenceMap);
        }
        return sequenceMap;
    }

    long complexity(String sequence) {
        return length(sequence) * value(sequence);
    }

    private long value(String sequence) {
        return Long.parseLong(sequence.replaceAll("A", ""));
    }

    private long length(String sequence) {
        return repeatInstructions(sequence).entrySet().stream().mapToLong(e -> e.getValue() * e.getKey().length()).sum();
    }

    String path(Key from, Key to) {
        var queue = new PriorityQueue<ND>();
        Set<Turn> visited = new HashSet<>();
        queue.add(new ND(from, 0, null, null));
        ND bestPath = null;
        while (!queue.isEmpty()) {
            var current = queue.remove();
            if (current.key() == to) {
                bestPath = current;
                break;
            }
            for (Neighbour neighbour : current.key().neighbours()) {
                var turn = new Turn(current.key(), current.direction(), neighbour.direction(), neighbour.key());
                if (!visited.contains(turn)) {
                    visited.add(turn);
                    queue.add(new ND(neighbour.key(), current.distance() + turn.cost(), neighbour.direction(), current));
                }
            }

        }
        return Objects.requireNonNull(bestPath).path();
    }
}

record Neighbour(Key direction, Key key) {
    Neighbour(String s) {
        this(Key.from(s.charAt(0)), Key.from(s.charAt(1)));
    }
}

record Turn(Key key, Key fromDirection, Key toDirection, Key toKey) {
    int cost() {
        if (fromDirection == null) {
            return switch (toDirection) {
                case LEFT -> 50;
                case UP, DOWN -> 100;
                case RIGHT -> 200;
                default -> throw new IllegalStateException();
            };
        }
        return fromDirection == toDirection ? 1 : 1000;
    }
}

record ND(Key key, int distance, Key direction, ND previous) implements Comparable<ND> {

    @Override
    public int compareTo(ND o) {
        return distance != o.distance ? Integer.compare(distance, o.distance)
                : key != o.key ? key.compareTo(o.key)
                : direction == null ? (o.direction == null ? 0 : -1)
                : o.direction == null ? 1 : direction.compareTo(o.direction);
    }

    String path() {
        List<Key> l = new ArrayList<>();
        l.add(Key.ACTIVATE);
        var nd = this;
        while (nd.direction != null) {
            l.add(nd.direction);
            nd = nd.previous;
        }
        Collections.reverse(l);
        return l.stream().map(k -> String.valueOf(k.symbol())).collect(Collectors.joining());
    }
}
