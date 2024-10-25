package runtju.christian.geomagic.service;

import lombok.NonNull;
import org.springframework.stereotype.Service;
import runtju.christian.geomagic.dto.Line2D;
import runtju.christian.geomagic.exceptions.InputException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LineService {

    /**
     * Create a list of lines from string input coordinates.
     * Valid string format is four whitespace separated integers per line, read as follows:
     * x1 y1 x2 y2
     * integers beyond the fourth on a line are ignored
     *
     * @param input a string
     * @return a list of 2d lines based on the coordinates in the string.
     * @throws NumberFormatException     if any number cannot be parsed as int
     * @throws IndexOutOfBoundsException if any line does not contain at least 4 integers
     */
    public List<Line2D> buildLinesFromString(@NonNull String input) throws InputException {
        try {
            return input.lines()
                    .map(String::trim)
                    .map(textLine -> Arrays.stream(textLine.split(" ")).mapToInt(Integer::parseInt).toArray())
                    .map(coordinates -> Line2D.fromCoordinates(coordinates[0], coordinates[1], coordinates[2], coordinates[3]))
                    .collect(Collectors.toList());
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new InputException();
        }
    }

    public double calculateLength(@NonNull Line2D line) {
        double deltaX = line.A().x() - line.B().x();
        double deltaY = line.A().y() - line.B().y();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }
}
