package year2022.day11;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Puzzle {
    final List<String> blocks;

    Puzzle() throws Exception {
        var input = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day11_input")).toURI()));
        blocks = Arrays.stream(input.split("\n\n")).toList();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(Monkeys.from(blocks, false).business());
        System.out.println(Monkeys.from(blocks, true).business());
    }
}

record Monkeys(SortedMap<Integer, Monkey> monkeys, boolean worried, long mod) {
    static Monkeys from(List<String> blocks, boolean worried) {
        SortedMap<Integer, Monkey> monkeys = new TreeMap<>();
        for (var lines : blocks) {
            var monkey = Monkey.from(lines, monkeys);
            monkeys.put(monkey.id(), monkey);
        }
        BigInteger b = monkeys.values().stream().map(m -> BigInteger.valueOf(m.divisor())).reduce(BigInteger.ONE, BigInteger::multiply);
        if (b.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException();
        }
        return new Monkeys(monkeys, worried, b.longValue());
    }

    long business() {
        var rounds = worried ? 10000 : 20;
        for (var round = 0; round < rounds; round++) {
            for (Integer i : monkeys.keySet()) {
                monkeys.get(i).passItems(worried, mod);
            }
        }
        var top2 = monkeys.keySet().stream().map(i -> monkeys.get(i).inspectedItems().get()).sorted(Collections.reverseOrder()).limit(2).toList();
        return (long) top2.get(0) * top2.get(1);
    }
}

record Monkey(
        int id,
        List<Long> items,
        Function<Long, Long> operation,
        long divisor,
        int trueMonkey,
        int falseMonkey,
        Map<Integer, Monkey> monkeys,
        AtomicInteger inspectedItems
) {
    static final Pattern OP_PATTERN = Pattern.compile(" {2}Operation: new = old (.) (.*)");

    static Function<Long, Long> buildOperation(String operator, String right) {
        if ("old".equals(right)) {
            return switch (operator) {
                case "*" -> old -> old * old;
                case "+" -> old -> old + old;
                default -> throw new IllegalArgumentException();
            };
        } else {
            long i = Long.parseLong(right);
            return switch (operator) {
                case "*" -> old -> old * i;
                case "+" -> old -> old + i;
                default -> throw new IllegalArgumentException();
            };
        }
    }

    void passItems(boolean worried, long mod) {
        var it = items.iterator();
        while (it.hasNext()) {
            long item = it.next();
            if (worried) {
                item = item % mod;
            }
            it.remove();
            inspectedItems.incrementAndGet();
            long newLevel = operation.apply(item);
            if (!worried) {
                newLevel /= 3;
            }
            int destination = newLevel % divisor == 0 ? trueMonkey : falseMonkey;
            monkeys.get(destination).items.add(newLevel);
        }
    }

    static Monkey from(String lines, Map<Integer, Monkey> monkeys) {
        Integer id = null;
        List<Long> items = new ArrayList<>();
        Function<Long, Long> operation = null;
        Long divisor = null;
        Integer trueMonkey = null;
        Integer falseMonkey = null;
        for (String line : lines.split("\n")) {
            if (line.startsWith("Monkey ")) {
                id = Integer.parseInt(line.replaceAll("Monkey (\\d+):", "$1"));
            } else if (line.startsWith("  Starting items: ")) {
                for (String item : line.replace("  Starting items: ", "").split(", ")) {
                    items.add(Long.parseLong(item));
                }
            } else if (line.startsWith("  Operation: new = ")) {
                var m = OP_PATTERN.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException();
                }
                operation = buildOperation(m.group(1), m.group(2));
            } else if (line.startsWith("  Test: divisible by ")) {
                divisor = Long.parseLong(line.replace("  Test: divisible by ", ""));
            } else if (line.startsWith("    If true: throw to monkey ")) {
                trueMonkey = Integer.parseInt(line.replace("    If true: throw to monkey ", ""));
            } else if (line.startsWith("    If false: throw to monkey ")) {
                falseMonkey = Integer.parseInt(line.replace("    If false: throw to monkey ", ""));
            }
        }
        return new Monkey(
                Objects.requireNonNull(id),
                items,
                operation,
                Objects.requireNonNull(divisor),
                Objects.requireNonNull(trueMonkey),
                Objects.requireNonNull(falseMonkey),
                monkeys,
                new AtomicInteger()
        );
    }
}
