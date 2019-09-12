package overlappingRectangles;

import genericStructures.LatticeNode;
import genericStructures.Interval;
import genericStructures.IntervalPoint;
import utilities.Utility;

import java.util.*;

import static overlappingRectangles.ORLattice.alpha;

public class ORLatticeNode extends LatticeNode {

    private List<Interval> intervals;
    private double tempValue = value; // C_{l(u)} - C_{l(p(u))}
    private double reconstructedValue = 0;

    public ORLatticeNode(String name) {
        super(name);
        intervals = new ArrayList<>();
    }

    void computeDS() {
        if (isLeaf) {
            Interval interval = tempValue > 0 ? new Interval(tempValue - tempValue * alpha, tempValue + tempValue * alpha) :
                    new Interval(tempValue + tempValue * alpha, tempValue - tempValue * alpha);
            intervals.add(interval);
        } else {
            if (!children.get(0).isEmpty()) {
                for (LatticeNode child : children.get(0))
                    ((ORLatticeNode) child).computeDS();

                Map<IntervalPoint, IntervalPoint> pointMap = new HashMap<>();
                List<IntervalPoint> points = new ArrayList<>();

                for (LatticeNode child : children.get(0)) {
                    for (Interval interval : ((ORLatticeNode) child).intervals) {
                        Utility.addPointIntervals(pointMap, points, interval);
                    }
                }

                if (!points.isEmpty()) {
                    List<double[]> mostOverlappedIntervals = Utility.getMostOverlappedIntervals(points, pointMap);
                    for (double[] overlap : mostOverlappedIntervals) {
                        intervals.add(new Interval(overlap[0], overlap[1]));
                    }
                }
            }
        }
    }


    Map<String, Double> weightTree() {
        Map<String, Double> rectangles = new HashMap<>();

        Interval proposedInterval = intervals.get(0); // get any interval
        if (proposedInterval.getMidPoint() != 0)
            rectangles.put(name, proposedInterval.getMidPoint());

        weightTreeHelper(proposedInterval.getMidPoint(), children.get(0), rectangles);
        return rectangles;
    }

    private void weightTreeHelper(double parentRootToLeaf, List<LatticeNode> children, Map<String, Double> rectangles) {
        for (LatticeNode child : children) {
            boolean matchedInterval = false;
            for (Interval proposedInterval : ((ORLatticeNode) child).intervals) {
                if (proposedInterval.containsPoint(parentRootToLeaf)) {
                    weightTreeHelper(parentRootToLeaf, ((ORLatticeNode) child).children.get(0), rectangles);
                    matchedInterval = true;
                    break;
                }
            }
            if (!matchedInterval) {
                Interval proposedInterval = ((ORLatticeNode) child).intervals.get(0);
                if (proposedInterval.getMidPoint() - parentRootToLeaf != 0)
                    rectangles.put(child.name, proposedInterval.getMidPoint() - parentRootToLeaf);
                weightTreeHelper(proposedInterval.getMidPoint(), ((ORLatticeNode) child).children.get(0), rectangles);
            }
        }
    }

    void subtractCol(LatticeNode node) {
        intervals.clear();
        if (isLeaf) {
            if (node == null)
                tempValue = value;
            else tempValue = value - node.getValue();
        } else {
            for (int i = 0; i < children.get(0).size(); i++) {
                if (node == null)
                    ((ORLatticeNode) children.get(0).get(i)).subtractCol(node);
                else
                    ((ORLatticeNode) children.get(0).get(i)).subtractCol(node.children.get(0).get(i));
            }
        }
    }

    double getReconstructedValue() {
        return reconstructedValue;
    }

    void setReconstructedValue(double value) {
        reconstructedValue = value;
    }

    void addToReconstructedValue(double weight) {
        if (isLeaf)
            reconstructedValue += weight;
        else {
            List<LatticeNode> childList = children.get(0).isEmpty() ? children.get(1) : children.get(0);
            for (LatticeNode child : childList) {
                ((ORLatticeNode) child).addToReconstructedValue(weight);
            }
        }
    }
}
