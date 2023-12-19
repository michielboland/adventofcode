package day19;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
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

@FunctionalInterface
interface RuleMatcher {
    boolean matches(Part part);
}

@FunctionalInterface
interface IntComparator {
    boolean compare(int a, int b);
}

public class Day19 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Rule(RuleMatcher matcher, String next) {
    private static final Pattern PATTERN = Pattern.compile("(.)([<>])(\\d+):([a-z]+|A|R)");
    private static final Pattern SIMPLE_PATTERN = Pattern.compile("([a-z]+|A|R)");
    private static final Map<String, IntComparator> LG = Map.of("<", (a, b) -> a < b, ">", (a, b) -> a > b);

    static Rule from(String s) {
        var m = PATTERN.matcher(s);
        if (m.matches()) {
            var category = Category.from(m.group(1));
            var comparator = Objects.requireNonNull(LG.get(m.group(2)));
            var value = Integer.parseInt(m.group(3));
            var next = m.group(4);
            return new Rule(p -> comparator.compare(p.ratings().get(category), value), next);
        }
        var sm = SIMPLE_PATTERN.matcher(s);
        if (!sm.matches()) {
            throw new IllegalArgumentException();
        }
        return new Rule(p -> true, sm.group(1));
    }
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

    String process(Part part) {
        return rules.stream().filter(r -> r.matcher().matches(part)).findFirst().map(Rule::next).orElseThrow();
    }
}

record Part(Map<Category, Integer> ratings) {
    private static final Pattern PATTERN = Pattern.compile("\\{(.*)}");
    private static final Pattern P2 = Pattern.compile("(.)=(\\d+)");

    static Part parse(String line) {
        var m = PATTERN.matcher(line);
        if (!m.matches()) {
            throw new IllegalArgumentException();
        }
        Map<Category, Integer> ratings = new HashMap<>();
        Arrays.stream(m.group(1).split(",")).forEach(s -> {
            var m2 = P2.matcher(s);
            if (!m2.matches()) {
                throw new IllegalArgumentException();
            }
            ratings.put(Category.from(m2.group(1)), Integer.parseInt(m2.group(2)));
        });
        return new Part(ratings);
    }

    int sum() {
        return ratings.values().stream().mapToInt(i -> i).sum();
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

    boolean isAccepted(Part part) {
        Workflow workflow = workflows.get("in");
        do {
            var next = workflow.process(part);
            if (next.equals("A")) {
                return true;
            } else if (next.equals("R")) {
                return false;
            } else {
                workflow = workflows.get(next);
            }
        } while (true);
    }

    int process() {
        return parts.stream().filter(this::isAccepted).mapToInt(Part::sum).sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day19/day19_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var workflowAndParts = WorkflowsAndParts.parse(reader.lines());
            System.out.println(workflowAndParts.process());
        }
    }
}
