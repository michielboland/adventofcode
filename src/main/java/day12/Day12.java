package day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

enum Condition {
    OPERATIONAL('.'), DAMAGED('#'), UNKNOWN('?');
    final char symbol;

    Condition(char symbol) {
        this.symbol = symbol;
    }

    static Condition from(int symbol) {
        return Arrays.stream(values()).filter(c -> c.symbol == symbol).findFirst().orElseThrow();
    }

    List<Condition> repeat(int n) {
        return IntStream.range(0, n).mapToObj(z -> this).toList();
    }

    boolean matches(Condition other) {
        return this == other || this == UNKNOWN || other == UNKNOWN;
    }
}

record SpringConditions(List<Condition> conditions) {
    static SpringConditions from(String line) {
        return new SpringConditions(line.chars().mapToObj(Condition::from).toList());
    }

    boolean matches(SpringConditions other) {
        if (conditions.size() != other.conditions.size()) {
            throw new IllegalArgumentException();
        }
        return IntStream.range(0, conditions.size()).allMatch(i -> conditions.get(i).matches(other.conditions.get(i)));
    }
}

record GroupSizesOfDamagedSprings(List<Integer> sizes) {
    static GroupSizesOfDamagedSprings from(String line) {
        return new GroupSizesOfDamagedSprings(Arrays.stream(line.split(",")).map(Integer::parseInt).toList());
    }

    SpringConditions weave(List<Integer> otherSizes, boolean inside) {
        Iterator<Integer> i = sizes.iterator();
        Iterator<Integer> j = otherSizes.iterator();
        List<Condition> conditions = new ArrayList<>();
        do {
            if (inside) {
                if (i.hasNext()) {
                    conditions.addAll(Condition.DAMAGED.repeat(i.next()));
                }
                if (j.hasNext()) {
                    conditions.addAll(Condition.OPERATIONAL.repeat(j.next()));
                }
            } else {
                if (j.hasNext()) {
                    conditions.addAll(Condition.OPERATIONAL.repeat(j.next()));
                }
                if (i.hasNext()) {
                    conditions.addAll(Condition.DAMAGED.repeat(i.next()));
                }
            }
        } while (i.hasNext() || j.hasNext());
        return new SpringConditions(conditions);
    }
}

record ConditionRecord(SpringConditions springConditions, GroupSizesOfDamagedSprings groupSizesOfDamagedSprings) {
    static final Partitioner PARTITIONER = new Partitioner();

    static ConditionRecord from(String line) {
        var parts = line.split(" ");
        return new ConditionRecord(SpringConditions.from(parts[0]), GroupSizesOfDamagedSprings.from(parts[1]));
    }

    long arrangements() {
        var groups = groupSizesOfDamagedSprings.sizes().size();
        var undamagedSprings = springConditions.conditions().size() - groupSizesOfDamagedSprings.sizes().stream().mapToInt(i -> i).sum();
        List<SpringConditions> toTest = new ArrayList<>();
        var inside = PARTITIONER.orderedPartitionsOfSize(groups - 1, undamagedSprings);
        var comb = PARTITIONER.orderedPartitionsOfSize(groups, undamagedSprings);
        var outside = PARTITIONER.orderedPartitionsOfSize(groups + 1, undamagedSprings);
        toTest.addAll(inside.stream().map(l -> groupSizesOfDamagedSprings.weave(l, true)).toList());
        toTest.addAll(comb.stream().map(l -> groupSizesOfDamagedSprings.weave(l, true)).toList());
        toTest.addAll(comb.stream().map(l -> groupSizesOfDamagedSprings.weave(l, false)).toList());
        toTest.addAll(outside.stream().map(l -> groupSizesOfDamagedSprings.weave(l, false)).toList());
        return toTest.stream().filter(s -> s.matches(springConditions)).count();
    }
}

record Springs(List<ConditionRecord> conditionRecords) {
    static Springs parse(Stream<String> lines) {
        return new Springs(lines.map(ConditionRecord::from).toList());
    }

    long arrangements() {
        return conditionRecords.stream().mapToLong(ConditionRecord::arrangements).sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day12/day12_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var springs = Springs.parse(reader.lines());
            System.out.println(springs.arrangements());
        }
    }
}

class Partitioner {
    private final Map<NT, List<List<Integer>>> CACHE = new HashMap<>();

    List<Integer> append(List<Integer> other, int n) {
        return Stream.concat(other.stream(), Stream.of(n)).toList();
    }

    List<List<Integer>> orderedPartitionsOfSize(int numberOfElements, int total) {
        NT key = new NT(numberOfElements, total);
        if (CACHE.containsKey(key)) {
            return CACHE.get(key);
        }
        if (numberOfElements < 1) {
            throw new IllegalArgumentException();
        }
        if (numberOfElements == 1) {
            return List.of(List.of(total));
        }
        var r = IntStream.range(1, total).boxed().flatMap(n -> orderedPartitionsOfSize(numberOfElements - 1, total - n).stream().map(l -> append(l, n))).toList();
        CACHE.put(key, r);
        return r;
    }

    record NT(int numberOfElements, int total) {
    }
}

public class Day12 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}
