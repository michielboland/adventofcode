package year2025.day1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

public class Puzzle {
    public static void main(String[] args) {
        var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Puzzle.class.getResourceAsStream("day1_input"))));
        Dial d = reader.lines().map(Instruction::parse).reduce(Dial.INITIAL, Dial::operate, Dial::combine);
        System.out.println(d.zeros);
    }

    static int posMod(int i, @SuppressWarnings("SameParameterValue") int j) {
        var r = i % j;
        return r >= 0 ? r : r + j;
    }

    enum LR {L, R}

    record Instruction(LR lr, int distance) {
        static Instruction parse(String line) {
            return new Instruction(LR.valueOf(line.substring(0, 1)), Integer.parseInt(line.substring(1)));
        }
    }

    record Dial(int number, int zeros) {
        static final Dial INITIAL = new Dial(50, 0);

        Dial click(int newNumber) {
            return new Dial(newNumber, zeros + (newNumber == 0 ? 1 : 0));
        }

        Dial right(int times) {
            return click(posMod(number + times, 100));
        }

        Dial left(int times) {
            return click(posMod(number - times, 100));
        }

        Dial operate(Instruction instruction) {
            return switch (instruction.lr) {
                case L -> left(instruction.distance);
                case R -> right(instruction.distance);
            };
        }

        public Dial combine(Dial other) {
            throw new UnsupportedOperationException();
        }
    }
}
