package year2022.day6;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class Puzzle {
    private final String data;

    Puzzle() throws Exception {
        data = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day6_input")).toURI()));
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(startPosition(4));
    }

    int startPosition(@SuppressWarnings("SameParameterValue") final int n) {
        List<Character> chars = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            if (chars.size() == n) {
                chars.remove(0);
            }
            chars.add(data.charAt(i));
            if (new HashSet<>(chars).size() == n) {
                return i + 1;
            }
        }
        throw new IllegalStateException();
    }
}
