package runtju.christian.geomagic.dto;


import lombok.NonNull;

import java.util.Objects;

public record Line2D(
        Coordinate A, Coordinate B
) {
    @NonNull
    public static Line2D fromCoordinates(Coordinate a, Coordinate b) {
        if (a.compareTo(b) > 0) {
            return new Line2D(a, b);
        }

        return new Line2D(b, a);
    }

    @NonNull
    public static Line2D fromCoordinates(int x1, int y1, int x2, int y2) {
        var a = new Coordinate(x1, y1);
        var b = new Coordinate(x2, y2);

        return fromCoordinates(a, b);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Line2D o)) return false;
        return (Objects.equals(A, o.A) && Objects.equals(B, o.B))
                || (Objects.equals(B, o.A) && Objects.equals(A, o.B));
    }
}
