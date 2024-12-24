package year2024.day24;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    final Map<String, Integer> initialValues = new HashMap<>();
    final Map<String, Wire> wires = new HashMap<>();

    Puzzle() throws Exception {
        var parts = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day24_input")).toURI())).split("\n\n");
        part0(parts[0]);
        part1(parts[1]);
    }

    private void part0(String lines) {
        for (String initLine : lines.split("\n")) {
            var initSplit = initLine.split(": ");
            String name = initSplit[0];
            initialValues.put(name, Integer.parseInt(initSplit[1]));
        }
    }

    private void part1(String lines) {
        for (String gateLine : lines.split("\n")) {
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

    long getValue() {
        long value = 0L;
        long bit = 1L;
        int i = 0;
        do {
            var wire = wires.get(("z" + "%02d").formatted(i));
            if (wire == null) {
                break;
            }
            if (wire.getValue() == 1) {
                value |= bit;
            }
            ++i;
            bit <<= 1;
        } while (true);
        return value;
    }

    void traceAll() {
        Set<String> swaps = new HashSet<>();
        int i = 0;
        do {
            ++i;
        } while (cell(i, swaps));
        System.out.println(swaps.stream().sorted().collect(Collectors.joining(",")));
    }

    boolean cell(int i, Set<String> swaps) {
        var a = wires.get(("x" + "%02d").formatted(i));
        if (a == null) {
            return false;
        }
        var xorGate = a.gates().stream().filter(g -> g.type() == GateType.XOR).findFirst().orElseThrow();
        var xorOutput = xorGate.output();
        var andGate = a.gates().stream().filter(g -> g.type() == GateType.AND).findFirst().orElseThrow();
        var andOutput = andGate.output();
        if (xorOutput.gates().stream().noneMatch(g -> g.type() == GateType.XOR) && andOutput.gates().stream().anyMatch(g -> g.type() == GateType.XOR)) {
            swaps.add(xorOutput.name());
            swaps.add(andOutput.name());
        }
        var outputName = xorOutput.gates().stream().filter(g -> g.type() == GateType.XOR).findFirst().map(g -> g.output().name()).orElse(null);
        if (outputName != null && !outputName.equals("z%02d".formatted(i))) {
            swaps.add(outputName);
            swaps.add("z%02d".formatted(i));
        }
        return true;
    }

    void solve() {
        initialValues.forEach((k, v) -> wires.get(k).setValue(v));
        System.out.println(getValue());
        traceAll();
    }
}

class State {
    Integer value;

    void setValue(int value) {
        if (this.value != null) {
            throw new IllegalStateException();
        }
        this.value = value;
    }
}

record Wire(String name, State state, Set<Gate> gates) {
    Wire(String name) {
        this(name, new State(), new HashSet<>());
    }

    Integer getValue() {
        return state.value;
    }

    void setValue(int value) {
        state.setValue(value);
        for (var gate : gates) {
            gate.trigger();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wire wire = (Wire) o;
        return Objects.equals(name, wire.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

record Gate(GateType type, Wire inputA, Wire inputB, Wire output) {
    void trigger() {
        if (inputA.getValue() != null && inputB.getValue() != null) {
            output.setValue(type.apply(inputA.getValue(), inputB.getValue()));
        }
    }
}
