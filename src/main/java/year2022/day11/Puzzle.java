package year2022.day11;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;

public class Puzzle {
    final SortedMap<Integer, Monkey> monkeys = new TreeMap<>();

    Puzzle() throws Exception {
        var input = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day11_input")).toURI()));
        for (String lines : input.split("\n\n")) {
            var monkey = Monkey.from(lines, monkeys);
            if (monkeys.containsKey(monkey.id())) {
                throw new IllegalStateException();
            }
            monkeys.put(monkey.id(), monkey);
        }
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        for (int i = 0; i < 20; i++) {
            round();
        }
        var top2 = monkeys.keySet().stream().map(i -> monkeys.get(i).inspectedItems().size()).sorted(Collections.reverseOrder()).limit(2).toList();
        System.out.println(top2.get(0) * top2.get(1));
    }

    void round() {
        for (Integer i : monkeys.keySet()) {
            monkeys.get(i).passItems();
        }
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
        List<Long> inspectedItems
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

    void passItems() {
        var it = items.iterator();
        while (it.hasNext()) {
            long item = it.next();
            it.remove();
            inspectedItems.add(item);
            long newLevel = operation.apply(item) / 3;
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
                new ArrayList<>()
        );
    }
}
