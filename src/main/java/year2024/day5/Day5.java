package year2024.day5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day5 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    void solve() throws IOException {
        var instructions = new Instructions();
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day5_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            reader.lines().forEachOrdered(instructions::parse);
        }
        instructions.printSumOfMiddlePagesOfCorrectlyOrderedUpdates();
    }
}

class Instructions {

    final List<Order> orders = new ArrayList<>();
    final List<Manual> manuals = new ArrayList<>();
    boolean top = true;

    void parse(String line) {
        if (top) {
            if (line.isEmpty()) {
                top = false;
            } else {
                orders.add(Order.from(line));
            }
        } else {
            manuals.add(Manual.from(line));
        }
    }

    void printSumOfMiddlePagesOfCorrectlyOrderedUpdates() {
        System.out.println(manuals.stream().filter(m -> m.satisfies(orders)).mapToInt(Manual::middle).sum());
    }
}

record Order(int before, int after) {
    static Order from(String s) {
        var pair = Arrays.stream(s.split("\\|")).mapToInt(Integer::parseInt).toArray();
        return new Order(pair[0], pair[1]);
    }
}

record Page(int seq, int number) {
}

record Manual(Map<Integer, Integer> pages, int middle) {
    static Manual from(String s) {
        int[] numbers = Arrays.stream(s.split(",")).mapToInt(Integer::parseInt).toArray();
        return new Manual(IntStream.range(0, numbers.length)
                .mapToObj(i -> new Page(i, numbers[i]))
                .collect(Collectors.toMap(Page::number, Page::seq)), numbers[numbers.length >> 1]);
    }

    boolean satisfies(Order order) {
        return !(pages.containsKey(order.before()) && pages.containsKey(order.after()) && pages.get(order.before()) > pages.get(order.after()));
    }

    boolean satisfies(List<Order> orders) {
        return orders.stream().allMatch(this::satisfies);
    }
}
