package year2022.day1;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Puzzle {

    private final List<List<Integer>> calories;

    Puzzle() throws Exception {
        var input = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day1_input")).toURI()));
        calories = Arrays.stream(input.split("\n\n")).map(s -> Arrays.stream(s.split("\n")).map(Integer::parseInt).toList()).toList();
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(maxCalories());
    }

    int maxCalories() {
        return calories.stream().mapToInt(l -> l.stream().mapToInt(i -> i).sum()).max().orElseThrow();
    }
}
