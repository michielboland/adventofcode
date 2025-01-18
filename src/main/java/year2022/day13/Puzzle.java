package year2022.day13;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
        System.out.println(part2());
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

    int part2() {
        final Pair divider = Pair.from("""
                [[2]]
                [[6]]
                """);
        List<Packet> packets = new ArrayList<>();
        packets.add(divider.left());
        packets.add(divider.right());
        for (var pair : pairs) {
            packets.add(pair.left());
            packets.add(pair.right());
        }
        packets.sort(Comparator.naturalOrder());
        int i = 1;
        int t = 1;
        for (var packet : packets) {
            if (packet == divider.left() || packet == divider.right()) {
                t *= i;
            }
            ++i;
        }
        return t;
    }
}

record Pair(Packet left, Packet right) {
    static Pair from(String string) {
        var parts = string.split("\n");
        return new Pair(Packet.parse(parts[0]), Packet.parse(parts[1]));
    }

    boolean isSorted() {
        return right.compareTo(left) >= 0;
    }
}

record Packet(Integer value, List<Packet> list) implements Comparable<Packet> {

    @Override
    public String toString() {
        return value != null ? value.toString() : "[" + list.stream().map(Packet::toString).collect(Collectors.joining(",")) + "]";
    }

    static Packet parse(String string) {
        if (string.startsWith("[")) {
            if (!string.endsWith("]")) {
                throw new IllegalArgumentException();
            }
            return new Packet(null, parseList(string.substring(1, string.length() - 1)));
        }
        return new Packet(Integer.parseInt(string), null);
    }

    static List<Packet> parseList(String string) {
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
        return Arrays.stream(parts).map(Packet::parse).toList();
    }

    private List<Packet> forceList() {
        return list != null ? list : List.of(new Packet(Objects.requireNonNull(value), null));
    }

    @Override
    public int compareTo(Packet o) {
        if (value != null && o.value != null) {
            return value.compareTo(o.value);
        }
        List<Packet> left = forceList();
        List<Packet> right = o.forceList();
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
