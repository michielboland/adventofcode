package day2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

enum Ball {
    RED("red", r -> new Sample(r, 0, 0)),
    GREEN("green", g -> new Sample(0, g, 0)),
    BLUE("blue", b -> new Sample(0, 0, b));
    final Pattern pattern;
    final Function<Integer, Sample> sampleFunction;

    Ball(String color, Function<Integer, Sample> sampleFunction) {
        pattern = Pattern.compile("^(\\d+) " + color + "$");
        this.sampleFunction = sampleFunction;
    }

    Optional<Sample> sample(String text) {
        var m = pattern.matcher(text);
        if (m.matches()) {
            return Optional.of(sampleFunction.apply(Integer.parseInt(m.group(1))));
        }
        return Optional.empty();
    }
}

public class Day2 {
    public static void main(String[] args) throws IOException {
        new Puzzle1().solve();
        new Puzzle2().solve();
    }
}

abstract class Puzzle {
    abstract void solve(Game[] games);

    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day2/day2_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var games = reader.lines().map(Game::from).toArray(Game[]::new);
            solve(games);
        }
    }
}

class Puzzle1 extends Puzzle {
    @Override
    void solve(Game[] games) {
        System.out.println(Arrays.stream(games).filter(Game::isPossible).mapToInt(Game::id).sum());
    }
}

class Puzzle2 extends Puzzle {
    @Override
    void solve(Game[] games) {
        System.out.println(Arrays.stream(games).mapToInt(Game::power).sum());
    }
}

record Game(int id, Sample[] samples) {

    static final Pattern LINE_PATTERN = Pattern.compile("^Game (\\d+): (.*)$");
    static final Pattern SAMPLE_SEPARATOR = Pattern.compile("; ");

    static Game from(String line) {
        var m1 = LINE_PATTERN.matcher(line);
        if (!m1.matches()) {
            throw new IllegalArgumentException("malformed input");
        }
        int id = Integer.parseInt(m1.group(1));
        String[] sampleTexts = SAMPLE_SEPARATOR.split(m1.group(2));
        return new Game(id, Arrays.stream(sampleTexts).map(Sample::fromText).toArray(Sample[]::new));
    }

    boolean isPossible() {
        return Arrays.stream(samples).allMatch(Sample::isPossible);
    }

    int power() {
        return Arrays.stream(samples).reduce(Sample.ZERO, Sample::max).power();
    }
}

record Sample(int red, int green, int blue) {
    static final Pattern BALL_SEPARATOR = Pattern.compile(", ");
    static final Sample ZERO = new Sample(0, 0, 0);

    static Sample fromText(String text) {
        return Arrays.stream(BALL_SEPARATOR.split(text)).map(Sample::fromBall).reduce(ZERO, Sample::add);
    }

    static Sample fromBall(String ballText) {
        return Arrays.stream(Ball.values())
                .map(b -> b.sample(ballText))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst().orElseThrow();
    }

    Sample add(Sample other) {
        return new Sample(red + other.red, green + other.green, blue + other.blue);
    }

    Sample max(Sample other) {
        return new Sample(Integer.max(red, other.red), Integer.max(green, other.green), Integer.max(blue, other.blue));
    }

    boolean isPossible() {
        return red <= 12 && green <= 13 && blue <= 14;
    }

    int power() {
        return red * green * blue;
    }
}
