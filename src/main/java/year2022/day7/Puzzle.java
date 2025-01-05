package year2022.day7;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Puzzle {
    private final FileSystem fileSystem = new FileSystem();
    private final Map<String, Long> sizes = new HashMap<>();

    Puzzle() throws Exception {
        var data = Files.readString(Paths.get(Objects.requireNonNull(getClass().getResource("day7_input")).toURI()));
        var blocks = data.split("(?s)\\$ ");
        String currentDirectory = "";
        for (var block : blocks) {
            if (block.isEmpty()) {
                continue;
            }
            var lines = block.split("\n");
            if (lines[0].startsWith("cd ")) {
                var target = lines[0].substring(3);
                currentDirectory = switch (target) {
                    case "/" -> "/";
                    case ".." -> currentDirectory.replaceAll("[^/]+/$", "");
                    default -> currentDirectory + target + "/";
                };
            } else { // ls
                var directory = Objects.requireNonNull(fileSystem.directories().get(currentDirectory));
                for (int i = 1; i < lines.length; i++) {
                    var parts = lines[i].split(" ");
                    if (parts[0].equals("dir")) {
                        fileSystem.createDirectory(directory, parts[1]);
                    } else {
                        directory.fileSizes().add(Long.parseLong(parts[0]));
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        new Puzzle().solve();
    }

    void solve() {
        System.out.println(part1());
        System.out.println(part2());
    }

    long part1() {
        fileSystem.directories().forEach((k, v) -> sizes.put(k, fileSystem.size(k)));
        return sizes.values().stream().filter(l -> l <= 100000).mapToLong(l -> l).sum();
    }

    long part2() {
        long total = sizes.get("/");
        return sizes.values().stream().filter(l -> total - l <= 40000000).mapToLong(l -> l).min().orElseThrow();
    }
}

record FileSystem(Map<String, Directory> directories) {
    void createDirectory(Directory parent, String name) {
        var directory = parent.create(name);
        directories.put(directory.path(), directory);
    }

    FileSystem() {
        this(new HashMap<>());
        directories.put("/", new Directory("/"));
    }

    long size(String directory) {
        return directories.entrySet().stream().filter(e -> e.getKey().startsWith(directory)).mapToLong(e -> e.getValue().size()).sum();
    }
}

record Directory(String path, List<Long> fileSizes) {
    Directory(String path) {
        this(path, new ArrayList<>());
    }

    Directory create(String name) {
        return new Directory(path + name + '/');
    }

    long size() {
        return fileSizes.stream().mapToLong(l -> l).sum();
    }
}
