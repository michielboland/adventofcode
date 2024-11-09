package year2023.day19;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum Category {
    EXTREMELY_COOL_LOOKING('x'),
    MUSICAL('m'),
    AERODYNAMIC('a'),
    SHINY('s');

    final char symbol;

    Category(char symbol) {
        this.symbol = symbol;
    }

    static Category from(String s) {
        return Arrays.stream(values()).filter(c -> c.symbol == s.charAt(0)).findFirst().orElseThrow();
    }
}

public class Day19 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Range(long from, long to) {
    static final long MIN = 1L;
    static final long MAX = 4001L;
    static final Range ALL = new Range(MIN, MAX);

    static Range lessThan(long to) {
        return new Range(MIN, to);
    }

    static Range notLessThan(long to) {
        return new Range(to, MAX);
    }

    static Range greaterThan(long from) {
        return new Range(from + 1, MAX);
    }

    static Range notGreaterThan(long from) {
        return new Range(MIN, from + 1);
    }

    static Range from(long value) {
        return new Range(value, value + 1);
    }

    boolean intersects(Range other) {
        return Long.max(from, other.from) < Long.min(to, other.to);
    }

    Range intersection(Range other) {
        return new Range(Long.max(from, other.from), Long.min(to, other.to));
    }

    long size() {
        return to - from;
    }

    @Override
    public String toString() {
        return "[" + from + "," + to + ")";
    }
}

record Tesseract(Map<Category, Range> ranges) {
    static final Tesseract ALL = new Tesseract(Map.of(
            Category.EXTREMELY_COOL_LOOKING, Range.ALL,
            Category.MUSICAL, Range.ALL,
            Category.AERODYNAMIC, Range.ALL,
            Category.SHINY, Range.ALL));

    static Tesseract build(Category category, Function<Long, Range> f, long value) {
        return new Tesseract(Arrays.stream(Category.values()).map(c -> new CR(c, c == category ? f.apply(value) : Range.ALL)).collect(Collectors.toMap(CR::category, CR::range)));
    }

    static Tesseract lessThan(Category category, long to) {
        return build(category, Range::lessThan, to);
    }

    static Tesseract greaterThan(Category category, long from) {
        return build(category, Range::greaterThan, from);
    }

    static Tesseract notLessThan(Category category, long to) {
        return build(category, Range::notLessThan, to);
    }

    static Tesseract notGreaterThan(Category category, long from) {
        return build(category, Range::notGreaterThan, from);
    }

    static Tesseract from(Part part) {
        return new Tesseract(Arrays.stream(Category.values()).map(c -> new CR(c, Range.from(part.ratings().get(c)))).collect(Collectors.toMap(CR::category, CR::range)));
    }

    boolean intersects(Tesseract other) {
        return Arrays.stream(Category.values()).allMatch(c -> ranges.get(c).intersects(other.ranges.get(c)));
    }

    Tesseract intersection(Tesseract other) {
        return other == null ? null : new Tesseract(Arrays.stream(Category.values()).map(c -> new CR(c, ranges.get(c).intersection(other.ranges.get(c)))).collect(Collectors.toMap(CR::category, CR::range)));
    }

    long size() {
        return ranges.values().stream().mapToLong(Range::size).reduce(1, (a, b) -> a * b);
    }

    @Override
    public String toString() {
        return Arrays.stream(Category.values()).map(c -> c.symbol + "=" + ranges.get(c)).collect(Collectors.joining(","));
    }

    record CR(Category category, Range range) {
    }
}

record Rule(Tesseract tesseract, Tesseract complement, String next) {
    private static final Pattern PATTERN = Pattern.compile("(.)([<>])(\\d+):([a-z]+|A|R)");
    private static final Pattern SIMPLE_PATTERN = Pattern.compile("([a-z]+|A|R)");

