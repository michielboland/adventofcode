package day12;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final Map<ConditionRecord, Long> CACHE = new HashMap<>();

    static ConditionRecord from(String line, int n) {
        var parts = line.split(" ");
        return new ConditionRecord(SpringConditions.from(parts[0], n), GroupSizesOfDamagedSprings.from(parts[1], n));
    }

    ConditionRecord sub(int fromSpring, int toSpring, int fromGroup, int toGroup) {
        return new ConditionRecord(springConditions.sub(fromSpring, toSpring), groupSizesOfDamagedSprings.sub(fromGroup, toGroup));
    }

    long arrangements() {
        Long cached = CACHE.get(this);
        if (cached != null) {
            return cached;
        }
        int groups = groupSizesOfDamagedSprings.sizes().size();
        int springs = springConditions.conditions().size();
        long total;
        if (groups == 0) {
            total = springConditions.matches() ? 1L : 0L;
        } else {
            total = 0L;
            int half = groups >> 1;
            Integer halfSize = groupSizesOfDamagedSprings.sizes().get(half);
            int leftMargin = groups > 1 ? IntStream.range(0, half).map(i -> groupSizesOfDamagedSprings.sizes().get(i) + 1).sum() : 0;
            int rightMargin = IntStream.range(half + 1, groups).map(i -> groupSizesOfDamagedSprings.sizes().get(i) + 1).sum() - 1;
            for (int i = leftMargin; i + halfSize < springs - rightMargin; i++) {
                if (springConditions.matches(i, i + halfSize)) {
                    long leftTotal = i - 1 > 0 ? sub(0, i - 1, 0, half).arrangements() : 1L;
                    long rightTotal = i + halfSize + 1 < springs ? sub(i + halfSize + 1, springs, half + 1, groups).arrangements() : 1L;
                    total = total + leftTotal * rightTotal;
                }
            }
        }
        CACHE.put(this, total);
        return total;
    }
}

record Springs(List<ConditionRecord> conditionRecords) {
    static Springs parse(Stream<String> lines, int n) {
        return new Springs(lines.map(line -> ConditionRecord.from(line, n)).toList());
    }

    long arrangements() {
        return conditionRecords.stream().mapToLong(ConditionRecord::arrangements).sum();
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
