package day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
}

record SpringConditions(List<Condition> conditions) {
    static SpringConditions from(String line, int n) {
        var dup = IntStream.range(0, n).mapToObj(z -> line).collect(Collectors.joining("?"));
        return new SpringConditions(dup.chars().mapToObj(Condition::from).toList());
    }

    boolean matches(int start, int end) {
        int p = start;
        if (p > 0 && conditions.get(p - 1) == Condition.DAMAGED) {
            return false;
        }
        while (p < end) {
            if (conditions.get(p++) == Condition.OPERATIONAL) {
                return false;
            }
        }
        return p >= conditions.size() || conditions.get(p) != Condition.DAMAGED;
    }

    boolean matches(List<Interval> intervals) {
        int p = 0;
        for (var d : intervals) {
            while (p < d.start()) {
                if (conditions.get(p++) == Condition.DAMAGED) {
                    return false;
                }
            }
            while (p < d.end()) {
                if (conditions.get(p++) == Condition.OPERATIONAL) {
                    return false;
                }
            }
        }
        while (p < conditions.size()) {
            if (conditions.get(p++) == Condition.DAMAGED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return conditions.stream().map(c -> String.valueOf(c.symbol)).collect(Collectors.joining());
    }
}

record Interval(int start, int end) {
}

record GroupSizesOfDamagedSprings(List<Integer> sizes) {
    static GroupSizesOfDamagedSprings from(String line, int n) {
        var dup = IntStream.range(0, n).mapToObj(z -> line).collect(Collectors.joining(","));
        return new GroupSizesOfDamagedSprings(Arrays.stream(dup.split(",")).map(Integer::parseInt).toList());
    }

    @Override
    public String toString() {
        return sizes.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
}

record ConditionRecord(SpringConditions springConditions, GroupSizesOfDamagedSprings groupSizesOfDamagedSprings) {
    static ConditionRecord from(String line, int n) {
        var parts = line.split(" ");
        return new ConditionRecord(SpringConditions.from(parts[0], n), GroupSizesOfDamagedSprings.from(parts[1], n));
    }

    private static <T> List<T> concat(List<T> a, List<T> b) {
        var l = new ArrayList<>(a);
        l.addAll(b);
        return l;
    }

    BigInteger arrangements() {
        return arrangements(0, -1, Collections.emptyList());
    }

    BigInteger arrangements(int g, int start, List<Interval> intervals) {
        BigInteger r;
        if (g == groupSizesOfDamagedSprings.sizes().size()) {
            if (springConditions.matches(intervals)) {
                r = BigInteger.ONE;
            } else {
                r = BigInteger.ZERO;
            }
        } else {
            int groupSize = groupSizesOfDamagedSprings.sizes().get(g);
            int offset = IntStream.range(0, g).map(gg -> groupSizesOfDamagedSprings.sizes().get(gg)).sum() - 1;
            r = BigInteger.ZERO;
            for (int i = Integer.max(start, offset); i + groupSize < springConditions.conditions().size(); i++) {
                if (springConditions.matches(i + 1, i + groupSize + 1)) {
                    BigInteger a = arrangements(g + 1, i + groupSize + 1, concat(intervals, List.of(new Interval(i + 1, i + groupSize + 1))));
                    r = r.add(a);
                }
            }
        }
        return r;
    }

    @Override
    public String toString() {
        return springConditions + " " + groupSizesOfDamagedSprings;
    }
}

record Springs(List<ConditionRecord> conditionRecords) {
    static Springs parse(Stream<String> lines, int n) {
        return new Springs(lines.map(line -> ConditionRecord.from(line, n)).toList());
    }

    BigInteger arrangements() {
        return conditionRecords.stream().map(ConditionRecord::arrangements).reduce(BigInteger.ZERO, BigInteger::add);
    }
}

class Puzzle {
    void solve(@SuppressWarnings("SameParameterValue") int n) throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day12/day12_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var springs = Springs.parse(reader.lines(), n);
            System.out.println(springs.arrangements());
        }
    }
}

public class Day12 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve(1);
    }
}
