package year2024.day25;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

enum Type {LOCK, KEY}

public class Day25 {
    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }
}

class Puzzle {
    final List<Piece> pieces;

    Puzzle() throws Exception {
        pieces = Arrays.stream(Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day25_input")).toURI())).split("\n\n")).map(Piece::from).toList();
    }

    void solve() {
        System.out.println(fittingPieces());
    }

    long fittingPieces() {
        return IntStream.range(0, pieces.size()).mapToLong(i -> IntStream.range(i + 1, pieces.size()).filter(j -> pieces.get(i).fits(pieces.get(j))).count()).sum();
    }
}

record Piece(Type type, int[] heights) {
    static Piece from(String blob) {
        var lines = blob.split("\n");
        var type = lines[0].startsWith("#") ? Type.LOCK : Type.KEY;
        var heights = new int[5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (lines[i + 1].charAt(j) == '#') {
                    heights[j]++;
                }
            }
        }
        return new Piece(type, heights);
    }

    boolean fits(Piece other) {
        if (type == other.type) {
            return false;
        }
        return IntStream.range(0, 5).allMatch(i -> heights[i] + other.heights[i] <= 5);
    }
}
