package cascadingAnalysts;

import java.util.List;

class Solution implements Comparable<Solution> {
    List<CALatticeNode> nodes; // nodes in the max weight union
    double weight;

    Solution(List<CALatticeNode> nodes, double weight) {
        this.nodes = nodes;
        this.weight = weight;
    }

    @Override
    public int compareTo(Solution o) {
        return Double.compare(weight, o.weight);
    }
}