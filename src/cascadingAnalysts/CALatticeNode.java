package cascadingAnalysts;

import genericStructures.Lattice;
import genericStructures.LatticeNode;

import java.util.*;

import static cascadingAnalysts.CALattice.MAX_OUTPUT_SIZE;
import static java.lang.Math.log;

// refer to https://dl.acm.org/citation.cfm?id=3183713.3183745
// for the descriptions of summarize() and combine()

// in this class all nodes will have the "value" field defined
// by the sum of values of all cells that the node covers.
// at the start, leaf nodes have values, internal nodes are assigned NaN


public class CALatticeNode extends LatticeNode {

    private Map<Integer, Solution> allSets; // map of max weight solution per map-key num of nodes
    private double weight;
    private double reconstructedValue = 0;
    private boolean summarized = false;

    public CALatticeNode(String name) {
        super(name);
        allSets = new HashMap<>();
    }

    @Override
    public void setValue(double value) {
        if (CALattice.takeLog)
            this.value = log(value);
        else this.value = value;
        this.isLeaf = !Double.isNaN(value);
    }

    public void summarize() {
        if (!summarized) {
            if (isLeaf) {
                List<CALatticeNode> l = new ArrayList<>();
                l.add(this);
                allSets.put(0, new Solution(new ArrayList<>(), 0));
                allSets.put(1, new Solution(l, weight));
            } else {
                for (List childrenList : children) {
                    if (!childrenList.isEmpty()) {
                        for (Object child : childrenList) {
                            if (!((CALatticeNode) child).summarized) {
                                ((CALatticeNode) child).summarize();
                            }
                        }
                        allSets.put(0, new Solution(new ArrayList<>(), 0));
                        combine(childrenList);
                    }
                }
            }
        }
        summarized = true;
    }

    // same as previous summarize() but does it level by level and
    // empties the previous levels when done
    public void summarizeSpaceEfficient() {
        int maxLevel = Collections.max(CALattice.levelMap.keySet());
        for (int i = maxLevel; i >= 0; i--) {
            summarizeSpaceEfficientHelper(CALattice.levelMap.get(i));
            //freeing memory
            if (i < maxLevel)
                freeMemory(CALattice.levelMap.get(i + 1));
        }
    }

    private void summarizeSpaceEfficientHelper(List<CALatticeNode> nodesAtLevel) {
        for (CALatticeNode node : nodesAtLevel) {
            if (node.isLeaf) {
                List<CALatticeNode> l = new ArrayList<>();
                l.add(node);
                node.allSets.put(0, new Solution(new ArrayList<>(), 0));
                node.allSets.put(1, new Solution(l, node.weight));
            } else {
                for (List childrenSet : node.children) {
                    if (!childrenSet.isEmpty()) {
                        node.allSets.put(0, new Solution(new ArrayList<>(), 0));
                        node.combine(childrenSet);
                    }
                }
            }
        }
    }

    private void freeMemory(List<CALatticeNode> nodesAtLevel) {
        for (LatticeNode node : nodesAtLevel) {
            ((CALatticeNode) node).allSets = null; // to be garbage collected
        }
    }

    private void combine(List<CALatticeNode> list) {
        double S[][] = new double[list.size() + 1][MAX_OUTPUT_SIZE + 1];
        List<List<List<CALatticeNode>>> solutions = new ArrayList<>(); // makes an equivalent of 2 dim array where each element is a list of included nodes
        solutions.add(null); // placeholder to match index in paper (for easier understanding)
        for (int m = 1; m <= list.size(); m++) {
            solutions.add(new ArrayList<>());
            solutions.get(m).add(new ArrayList<>()); // placeholder to match index in paper (for easier understanding)
            int t;
            for (int j = 1; j <= MAX_OUTPUT_SIZE; j++) {
                List<CALatticeNode> temp = new ArrayList<>();
                if (m == 1) {
                    t = j;
                    if (list.get(m - 1).isLeaf && t > 1) // to not store the same weight at the leaf k times
                        t = 1;
                    S[m][j] = list.get(m - 1).allSets.get(t).weight;
                    temp.addAll(list.get(m - 1).allSets.get(t).nodes);
                } else {
                    double maxWeight = 0;
                    for (int p = 0; p <= j; p++) {
                        int q = j - p;
                        t = q;
                        if (list.get(m - 1).isLeaf && t > 1) // to not store the same weight at the leaf k times
                            t = 1;
                        double union = S[m - 1][p] + list.get(m - 1).allSets.get(t).weight;
                        if (maxWeight < union) {
                            maxWeight = union;
                            temp.clear();
                            temp.addAll(solutions.get(m - 1).get(p));
                            temp.addAll(list.get(m - 1).allSets.get(t).nodes);
                        }
                    }
                    S[m][j] = maxWeight;
                }
                solutions.get(m).add(temp);
            }
        }

        for (int i = 1; i <= MAX_OUTPUT_SIZE; i++) {
            if (!allSets.containsKey(i) || (allSets.containsKey(i) &&
                    (allSets.get(i).weight < S[list.size()][i] || allSets.get(i).weight < weight))) {
                if (S[list.size()][i] <= weight) { // a singleton set {v}
                    List<CALatticeNode> temp = new ArrayList<>();
                    temp.add(this);
                    allSets.put(i, new Solution(temp, weight));
                } else {
                    // or the largest union of children weights
                    allSets.put(i, new Solution(solutions.get(list.size()).get(i), S[list.size()][i]));
                }
            }
        }
    }


    // a value for each node is the sum of values of nodes' children
    public double calculateValue() {
        if (!isLeaf && Double.isNaN(value)) {
            for (List<LatticeNode> list : children) {
                double tempVal = 0;
                for (LatticeNode node : list) {
                    tempVal += ((CALatticeNode) node).calculateValue();
                }
                if (!list.isEmpty())
                    value = tempVal;
            }
        }
        weight = Math.abs(value);
        return value;
    }

    public void printValues() {
        System.out.println(name + ": " + value);
        for (List<LatticeNode> l : children) {
            for (LatticeNode child : l) {
                ((CALatticeNode) child).printValues(1);
            }
        }
    }

    private void printValues(int spaces) {
        for (int i = 0; i < spaces; i++)
            System.out.print("  ");
        System.out.println(name + ": " + value + " | " + reconstructedValue);
        for (List<LatticeNode> l : children) {
            for (LatticeNode child : l) {
                ((CALatticeNode) child).printValues(spaces + 1);
            }
        }
    }

    Solution getFinalSet() {
        return allSets.getOrDefault(MAX_OUTPUT_SIZE, null);
    }

    double getReconstructedValue() {
        return reconstructedValue;
    }

    void setReconstructedValue(double val) {
        if (isLeaf) {
            reconstructedValue = val;
        } else {
            for (int d = 0; d < Lattice.dims; d++) {
                if (!children.get(d).isEmpty()) {
                    for (LatticeNode child : children.get(d))
                        ((CALatticeNode) child).setReconstructedValue(val);
                    break; // only important to get reconstructed values to the leaf nodes
                }
            }
        }
    }
}


