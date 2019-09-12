package cascadingAnalysts;

import genericStructures.ReconstructionError;
import main.Main;
import genericStructures.Lattice;
import genericStructures.LatticeNode;
import genericStructures.SummaryType;
import utilities.Utility;

import java.io.BufferedWriter;
import java.util.HashMap;
import java.util.List;

import static java.lang.Math.exp;

public class CALattice extends Lattice {

    public static int MAX_OUTPUT_SIZE;
    public static HashMap<Integer, List<CALatticeNode>> levelMap; //map of level number (depth) and corresponding nodes
    static boolean takeLog;

    public CALattice(String[] files, boolean takeLogArg, boolean levelByLevel, int summarySize) {
        super(files);
        takeLog = takeLogArg;
        MAX_OUTPUT_SIZE = summarySize;
        if (levelByLevel) {
            levelMap = new HashMap<>();
            createLattice(files, SummaryType.CascadingAnalystsLevelByLevel);
        } else {
            createLattice(files, SummaryType.CascadingAnalysts);
        }
    }

    public ReconstructionError getReconstructionError(BufferedWriter bw) throws Exception {
        ReconstructionError error = new ReconstructionError();
        Solution solution = ((CALatticeNode) root).getFinalSet();

        for (CALatticeNode node : solution.nodes) {
            int descNum = node.getLeafDescendantNum();
            double val = node.getValue() / descNum;
            node.setReconstructedValue(val);
        }

        for (LatticeNode node : map.values()) {
            if (node.isLeaf) {
                double origVal = Main.valMap.get(node.name);
                double recVal;
                if (takeLog)
                    recVal = exp(((CALatticeNode) node).getReconstructedValue());
                else recVal = ((CALatticeNode) node).getReconstructedValue();
                double e = Utility.calculateSMAPE(origVal, recVal);
                if (node.getValue() == 0 && ((CALatticeNode) node).getReconstructedValue() == 0)
                    e = 0;
                if (bw != null)
                    bw.write(e + ",");
                error.sum += e;
                if (e > error.worst) {
                    error.worst = e;
                }
            }
        }
        return error;
    }

    public void printSets() {
        Solution solution = ((CALatticeNode) root).getFinalSet();
        System.out.println("There are " + solution.nodes.size() + " nodes in the summary set:");
        for (CALatticeNode n : solution.nodes)
            System.out.println(n.name + ": " + n.getValue());
    }

    public int getFinalSize() {
        Solution solution = ((CALatticeNode) root).getFinalSet();
        return solution.nodes.size();
    }

}
