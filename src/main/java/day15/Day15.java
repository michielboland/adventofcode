package day15;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Day15 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Instruction(String line) {
    int hash() {
        int h = 0;
        for (int i = 0; i < line.length(); i++) {
            h += line.charAt(i);
            h += h << 4;
            h &= 0xff;
        }
        return h;
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
        }
    }
}
