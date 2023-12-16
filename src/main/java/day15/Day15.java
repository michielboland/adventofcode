package day15;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

enum Action {REMOVE, INSERT}

public class Day15 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record LavaProductionFacility(Map<Integer, Box> boxes) {
    LavaProductionFacility() {
        this(new HashMap<>());
    }

    void process(Instruction instruction) {
        var box = boxes.computeIfAbsent(instruction.boxNumber(), z -> new Box(instruction.boxNumber()));
        switch (instruction.action()) {
            case INSERT -> box.insert(instruction.label(), instruction.focalLength());
            case REMOVE -> box.remove(instruction.label());
        }
    }

    void process(Instructions instructions) {
        instructions.instructions().forEach(this::process);
    }

    int focusingPower() {
        return boxes.values().stream().mapToInt(Box::focusingPower).sum();
    }
}

record Box(int number, Map<String, Integer> lenses) {
    Box(int number) {
        this(number, new LinkedHashMap<>());
    }

    void insert(String label, int focalLength) {
        lenses.put(label, focalLength);
    }

    void remove(String label) {
        lenses.remove(label);
    }

    int focusingPower() {
        AtomicInteger i = new AtomicInteger();
        return lenses.values().stream().mapToInt(l -> l * i.incrementAndGet()).sum() * (number + 1);
    }
}

record Instruction(String line) {
    private static final Pattern PATTERN = Pattern.compile("(.*)(=\\d|-)");

    static int hash(String line) {
        int h = 0;
        for (int i = 0; i < line.length(); i++) {
            h += line.charAt(i);
            h += h << 4;
            h &= 0xff;
        }
        return h;
    }

    int hash() {
        return hash(line);
    }

    Matcher matcher() {
        var matcher = PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IllegalStateException();
        }
        return matcher;
    }

    String label() {
        return matcher().group(1);
    }

    int boxNumber() {
        return hash(label());
    }

    Action action() {
        return matcher().group(2).equals("-") ? Action.REMOVE : Action.INSERT;
    }

    int focalLength() {
        return Integer.parseInt(matcher().group(2).replace("=", ""));
    }
}

record Instructions(List<Instruction> instructions) {
    static Instructions parse(Stream<String> lines) {
        return new Instructions(lines.map(Instruction::new).toList());
    }

    int hash() {
        return instructions.stream().mapToInt(Instruction::hash).sum();
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day15/day15_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var instructions = Instructions.parse(reader.lines());
            System.out.println(instructions.hash());
            var facility = new LavaProductionFacility();
            facility.process(instructions);
            System.out.println(facility.focusingPower());
        }
    }
}
