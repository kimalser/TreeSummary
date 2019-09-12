package genericStructures;

public class Interval{
    public double start;
    public double end;

    public Interval(double start, double end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Interval) {
            Interval other = (Interval) o;
            return start == other.start && end == other.end;
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Double.hashCode(start);
        result = prime * result + Double.hashCode(end);
        return result;
    }

    public boolean intersects(Interval other) {
        return start <= other.end && other.start <= end;
    }

    public double getMidPoint() {
        return (start + end) / 2;
    }

    public boolean containsPoint(double p) {
        return start <= p && end >= p;
    }
}
