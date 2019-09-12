package exhaustiveTrees;

import genericStructures.Lattice;
import genericStructures.LatticeNode;
import genericStructures.ReconstructionError;
import genericStructures.SummaryType;

import java.util.HashMap;
import java.util.Stack;

public class ExhaustiveTrees extends Lattice {
    static double alpha;

    static HashMap<String, Integer> nodeDimsMap;
    static Stack<LatticeNode> traversalStack;

    public ReconstructionError error; // stores errors of the best tree

    public int bestSize = Integer.MAX_VALUE;
    public int treeNumber = 0;

    public long totalExecutionTime = 0;

    public ExhaustiveTrees(String[] files, double alphaVal) {
        super(files);
        createLattice(files, SummaryType.ExhaustiveTrees);
        traversalStack = new Stack<>();
        nodeDimsMap = new HashMap<>();
        alpha = alphaVal;
        initDimensionsMap();
        ((ExhaustiveTreesNode) root).pushDescendantsToStack();
    }


    public void iterate() {
        boolean treeChanged = true;
        int currentSize;
        while (!traversalStack.isEmpty()) {
            if (treeChanged) {
                treeNumber++;

                long startTime = System.currentTimeMillis();
                ((ExhaustiveTreesNode) root).computeDS();
                ((ExhaustiveTreesNode) root).weightTree();
                long endTime = System.currentTimeMillis();
                totalExecutionTime += endTime - startTime;

                currentSize = getCurrentSize();
                if (currentSize < bestSize) {
                    bestSize = currentSize;
                    updateReconstructionError();
                }
                treeChanged = false;
            }

            LatticeNode top = traversalStack.peek();

            int curDim = nodeDimsMap.get(top.name);
            if (curDim >= dims - 1) {// all dimensions explored
                traversalStack.pop();
            } else {
                boolean foundDim = false;
                for (int dim = curDim + 1; dim < dims; dim++) { // find next unexplored dimension
                    if (!top.children.get(dim).isEmpty()) {
                        foundDim = true;
                        nodeDimsMap.put(top.name, dim);
                        treeChanged = true;

                        // since it's a new dimension - need to add descendants in the stack
                        for (LatticeNode child : ((ExhaustiveTreesNode) top).getChildren()) {
                            ((ExhaustiveTreesNode) child).initNodeDimensions();
                            ((ExhaustiveTreesNode) child).pushDescendantsToStack();
                        }

                        for (LatticeNode node : ((ExhaustiveTreesNode) top).getPreviousNodes(root)) {
                            ((ExhaustiveTreesNode) node).initNodeDimensions();
                            ((ExhaustiveTreesNode) node).pushDescendantsToStack();
                        }
                        break;
                    }
                }
                if (!foundDim)
                    traversalStack.pop();
            }
        }
    }

    // for all intermediate nodes, identify the initial drill-down dimension and save it into a map
    private void initDimensionsMap() {
        for (LatticeNode node : map.values()) {
            if (!node.isLeaf) {
                for (int dim = 0; dim < dims; dim++) {
                    if (!node.children.get(dim).isEmpty()) {
                        nodeDimsMap.put(node.name, dim);
                        break;
                    }
                }
            }
        }
    }

    private int getCurrentSize() {
        return ((ExhaustiveTreesNode) root).getCurrentSize(root);
    }

    private void updateReconstructionError() {
        error = new ReconstructionError();
        error = ((ExhaustiveTreesNode) root).getReconstructionError(error);
    }

}
