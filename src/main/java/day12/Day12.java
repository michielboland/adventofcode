package day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.Arrays;
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

    boolean matches() {
        int p = 0;
        while (p < conditions.size()) {
            if (conditions.get(p++) == Condition.DAMAGED) {
                return false;
            }
        }
        return true;
    }

    SpringConditions sub(int from, int to) {
        return new SpringConditions(conditions.subList(from, to));
    }
}

record GroupSizesOfDamagedSprings(List<Integer> sizes) {
    static GroupSizesOfDamagedSprings from(String line, int n) {
        var dup = IntStream.range(0, n).mapToObj(z -> line).collect(Collectors.joining(","));
        return new GroupSizesOfDamagedSprings(Arrays.stream(dup.split(",")).map(Integer::parseInt).toList());
    }

    GroupSizesOfDamagedSprings sub(int from, int to) {
        return new GroupSizesOfDamagedSprings(sizes.subList(from, to));
    }
}

record ConditionRecord(SpringConditions springConditions, GroupSizesOfDamagedSprings groupSizesOfDamagedSprings) {
    static ConditionRecord from(String line, int n) {
        var parts = line.split(" ");
        return new ConditionRecord(SpringConditions.from(parts[0], n), GroupSizesOfDamagedSprings.from(parts[1], n));
    }

    ConditionRecord sub(int fromSpring, int toSpring, int fromGroup, int toGroup) {
        return new ConditionRecord(springConditions.sub(fromSpring, toSpring), groupSizesOfDamagedSprings.sub(fromGroup, toGroup));
    }

    BigInteger arrangements() {
        int groups = groupSizesOfDamagedSprings.sizes().size();
        int springs = springConditions.conditions().size();
        BigInteger total;
        if (groups == 0) {
            total = springConditions.matches() ? BigInteger.ONE : BigInteger.ZERO;
        } else {
            total = BigInteger.ZERO;
            int half = groups >> 1;
            Integer halfSize = groupSizesOfDamagedSprings.sizes().get(half);
            int leftMargin = groups > 1 ? IntStream.range(0, half).map(i -> groupSizesOfDamagedSprings.sizes().get(i) + 1).sum() : 0;
            int rightMargin = IntStream.range(half + 1, groups).map(i -> groupSizesOfDamagedSprings.sizes().get(i) + 1).sum() - 1;
            for (int i = leftMargin; i + halfSize < springs - rightMargin; i++) {
                if (springConditions.matches(i, i + halfSize)) {
                    BigInteger leftTotal = i - 1 > 0 ? sub(0, i - 1, 0, half).arrangements() : BigInteger.ONE;
                    BigInteger rightTotal = i + halfSize + 1 < springs ? sub(i + halfSize + 1, springs, half + 1, groups).arrangements() : BigInteger.ONE;
                    total = total.add(leftTotal.multiply(rightTotal));
                }
            }
        }
        return total;
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
    void solve(int n) throws IOException {
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
        new Puzzle().solve(5);
    }
}
