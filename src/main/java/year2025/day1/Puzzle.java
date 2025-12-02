package year2025.day1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

public class Puzzle {
    private final Dial dial = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("day1_input"))))
            .lines()
            .map(Instruction::parse)
            .reduce(Dial.INITIAL, Dial::operate, Dial::combine);

    public static void main(String[] args) {
        var puzzle = new Puzzle();
        System.out.println(puzzle.part1());
        System.out.println(puzzle.part2());
    }

    int part1() {
        return dial.zeros();
    }

    int part2() {
        return dial.totalZeros();
    }

    record Instruction(int distance) {
        static Instruction parse(String line) {
            enum LR {L, R}
            return switch (LR.valueOf(line.substring(0, 1))) {
                case L -> new Instruction(-Integer.parseInt(line.substring(1)));
                case R -> new Instruction(Integer.parseInt(line.substring(1)));
            };
        }
    }

    record Dial(int number, int zeros, int zeroCrossings) {
        static final Dial INITIAL = new Dial(50, 0, 0);

        int totalZeros() {
            return zeros + zeroCrossings;
        }

        Dial update(int newNumber) {
            int newCrossings = 0;
            if (newNumber > number) {
                for (int c = number + 1; c < newNumber; c++) {
                    if (c % 100 == 0) {
                        // ugh
                        ++newCrossings;
                    }
                }
            } else if (number > newNumber) {
                for (int c = number - 1; c > newNumber; c--) {
                    if (c % 100 == 0) {
                        ++newCrossings;
                    }
                }
            } else {
                throw new IllegalArgumentException();
            }
            return new Dial(newNumber, zeros + (newNumber % 100 == 0 ? 1 : 0), zeroCrossings + newCrossings);
        }

        Dial operate(Instruction instruction) {
            return update(number + instruction.distance);
        }

        public Dial combine(Dial other) {
            throw new UnsupportedOperationException();
        }
    }
}
