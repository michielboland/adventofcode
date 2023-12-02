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

public class Puzzle1 {
    public static void main(String[] args) throws IOException {
        new Puzzle1().solve();
    }

    private static int getTotal(BufferedReader reader) throws IOException {
        int total = 0;
        for (var line = reader.readLine(); line != null; line = reader.readLine()) {
            var game = Game.from(line);
            if (game.isPossible()) {
                total += game.id();
            }
        }
        return total;
    }

    private void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day2/day2_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var total = getTotal(reader);
            System.out.println(total);
        }
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

    public boolean isPossible() {
        return Arrays.stream(samples).allMatch(Sample::isPossible);
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

    public boolean isPossible() {
        return red <= 12 && green <= 13 && blue <= 14;
    }
}
