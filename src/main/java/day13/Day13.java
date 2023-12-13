package day13;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day13 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Mirror(List<String> rows, List<String> columns) {
    static Mirror from(List<String> rows) {
        List<String> columns = IntStream.range(0, rows.get(0).length()).mapToObj(i -> rows.stream().map(row -> row.charAt(i)).map(String::valueOf).collect(Collectors.joining())).toList();
        return new Mirror(rows, columns);
    }

    private static boolean isReallyIt(final int i, List<String> strings) {
        int l = i - 1;
        int r = i;
        final int s = strings.size();
        while (l >= 0 && r < s) {
            if (!strings.get(l).equals(strings.get(r))) {
                return false;
            }
            --l;
            ++r;
        }
        return true;
    }

    long summary() {
        int x = axis(columns);
        int y = axis(rows);
        if (x == 0 && y == 0 || x > 0 && y > 0) {
            throw new IllegalStateException();
        }
        return x + 100L * y;
    }

    private int axis(List<String> strings) {
        List<Integer> axes = IntStream.range(1, strings.size()).filter(i -> strings.get(i - 1).equals(strings.get(i))).filter(i -> isReallyIt(i, strings)).boxed().toList();
        return switch (axes.size()) {
            case 0 -> 0;
            case 1 -> axes.get(0);
            default -> throw new IllegalStateException();
        };
    }
}

record Mirrors(List<Mirror> mirrors) {
    static Mirrors parse(Stream<String> lines) {
        List<List<String>> blocks = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        lines.forEach(line -> {
            if (line.isEmpty()) {
                counter.incrementAndGet();
            } else {
                if (counter.get() == blocks.size()) {
                    blocks.add(new ArrayList<>());
                }
                blocks.get(counter.get()).add(line);
            }
        });
        return new Mirrors(blocks.stream().map(Mirror::from).toList());
    }

    long summary() {
        return mirrors.stream().mapToLong(Mirror::summary).sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day13/day13_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var mirrors = Mirrors.parse(reader.lines());
            System.out.println(mirrors.summary());
        }
    }
}
