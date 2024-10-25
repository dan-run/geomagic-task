package runtju.christian.geomagic.dto;

import java.util.List;

public record SimplifiedLineChain(
        List<Coordinate> coordinates,
        double length
) {
}
