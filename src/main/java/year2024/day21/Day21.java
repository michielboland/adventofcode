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
        new Puzzle().solve();
    }
}

class Puzzle {
    private final KeyPad keyPad;
    private final List<String> sequences;

    Puzzle() throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day21_input"))) {
            sequences = new BufferedReader(new InputStreamReader(input)).lines().toList();
        }
        keyPad = new KeyPad();
    }

    void solve() {
        // 211214 is too high
        System.out.println(sequences.stream().mapToInt(keyPad::complexity).sum());
    }
}

class KeyPad {
    private final Map<Pair, String> cache = new HashMap<>();

    String instructions(String sequence) {
        var sb = new StringBuilder();
        sb.append(path(Key.ACTIVATE, Key.from(sequence.charAt(0))));
        for (int i = 0; i < sequence.length() - 1; i++) {
            sb.append(path(Key.from(sequence.charAt(i)), Key.from(sequence.charAt(i + 1))));
        }
        return sb.toString();
    }

    String instructions3(String sequence) {
        return instructions(instructions(instructions(sequence)));
    }

    int complexity(String sequence) {
        return instructions3(sequence).length() * Integer.parseInt(sequence.replaceAll("A", ""));
    }

    String path(Key from, Key to) {
        var cached = cache.get(new Pair(from, to));
        if (cached != null) {
            return cached;
        }
        var queue = new PriorityQueue<ND>();
        Set<Turn> visited = new HashSet<>();
        queue.add(new ND(from, 0, null, null));
        ND bestPath = null;
        while (!queue.isEmpty()) {
            var current = queue.remove();
            if (current.key() == to) {
                bestPath = current;
                break;
            } else {
                for (Neighbour neighbour : current.key().neighbours()) {
                    var turn = new Turn(current.key(), current.direction(), neighbour.direction(), neighbour.key());
                    if (!visited.contains(turn)) {
                        visited.add(turn);
                        queue.add(new ND(neighbour.key(), current.distance() + turn.cost(), neighbour.direction(), current));
                    }
                }
            }
        }
        var path = Objects.requireNonNull(bestPath).path();
        cache.put(new Pair(from, to), path);
        return path;
    }
}

record Pair(Key from, Key to) {
}

record Neighbour(Key direction, Key key) {
    Neighbour(String s) {
        this(Key.from(s.charAt(0)), Key.from(s.charAt(1)));
    }
}

record Turn(Key key, Key fromDirection, Key toDirection, Key toKey) {
    int cost() {
        return fromDirection == toDirection ? 1 : 1000;
    }
}

record ND(Key key, int distance, Key direction, ND previous) implements Comparable<ND> {

    @Override
    public int compareTo(ND o) {
        return distance == o.distance ? key.compareTo(o.key) : Integer.compare(distance, o.distance);
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
