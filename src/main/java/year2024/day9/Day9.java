package year2024.day9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day9 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

class Puzzle {
    final Disk disk;

    Puzzle() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day9_input"))) {
            disk = Disk.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() {
        disk.compress();
        System.out.println(disk.checksum());
    }
}

class Disk {
    final int[] blocks;

    Disk(String string) {
        int n = 0;
        boolean free = false;
        int[] diskMap = string.chars().filter(Character::isDigit).map(i -> i - '0').toArray();
        int nBlocks = Arrays.stream(diskMap).sum();
        int pos = 0;
        blocks = new int[nBlocks];
        for (int mapEntry : diskMap) {
            for (int i = 0; i < mapEntry; i++) {
                blocks[pos + i] = free ? -1 : n;
            }
            pos += mapEntry;
            free = !free;
            if (!free) {
                ++n;
            }
        }
    }

    static Disk from(Stream<String> lines) {
        return new Disk(lines.collect(Collectors.joining()));
    }

    void compress() {
        int freePtr = 0;
        int nonFreePtr = blocks.length;
        do {
            while (freePtr < blocks.length && blocks[freePtr] != -1) {
                freePtr++;
            }
            while (nonFreePtr > 0 && blocks[nonFreePtr - 1] == -1) {
                nonFreePtr--;
            }
            if (freePtr >= nonFreePtr) {
                break;
            }
            swap(freePtr, nonFreePtr - 1);
            freePtr++;
            nonFreePtr--;
        } while (true);
    }

    long checksum() {
        return IntStream.range(0, blocks.length).filter(i -> blocks[i] != -1).mapToLong(i -> (long) i * blocks[i]).sum();
    }

    void swap(int a, int b) {
        var c = blocks[a];
        blocks[a] = blocks[b];
        blocks[b] = c;
    }
}
