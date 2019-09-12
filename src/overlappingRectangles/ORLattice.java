package overlappingRectangles;

import genericStructures.*;
import utilities.Utility;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

// implementation of the TREExTREE algorithm from the following paper:
// http://drops.dagstuhl.de/opus/volltexte/2011/3024/pdf/32.pdf
// also available at https://arxiv.org/pdf/1101.1941.pdf

public class ORLattice extends Lattice {
    static double alpha = 0.0;
    private HashMap<String, String> nodeColumns; // key is a node name, value is the name of a randomly chosen leaf descendant of that node
    private HashMap<String, Double> rectangles; // key is rectangle (node) name, value is its corresponding weight
    private Map<Integer, TreeNode> trees; // contains root nodes of the two tree hierarchies

    public ORLattice(String[] files, double alphaVal) {
        super(files);
        alpha = alphaVal;
        trees = createLattice(files, SummaryType.OverlappingRectangles);
        nodeColumns = new HashMap<>();
        rectangles = new HashMap<>();
    }

    // step 1 from the TREE x TREE algorithm
    private void chooseLeaves(TreeNode node) {
        TreeNode temp = node;
        while (!temp.getChildren().isEmpty()) {
            temp = temp.getChildren().get(ThreadLocalRandom.current().nextInt(0, temp.getChildren().size()));
        }
        nodeColumns.put(node.getName(), temp.getName());
        for (TreeNode child : node.getChildren())
            chooseLeaves(child);
    }

    public void findRectangles() {
        TreeNode T1 = trees.get(0);
        TreeNode T2 = trees.get(1);
        rectangles.clear();
        chooseLeaves(T2);
        String u = nodeColumns.get(T2.getName()); // refer to step 2 of the TREExTREE algorithm
        ORLatticeNode n = (ORLatticeNode) map.get(T1.getName() + "," + u);
        n.subtractCol(null); //subtracting parent column values
        n.computeDS();
        Map<String, Double> temp = n.weightTree();
        for (String key : temp.keySet()) {
            String rectangleName = key.split(",")[0] + "," + T2.getName();
            rectangles.put(rectangleName, temp.get(key));
        }
        for (TreeNode child : T2.getChildren())
            findRectangles(child);
    }

    private void findRectangles(TreeNode tn) {
        TreeNode T1 = trees.get(0);
        String u = nodeColumns.get(tn.getName());
        ORLatticeNode n = (ORLatticeNode) map.get(T1.getName() + "," + u);
        n.subtractCol((map.get(T1.getName() + "," + nodeColumns.get(tn.getParent().getName())))); //subtracting parent column values
        n.computeDS();
        Map<String, Double> temp = n.weightTree();
        for (String key : temp.keySet()) {
            String rectangleName = key.split(",")[0] + "," + tn.getName();
            rectangles.put(rectangleName, temp.get(key));
        }
        for (TreeNode child : tn.getChildren())
            findRectangles(child);
    }

    public int getRectanglesSize() {
        return rectangles.size();
    }

    public ReconstructionError getReconstructionError(ReconstructionError error, BufferedWriter bw) throws Exception {
        double worstError = 0;
        for (String s : rectangles.keySet()) {
            ((ORLatticeNode) map.get(s)).addToReconstructedValue(rectangles.get(s));
        }

        for (LatticeNode node : map.values()) {
            if (node.isLeaf) {
                double reconstructed = ((ORLatticeNode) node).getReconstructedValue();
                ((ORLatticeNode) node).setReconstructedValue(0); //initializing to 0 for future runs

                if (node.getValue() != 0 || reconstructed != 0) {
                    double e = Utility.calculateSMAPE(node.getValue(), reconstructed);
                    if (bw != null)
                        bw.write(e + ", ");
                    if (e > worstError)
                        worstError = e;
                    error.sum += e;
                }
            }
        }
        error.worst += worstError;
        return error;
    }
}
