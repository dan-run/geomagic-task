package runtju.christian.geomagic.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class LineChain {
    private final List<Line2D> lines;

    private Coordinate start;
    private Coordinate end;

    @Setter
    private boolean canBeAppended = true;

    public LineChain (Line2D line, Coordinate start) {
        lines = new ArrayList<>();
        lines.add(line);
        this.start = start;

        if (line.A().equals(start)) {
            end = line.B();
        } else {
            end = line.A();
        }
    }

    /**
     * Try to append the chain with the given line.
     * This is only allowed if the chain is either empty or the line can be added to the start or end of the chain.
     *
     * @param line the line to append the chain with
     * @return true if successfully appended, false otherwise
     */
    public boolean appendIfPossible(Line2D line) {
        if (start == null && end == null) {
            start = line.A();
            end = line.B();
            lines.add(line);
            return true;
        }

        if (matchAndSetNewEnd(line.A(), line.B())) {
            lines.add(line);
            return true;
        }

        return false;
    }

    public boolean connectIfPossible(LineChain other) {
        if (start == null && end == null) {
            lines.addAll(other.lines);
            return true;
        }

        if (!other.canBeAppended) {
            return false;
        }

        if (matchAndSetNewEnd(other.start, other.end)) {
            lines.addAll(other.lines);
            // since all chain starts aren't appendable, we must have connected the ends
            // therefore the new end also isn't appendable
            canBeAppended = false;
            return true;
        }

        return false;
    }

    private boolean matchAndSetNewEnd(Coordinate a, Coordinate b) {
        if (!canBeAppended) return false;

        if (a.equals(end)) {
            end = b;
            return true;
        } else if (b.equals(end)) {
            end = a;
            return true;
        }
        return false;
    }

    // for faster debugging overview
    @Override
    public String toString() {
        return "LineChain [start=" + start + ", end=" + end + "]";
    }
}
