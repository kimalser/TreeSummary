package treeSummary;

import genericStructures.Lattice;
import genericStructures.SummaryType;

public class TSLattice extends Lattice {
    static double alpha = 0.1;

    public TSLattice(String[] files, double alphaVal) {
        super(files);
        alpha = alphaVal;
        createLattice(files, SummaryType.TreeSummary);
    }

    public void annotate() {
        ((TSLatticeNode) root).annotate();
    }

    public void print() {
        ((TSLatticeNode) root).printLatticeAnnotations();
    }

    public TSNode constructTreeSummary() {
        return ((TSLatticeNode) root).constructSummary();
    }

}