    static Rule from(String s) {
        var m = PATTERN.matcher(s);
        if (m.matches()) {
            var category = Category.from(m.group(1));
            var value = Long.parseLong(m.group(3));
            var tesseract = m.group(2).equals("<") ? Tesseract.lessThan(category, value) : Tesseract.greaterThan(category, value);
            var complement = m.group(2).equals("<") ? Tesseract.notLessThan(category, value) : Tesseract.notGreaterThan(category, value);
            return new Rule(tesseract, complement, m.group(4));
        }
        var sm = SIMPLE_PATTERN.matcher(s);
        if (!sm.matches()) {
            throw new IllegalArgumentException();
        }
        return new Rule(Tesseract.ALL, null, sm.group(1));
    }
}

record TS(Tesseract tesseract, String next) {
}

record Workflow(String name, List<Rule> rules) {
    private static final Pattern PATTERN = Pattern.compile("([a-z]+)\\{(.*)}");

    static Workflow parse(String line) {
        var m = PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }
        return new Workflow(m.group(1), Arrays.stream(m.group(2).split(",")).map(Rule::from).toList());
    }

    List<TS> process(Tesseract tesseract) {
        List<TS> r = new ArrayList<>();
        for (var rule : rules) {
            if (tesseract.intersects(rule.tesseract())) {
                r.add(new TS(tesseract.intersection(rule.tesseract()), rule.next()));
            }
            tesseract = tesseract.intersection(rule.complement());
        }
        return r;
    }
}

record Part(Map<Category, Long> ratings) {
    private static final Pattern PATTERN = Pattern.compile("\\{(.*)}");
    private static final Pattern P2 = Pattern.compile("(.)=(\\d+)");

    static Part parse(String line) {
        var m = PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }
        Map<Category, Long> ratings = new HashMap<>();
        Arrays.stream(m.group(1).split(",")).forEach(s -> {
            var m2 = P2.matcher(s);
            if (!m2.matches()) {
                throw new IllegalArgumentException();
            }
            ratings.put(Category.from(m2.group(1)), Long.parseLong(m2.group(2)));
        });
        return new Part(ratings);
    }

    long sum() {
        return ratings.values().stream().mapToLong(l -> l).sum();
    }
}

record WorkflowsAndParts(Map<String, Workflow> workflows, List<Part> parts) {
    static WorkflowsAndParts parse(Stream<String> lines) {
        Map<String, Workflow> workflows = new HashMap<>();
        List<Part> parts = new ArrayList<>();
        class StateHolder {
            boolean parts;
        }
        var state = new StateHolder();
        lines.forEach(line -> {
            if (line.isEmpty()) {
                state.parts = true;
            } else if (state.parts) {
                parts.add(Part.parse(line));
            } else {
                var workflow = Workflow.parse(line);
                workflows.put(workflow.name(), workflow);
            }
        });
        return new WorkflowsAndParts(workflows, parts);
    }

    Set<Tesseract> process(Workflow workflow, Tesseract tesseract) {
        Set<Tesseract> tesseracts = new HashSet<>();
        for (var ts : workflow.process(tesseract)) {
            if (ts.next().equals("A")) {
                tesseracts.add(ts.tesseract());
            } else if (!ts.next().equals("R")) {
                tesseracts.addAll(process(workflows.get(ts.next()), ts.tesseract()));
            }
        }
        return tesseracts;
    }

    long process() {
        var accepted = process(workflows.get("in"), Tesseract.ALL);
        return parts.stream().filter(part -> accepted.stream().anyMatch(tesseract -> tesseract.intersects(Tesseract.from(part)))).mapToLong(Part::sum).sum();
    }

    long totalSize() {
        Set<Tesseract> all = process(workflows.get("in"), Tesseract.ALL);
        Set<Tesseract> tmp = new HashSet<>(all);
        Set<Tesseract> intersections = all.stream().flatMap(t1 -> {
            tmp.remove(t1);
            return tmp.stream().filter(t1::intersects).map(t1::intersection);
        }).collect(Collectors.toSet());
        // We don't have three different tesseracts with nonempty intersection, so we don't have
        // to do complicated stuff with the inclusion/exclusion principle.
        return all.stream().mapToLong(Tesseract::size).sum() - intersections.stream().mapToLong(Tesseract::size).sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/year2023/day19/day19_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var workflowAndParts = WorkflowsAndParts.parse(reader.lines());
            System.out.println(workflowAndParts.process());
            System.out.println(workflowAndParts.totalSize());
        }
    }
}
