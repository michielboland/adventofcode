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
    Disk disk() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("day9_input"))) {
            return Disk.from(new BufferedReader(new InputStreamReader(input)).lines());
        }
    }

    void solve() throws IOException {
        System.out.println(disk().compress().checksum());
        System.out.println(disk().compressBetter().checksum());
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

    Disk compress() {
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
        return this;
    }

    int freePos(int len, int max) {
        int i = 0;
        int candidate = -1;
        int n = 0;
        while (i < max) {
            if (blocks[i] == -1) {
                if (candidate == -1) {
                    candidate = i;
                    n = 1;
                } else {
                    ++n;
                }
                if (n >= len) {
                    return candidate;
                }
            } else {
                candidate = -1;
            }
            ++i;
        }
        return -1;
    }

    Disk compressBetter() {
        int end = blocks.length;
        do {
            while (end > 0 && blocks[end - 1] == -1) {
                end--;
            }
            if (end == 0) {
                break;
            }
            int fileId = blocks[end - 1];
            int start = end - 1;
            while (start > 0 && blocks[start - 1] == fileId) {
                start--;
            }
            int len = end - start;
            int freePos = freePos(len, start);
            if (freePos != -1) {
                swap(freePos, start, len);
            }
            end = start;
        } while (true);
        return this;
    }

    long checksum() {
        return IntStream.range(0, blocks.length).filter(i -> blocks[i] != -1).mapToLong(i -> (long) i * blocks[i]).sum();
    }

    void swap(int a, int b) {
        var c = blocks[a];
        blocks[a] = blocks[b];
        blocks[b] = c;
    }

    void swap(int a, int b, int len) {
        for (int i = 0; i < len; i++) {
            swap(a + i, b + i);
        }
    }
}
