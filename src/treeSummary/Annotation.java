package treeSummary;

import genericStructures.Interval;

public class Annotation {
    Interval proposedInterval;
    int cost;
    int dim;

    Annotation(Interval proposedInterval, int cost, int dim) {
        this.proposedInterval = proposedInterval;
        this.cost = cost;
        this.dim = dim;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Annotation) {
            Annotation other = (Annotation) o;
            return proposedInterval.equals(other.proposedInterval) && cost == other.cost && dim == other.dim;
        }
        return false;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + proposedInterval.hashCode();
        result = prime * result + dim;
        result = prime * result + cost;
        return result;
    }

}
