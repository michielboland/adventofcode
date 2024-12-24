package year2024.day24;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

enum GateType {
    AND {
        @Override
        Integer apply(Integer a, Integer b) {
            return a & b;
        }
    }, OR {
        @Override
        Integer apply(Integer a, Integer b) {
            return a | b;
        }
    }, XOR {
        @Override
        Integer apply(Integer a, Integer b) {
            return a ^ b;
        }
    };

    abstract Integer apply(Integer a, Integer b);
}

public class Day24 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Map<String, Integer> initialValues = new TreeMap<>();
    final Map<String, Wire> wires = new TreeMap<>();

    Puzzle() throws Exception {
        var parts = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day24_input")).toURI())).split("\n\n");
        for (String initLine : parts[0].split("\n")) {
            var initSplit = initLine.split(": ");
            String name = initSplit[0];
            wires.computeIfAbsent(name, k -> new Wire(name));
            initialValues.put(name, Integer.parseInt(initSplit[1]));
        }
        for (String gateLine : parts[1].split("\n")) {
            var split = gateLine.split(" ");
            var nameA = split[0];
            var type = GateType.valueOf(split[1]);
            var nameB = split[2];
            var name = split[4];
            var inputA = wires.computeIfAbsent(nameA, k -> new Wire(nameA));
            var inputB = wires.computeIfAbsent(nameB, k -> new Wire(nameB));
            var output = wires.computeIfAbsent(name, k -> new Wire(name));
            var gate = new Gate(type, inputA, inputB, output);
            inputA.gates().add(gate);
            inputB.gates().add(gate);
        }
    }

    void solve() {
        initialValues.forEach((k, v) -> wires.get(k).value(v));
        var outputNames = wires.keySet().stream().filter(s -> s.startsWith("z")).sorted(Comparator.reverseOrder()).toList();
        String binary = outputNames.stream().map(s -> String.valueOf(wires.get(s).value())).collect(Collectors.joining());
        System.out.println(binary);
        System.out.println(Long.parseLong(binary, 2));
    }
}

class Holder {
    Integer value;

    boolean setValue(Integer value) {
        if (this.value == null) {
            this.value = Objects.requireNonNull(value);
            return true;
        }
        if (!this.value.equals(value)) {
            throw new IllegalStateException();
        }
        return false;
    }
}

record Wire(String name, Holder state, Set<Gate> gates) implements Comparable<Wire> {
    Wire(String name) {
        this(name, new Holder(), new HashSet<>());
    }

    Integer value() {
        return state.value;
    }

    void value(Integer value) {
        if (state.setValue(value)) {
            for (var gate : gates) {
                gate.trigger();
            }
        }
    }

    @Override
    public String toString() {
        return name + "(" + state.value + ")";
    }

    @Override
    public int compareTo(Wire o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wire wire = (Wire) o;
        return Objects.equals(name, wire.name) && Objects.equals(state, wire.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, state);
    }
}

record Gate(GateType type, Wire inputA, Wire inputB, Wire output) {
    void trigger() {
        if (inputA.value() != null && inputB.value() != null) {
            output.value(type.apply(inputA.value(), inputB.value()));
        }
    }
}
