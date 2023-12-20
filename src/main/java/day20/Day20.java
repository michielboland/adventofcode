package day20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

enum Signal {LOW, HIGH}

interface Module {
    String name();

    void receive(Pulse pulse, Module sender);
}

public class Day20 {
    public static void main(String[] args) throws IOException {
        new Puzzle().solve();
    }
}

record Pulse(Signal signal, long id, Module sender, Module receiver) {
    @Override
    public String toString() {
        return sender.name() + " -" + signal.name().toLowerCase() + "-> " + receiver.name();
    }
}

record Coordinator(AtomicLong lows, AtomicLong highs, Deque<Pulse> sendQueue, Deque<Pulse> processQueue) {
    private static final AtomicBoolean WHAT = new AtomicBoolean();

    Coordinator {
        if (WHAT.getAndSet(true)) {
            throw new IllegalStateException();
        }
    }

    Coordinator() {
        this(new AtomicLong(), new AtomicLong(), new ArrayDeque<>(), new ArrayDeque<>());
    }

    void queue(Pulse pulse) {
        //System.out.println(pulse);
        sendQueue.addFirst(pulse);
    }

    void sendLowPulse(Module sender, Module receiver) {
        queue(new Pulse(Signal.LOW, lows.incrementAndGet(), sender, receiver));
    }

    void sendHighPulse(Module sender, Module receiver) {
        queue(new Pulse(Signal.HIGH, highs.incrementAndGet(), sender, receiver));
    }

    void runQueue() {
        while (!sendQueue.isEmpty()) {
            processQueue.addAll(sendQueue);
            sendQueue.clear();
            while (!processQueue.isEmpty()) {
                Pulse pulse = processQueue.removeLast();
                pulse.receiver().receive(pulse, pulse.sender());
            }
        }
    }
}

abstract class AbstractModule implements Module {
    final Coordinator coordinator;
    final List<Module> receivers = new ArrayList<>();
    private final String name;

    AbstractModule(String name, Coordinator coordinator) {
        this.name = name;
        this.coordinator = coordinator;
    }

    @Override
    public String name() {
        return name;
    }
}

class Dummy extends AbstractModule {
    Dummy(String name, Coordinator coordinator) {
        super(name, coordinator);
    }

    @Override
    public void receive(Pulse pulse, Module sender) {
        // do nothing
    }
}

class Button extends AbstractModule {
    Button(String name, Coordinator coordinator) {
        super(name, coordinator);
    }

    void push() {
        coordinator.sendLowPulse(this, receivers.get(0));
    }

    @Override
    public void receive(Pulse pulse, Module sender) {
        throw new UnsupportedOperationException();
    }
}

class Broadcaster extends AbstractModule {
    Broadcaster(String name, Coordinator coordinator) {
        super(name, coordinator);
    }

    @Override
    public void receive(Pulse pulse, Module sender) {
        if (pulse.signal() == Signal.HIGH) {
            throw new IllegalStateException();
        }
        receivers.forEach(m -> coordinator.sendLowPulse(this, m));
    }
}

class FlipFlop extends AbstractModule {
    private final AtomicBoolean state = new AtomicBoolean();

    FlipFlop(String name, Coordinator coordinator) {
        super(name, coordinator);
    }

    @Override
    public void receive(Pulse pulse, Module sender) {
        if (pulse.signal() == Signal.HIGH) {
            return;
        }
        var newState = !state.get();
        state.set(newState);
        if (newState) {
            receivers.forEach(m -> coordinator.sendHighPulse(this, m));
        } else {
            receivers.forEach(m -> coordinator.sendLowPulse(this, m));
        }
    }
}

class Conjunction extends AbstractModule {
    private final Map<Module, AtomicBoolean> state = new HashMap<>();

    Conjunction(String name, Coordinator coordinator) {
        super(name, coordinator);
    }

    void addSender(Module sender) {
        state.put(sender, new AtomicBoolean());
    }

    @Override
    public void receive(Pulse pulse, Module sender) {
        state.get(sender).set(pulse.signal() == Signal.HIGH);
        if (state.values().stream().allMatch(AtomicBoolean::get)) {
            receivers.forEach(m -> coordinator.sendLowPulse(this, m));
        } else {
            receivers.forEach(m -> coordinator.sendHighPulse(this, m));
        }
    }
}

record Modules(Map<String, ? extends Module> modules, Coordinator coordinator, Button button) {
    static final String BROADCASTER = "broadcaster";
    static final String BUTTON = "button";
    private static final Pattern PATTERN = Pattern.compile("([&%]?)([a-z]+) -> (.*)");

    static Modules parse(Stream<String> lines) {
        List<String> broadcastReceivers = new ArrayList<>();
        Map<String, List<String>> flipFlopReceivers = new HashMap<>();
        Map<String, List<String>> conjunctionReceivers = new HashMap<>();
        Set<String> dummyNames = new HashSet<>();
        lines.forEach(line -> {
            var m = PATTERN.matcher(line);
            if (!m.matches()) {
                throw new IllegalArgumentException();
            }
            var type = m.group(1);
            var name = m.group(2);
            var receivers = Arrays.stream(m.group(3).split(", ")).toList();
            dummyNames.addAll(receivers);
            switch (type) {
                case "" -> broadcastReceivers.addAll(receivers);
                case "%" -> flipFlopReceivers.put(name, receivers);
                case "&" -> conjunctionReceivers.put(name, receivers);
                default -> throw new IllegalArgumentException();
            }
        });
        dummyNames.removeAll(flipFlopReceivers.keySet());
        dummyNames.removeAll(conjunctionReceivers.keySet());
        Map<String, AbstractModule> modules = new HashMap<>();
        var coordinator = new Coordinator();
        dummyNames.forEach(n -> modules.put(n, new Dummy(n, coordinator)));
        conjunctionReceivers.keySet().forEach(n -> modules.put(n, new Conjunction(n, coordinator)));
        flipFlopReceivers.keySet().forEach(n -> modules.put(n, new FlipFlop(n, coordinator)));
        var broadcaster = new Broadcaster(BROADCASTER, coordinator);
        broadcaster.receivers.addAll(broadcastReceivers.stream().map(modules::get).toList());
        flipFlopReceivers.forEach((key, value) -> modules.get(key).receivers.addAll(value.stream().map(modules::get).toList()));
        conjunctionReceivers.forEach((key, value) -> modules.get(key).receivers.addAll(value.stream().map(modules::get).toList()));
        modules.put(BROADCASTER, broadcaster);
        var button = new Button(BUTTON, coordinator);
        button.receivers.add(broadcaster);
        modules.put(BUTTON, button);
        modules.values().forEach(module -> module.receivers.forEach(receiver -> {
            if (receiver instanceof Conjunction conjunction) {
                conjunction.addSender(module);
            }
        }));
        return new Modules(modules, coordinator, button);
    }

    void pushButton() {
        IntStream.range(0, 1000).forEach(i -> {
            button.push();
            coordinator.runQueue();
        });
    }
}

class Puzzle {
    void solve() throws IOException {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/day20/day20_input"))) {
            var reader = new BufferedReader(new InputStreamReader(input));
            var modules = Modules.parse(reader.lines());
            modules.pushButton();
            System.out.println(modules.coordinator().highs().get() * modules.coordinator().lows().get());
        }
    }
}
