package treeSummary;

import genericStructures.Interval;
import genericStructures.IntervalPoint;
import genericStructures.Lattice;
import genericStructures.LatticeNode;
import utilities.Utility;

import java.util.*;

import static treeSummary.TSLattice.alpha;

// TreeSummary algorithm is implemented in this class
public class TSLatticeNode extends LatticeNode {

    private List<Annotation> annotations;
    private boolean annotated = false;

    public TSLatticeNode(String name) {
        super(name);
        this.annotations = new ArrayList<>();
    }

    public void setValue(double value) {
        this.value = value;
        isLeaf = true;
    }

    void annotate() {
        if (isLeaf && !annotated) {
            Interval interval = value > 0 ? new Interval(value - value * alpha, value + value * alpha) :
                    new Interval(value + value * alpha, value - value * alpha);
            annotations.add(new Annotation(interval, 1, 0));
        } else {
            for (int dim = 0; dim < TSLattice.dims; dim++) {
                if (!children.get(dim).isEmpty()) {
                    for (LatticeNode child : children.get(dim))
                        if (!((TSLatticeNode) child).annotated) {
                            ((TSLatticeNode) child).annotate();
                        }

                    Map<IntervalPoint, IntervalPoint> pointMap = new HashMap<>();
                    List<IntervalPoint> points = new ArrayList<>();

                    for (LatticeNode child : children.get(dim)) {
                        for (Annotation a : ((TSLatticeNode) child).annotations) {
                            Utility.addPointIntervals(pointMap, points, a.proposedInterval);
                        }
                    }

                    if (!points.isEmpty()) {
                        List<double[]> mostOverlappedIntervals = Utility.getMostOverlappedIntervals(points, pointMap);

                        for (double[] overlap : mostOverlappedIntervals) {
                            int cost = 1; // 1 for the root node
                            for (LatticeNode child : children.get(dim)) {
                                cost += ((TSLatticeNode) child).getLeastCost();  // + cost of each child
                                if (((TSLatticeNode) child).cheapestIncludes(overlap))
                                    cost--; // but -1 if child matches root
                            }
                            Annotation a = new Annotation(new Interval(overlap[0], overlap[1]), cost, dim);
                            annotations.add(a);
                        }
                    }
                }
            }
            filter();
        }
        annotated = true;
    }

    TSNode constructSummary() {
        TSNode root = new TSNode(null, this.name);

        Annotation a = annotations.get(0); // get any annotation, all annotations at root are optimal
        root.setWeight(a.proposedInterval.getMidPoint());
        root.setClosestAncestorWeight(a.proposedInterval.getMidPoint());
        constructSummaryHelper(root, children.get(a.dim));

        return root;
    }

    private void constructSummaryHelper(TSNode parent, List<LatticeNode> children) {
        for (LatticeNode child : children) {

            if (!((TSLatticeNode) child).annotations.isEmpty()) {
                TSNode n = new TSNode(parent, child.name);

                parent.addChild(n);

                int dim = -1;
                for (Annotation a : ((TSLatticeNode) child).annotations) {
                    if (a.proposedInterval.containsPoint(parent.getClosestAncestorWeight())) {
                        n.setWeight(Double.NaN);
                        n.setClosestAncestorWeight(parent.getClosestAncestorWeight());
                        dim = a.dim;
                        break;
                    }
                }
                if (dim == -1) {
                    Annotation a = ((TSLatticeNode) child).annotations.get(0);
                    n.setWeight(a.proposedInterval.getMidPoint());
                    n.setClosestAncestorWeight(a.proposedInterval.getMidPoint());
                    dim = a.dim;
                }
                constructSummaryHelper(n, ((TSLatticeNode) child).children.get(dim));
            }
        }
    }

    private boolean cheapestIncludes(double[] weight) {
        filter();
        for (Annotation a : annotations) {
            if (a.proposedInterval.intersects(new Interval(weight[0], weight[1]))) {
                return true;
            }
        }
        return false;
    }

    // filters out suboptimal annotations
    private void filter() {
        int minCost = getLeastCost();
        List<Annotation> temp = new ArrayList<>();
        for (Annotation a : annotations) {
            if (a.cost == minCost)
                temp.add(a);
        }
        annotations = temp;
    }

    private int getLeastCost() {
        Annotation minCostAnnotation = Collections.min(annotations, Comparator.comparing(a -> a.cost));
        return minCostAnnotation.cost;
    }

    public void printLatticeAnnotations() {
        System.out.print(name);
        for (Annotation a : annotations) {
            System.out.print(" <[" + String.format("%.4f", a.proposedInterval.start) + ", " + String.format("%.4f", a.proposedInterval.end) + "], " + a.cost + ", " + a.dim + "> ");
        }
        System.out.println();
        for (List<LatticeNode> l : children) {
            for (LatticeNode child : l) {
                ((TSLatticeNode) child).printLatticeAnnotations(1);
            }
        }
    }

    private void printLatticeAnnotations(int spaces) {
        for (int i = 0; i < spaces; i++)
            System.out.print("  ");
        if (!isLeaf)
            System.out.print(name);
        else System.out.print(name + ": ");

        for (Annotation a : annotations) {
            System.out.print(" <[" + String.format("%.4f", a.proposedInterval.start) + ", " + String.format("%.4f", a.proposedInterval.end) + "], " + a.cost + ", " + a.dim + "> ");
        }
        System.out.println();

        for (List<LatticeNode> l : children) {
            for (LatticeNode child : l) {
                ((TSLatticeNode) child).printLatticeAnnotations(spaces + 1);
            }
        }
    }

    public void printNodeInfo() {
        System.out.println("Node: " + name);
        for (int dim = 0; dim < Lattice.dims; dim++) {
            if (parents.get(dim) == null)
                System.out.println("Parent in dim#" + (dim + 1) + ": null");
            else System.out.println("Parent in dim#" + (dim + 1) + ": " + parents.get(dim).name);
        }

        for (int dim = 0; dim < Lattice.dims; dim++) {
            if (!children.get(dim).isEmpty()) {
                System.out.println("Children in dim#" + (dim + 1) + ": ");
                for (LatticeNode n : children.get(dim)) {
                    System.out.println(n.name + " ");
                }
                System.out.println();
            }
        }

        if (isLeaf) {
            System.out.println("Leaf value: " + value);
        }

        System.out.print("Annotations: ");
        for (Annotation a : annotations) {
            System.out.print(" <[" + String.format("%.4f", a.proposedInterval.start) + ", " + String.format("%.4f", a.proposedInterval.end) + "], " + a.cost + ", " + a.dim + "> ");
        }
        System.out.println("\n");
    }
}
