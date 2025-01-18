package year2022.day13;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Puzzle {
    final List<Pair> pairs;

    Puzzle() throws Exception {
        var input = Files.readString(Path.of(Objects.requireNonNull(getClass().getResource("day13_input")).toURI()));
        pairs = Arrays.stream(input.split("\n\n")).map(Pair::from).toList();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(part1());
    }

    int part1() {
        int total = 0;
        for (int i = 0; i < pairs.size(); i++) {
            if (pairs.get(i).isSorted()) {
                total += i + 1;
            }
        }
        return total;
    }
}

record Pair(Item left, Item right) {
    static Pair from(String string) {
        var parts = string.split("\n");
        return new Pair(Item.parse(parts[0]), Item.parse(parts[1]));
    }

    boolean isSorted() {
        return right.compareTo(left) >= 0;
    }
}

record Item(Integer value, List<Item> list) implements Comparable<Item> {

    @Override
    public String toString() {
        return value != null ? value.toString() : "[" + list.stream().map(Item::toString).collect(Collectors.joining(",")) + "]";
    }

    static Item parse(String string) {
        if (string.startsWith("[")) {
            if (!string.endsWith("]")) {
                throw new IllegalArgumentException();
            }
            return new Item(null, parseList(string.substring(1, string.length() - 1)));
        }
        return new Item(Integer.parseInt(string), null);
    }

    static List<Item> parseList(String string) {
        if (string.isEmpty()) {
            return List.of();
        }
        char[] tmp = new char[string.length()];
        int depth = 0;
        for (int i = 0; i < string.length(); i++) {
            final var c = string.charAt(i);
            var nc = c;
            switch (c) {
                case '[' -> ++depth;
                case ']' -> --depth;
                case ',' -> nc = depth == 0 ? '/' : c;
            }
            tmp[i] = nc;
        }
        var parts = new String(tmp).split("/");
        return Arrays.stream(parts).map(Item::parse).toList();
    }

    private List<Item> forceList() {
        return list != null ? list : List.of(new Item(Objects.requireNonNull(value), null));
    }

    @Override
    public int compareTo(Item o) {
        if (value != null && o.value != null) {
            return value.compareTo(o.value);
        }
        List<Item> left = forceList();
        List<Item> right = o.forceList();
        int i = 0;
        while (i < left.size() && i < right.size()) {
            var comparison = left.get(i).compareTo(right.get(i));
            if (comparison != 0) {
                return comparison;
            }
            ++i;
        }
        return Integer.compare(left.size(), right.size());
    }
}
