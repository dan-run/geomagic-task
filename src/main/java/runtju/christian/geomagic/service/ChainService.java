package runtju.christian.geomagic.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import runtju.christian.geomagic.dto.Coordinate;
import runtju.christian.geomagic.dto.Line2D;
import runtju.christian.geomagic.dto.LineChain;
import runtju.christian.geomagic.dto.SimplifiedLineChain;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired)) //lombok magic to avoid constructor boilerplate
public class ChainService {
    private final LineService lineService;

    public List<LineChain> buildChains(@NonNull List<Line2D> lines) {
        if (lines.isEmpty()) {
            return Collections.emptyList();
        }

        // preprocess lines for easier chain construction
        var lineMap = new LineMap();
        lines.forEach(line -> {
            lineMap.put(line.A(), line);
            lineMap.put(line.B(), line);
        });

        List<LineChain> chains = new ArrayList<>();

        // determine fixed starts that can't connect chains
        var unconnectableCoords = lineMap.entrySet().stream()
                .filter(entry -> entry.getValue().size() > 2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());


        // build starting chains
        unconnectableCoords.forEach(coordinate -> {
            Optional<Line2D> nextLine;
            while ((nextLine = lineMap.pop(coordinate)).isPresent()) {
                var chain = new LineChain(
                        nextLine.get(),
                        coordinate
                );
                if (unconnectableCoords.contains(chain.getEnd())) {
                    chain.setCanBeAppended(false);
                }
                chains.add(chain);
            }
        });

        // naively and greedily use all leftover lines
        for (var chain : chains) {
            boolean successFullyAppended;
            do {
                successFullyAppended = false;
                var nextLine = lineMap.pop(chain.getEnd());
                if (nextLine.isEmpty()) {
                    nextLine = lineMap.pop(chain.getStart());
                }
                if (nextLine.isPresent()) {
                    successFullyAppended = chain.appendIfPossible(nextLine.get());
                }
            } while (successFullyAppended);
        }

        // connect existing chains
        var toBeRemoved = new ArrayList<LineChain>();
        for (var chain : chains) {
            if (!chain.isCanBeAppended()) {
                continue;
            }
            for (var chain1: chains) {
                if (chain.equals(chain1)
                        || toBeRemoved.contains(chain1)
                        || (!chain1.isCanBeAppended())
                ) continue;

                if (chain.connectIfPossible(chain1)) {
                    toBeRemoved.add(chain1);
                    if (!chain.isCanBeAppended()) {
                        break;
                    }
                }
            }
        }
        chains.removeAll(toBeRemoved);

        return chains;
    }

    public double calculateLength(@NonNull LineChain chain) {
        return chain.getLines().stream().reduce(
                0d,
                (result, line) -> lineService.calculateLength(line) + result,
                Double::sum
        );
    }

    public SimplifiedLineChain simplifyChain(@NonNull LineChain chain) {
        double length = calculateLength(chain);
        var coords = new LinkedList<Coordinate>();
        coords.add(chain.getStart());

        for (var line : chain.getLines()) {
            if (coords.getLast().equals(line.A())) {
                coords.add(line.B());
            } else {
                coords.add(line.A());
            }
        }

        return new SimplifiedLineChain(coords, length);
    }

    // helper class to do some boilerplate through the data structure
    private static final class LineMap extends HashMap<Coordinate, HashSet<Line2D>> {
        public void put(Coordinate key, Line2D value) {
            if (!super.containsKey(key)) {
                var set = new HashSet<Line2D>();
                set.add(value);
                super.put(key, set);
            }

            super.get(key).add(value);
        }

        public void remove(Line2D value) {
            Optional.ofNullable(super.get(value.A()))
                    .ifPresent(set -> {
                        set.remove(value);
                        if (set.isEmpty()) {
                            super.remove(value.A());
                        }
                    });
            Optional.ofNullable(super.get(value.B()))
                    .ifPresent(set -> {
                        set.remove(value);
                        if (set.isEmpty()) {
                            super.remove(value.B());
                        }
                    });
        }

        public Optional<Line2D> pop(Coordinate key) {
            if (!super.containsKey(key) || super.get(key).isEmpty()) {
                return Optional.empty();
            }

            var line = super.get(key).stream().findFirst().get();
            remove(line);
            return Optional.of(line);
        }
    }
}
