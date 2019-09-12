package genericStructures;

public class IntervalPoint implements Comparable<IntervalPoint> {
    private boolean isStartPoint;
    private Interval interval;

    public IntervalPoint(boolean isStartPoint, Interval interval) {
        this.isStartPoint = isStartPoint;
        this.interval = interval;
    }

    public Interval getInterval() {
        return interval;
    }

    public boolean isStartPoint() {
        return isStartPoint;
    }

    public double getPointValue() {
        if (isStartPoint)
            return interval.start;
        return interval.end;
    }

    public double getOtherPointValue() {
        if (isStartPoint)
            return interval.end;
        return interval.start;
    }

    public int compareTo(IntervalPoint p) {
        if (isStartPoint == p.isStartPoint && getPointValue() == p.getPointValue())
            return Double.compare(getOtherPointValue(), p.getOtherPointValue());

        if (getPointValue() == p.getPointValue()) {
            return Boolean.compare(p.isStartPoint, isStartPoint);
        }
        return Double.compare(getPointValue(), p.getPointValue());
    }
}