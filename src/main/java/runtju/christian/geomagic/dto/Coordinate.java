package runtju.christian.geomagic.dto;

import lombok.NonNull;

public record Coordinate(
        int x,
        int y
) implements Comparable<Coordinate> {
    /**
     * Compare this coordinate with another based on manhattan distance to coordinate origin
     *
     * @param other the object to be compared to
     * @return int > 0 if this is further from origin than {@code other}, int < 0 if this is closer, 0 if the distance is the same
     */
    @Override
    public int compareTo(@NonNull Coordinate other) {
        return (x + y) - (other.x+other.y);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Coordinate)) return false;
        return compareTo((Coordinate) other) == 0;
    }
}
