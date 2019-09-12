package main;

import exhaustiveTrees.ExhaustiveTrees;
import genericStructures.ReconstructionError;
import overlappingRectangles.ORLattice;
import cascadingAnalysts.CALattice;
import cascadingAnalysts.CALatticeNode;
import treeSummary.*;
import utilities.Utility;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

public class Main {
    /* VG Sales */
    private static String valuesFile = "data/video games sales/EU_JP_ratio.txt";
    private static String[] hierarchies = {"data/video games sales/hierarchies/platforms.txt",
            "data/video games sales/hierarchies/genres.txt"};

    /* Census 1 */
    // private static String valuesFile = "data/us census/ratios16-17.txt";
    /* for size testing, vary the geographic dimension, it is available in 4 sizes: [geo_1, geo_2, geo_3, geo_all].txt */
    /* for dimensionality testing, refer to the hierarchy sets below */
    // private static String[] hierarchies = {"data/us census/hierarchies/geo_all.txt", "data/us census/hierarchies/sex_age_edu.txt"};
    // private static String[] hierarchies = {"data/us census/hierarchies/geo_all.txt", "data/us census/hierarchies/sex_age.txt", "data/us census/hierarchies/edu.txt"};
    // private static String[] hierarchies = {"data/us census/hierarchies/geo_all.txt", "data/us census/hierarchies/sex.txt", "data/us census/hierarchies/age.txt", "data/us census/hierarchies/edu.txt"};

    /* Census 2 */
    // private static String valuesFile = "data/us census/ratios06-17.txt";
    // private static String[] hierarchies = {"data/us census/hierarchies/year.txt", "data/us census/hierarchies/counties.txt", "data/us census/hierarchies/sex.txt",
    //        "data/us census/hierarchies/age.txt", "data/us census/hierarchies/edu.txt"};


    // a map of values to assign to lattice cells
    public static HashMap<String, Double> valMap;

    public static void main(String[] args) {
        double alpha = 0.1;
        boolean outputErrors = false;
        Utility.readValuesFile(true, valuesFile);
        try {
            runOverlappingRectangles(alpha, 10, outputErrors);
            int budget = runTreeSummaryCode(alpha, outputErrors);
            runCascadingAnalystsCode(budget, true, false, outputErrors);
            runExhaustiveTrees(alpha); // note that this baseline (ALL-T) is extremely slow
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // returns the size of the summary tree
    private static int runTreeSummaryCode(double alpha, boolean outputErrors) throws Exception {
        TSLattice lattice = new TSLattice(hierarchies, alpha);
        // System.out.println("number of lattice nodes: " + lattice.getLatticeSize());
        // System.out.println("number of leaves: " + lattice.getLatticeLeavesNum() + "\n");

        long startTime = System.currentTimeMillis();
        lattice.annotate();
        TSNode treeSummary = lattice.constructTreeSummary();
        long endTime = System.currentTimeMillis();

        BufferedWriter bw = null;
        if (outputErrors) {
            String filename = "data/SMAPE/TS_" + String.format("%.2f", alpha) + ".txt";
            bw = new BufferedWriter(new FileWriter(filename));
        }
        ReconstructionError error = treeSummary.getReconstructionError(lattice.getMap(), bw);

        int size = treeSummary.getTsSize();
        System.out.println("TS size = " + size);
        System.out.println("TS average error = " + error.sum / lattice.getLatticeLeavesNum());
        System.out.println("TS worst error = " + error.worst);
        System.out.println("TS execution time: " + (endTime - startTime) + "\n");

        if (bw != null)
            bw.close();

        return size;
    }

    private static void runCascadingAnalystsCode(int budget, boolean takeLog, boolean levelByLevel, boolean outputErrors) throws Exception {
        CALattice lattice = new CALattice(hierarchies, takeLog, levelByLevel, budget);
        CALatticeNode root = (CALatticeNode) lattice.getRoot();

        long startTime = System.currentTimeMillis();

        root.calculateValue();
        if (!levelByLevel)
            root.summarize();
        else root.summarizeSpaceEfficient();

        long endTime = System.currentTimeMillis();

        BufferedWriter bw = null;
        if (outputErrors) {
            String filename = "data/SMAPE/CA_" + budget + ".txt";
            bw = new BufferedWriter(new FileWriter(filename));
        }

        ReconstructionError error = lattice.getReconstructionError(bw);
        System.out.println("CA size = " + lattice.getFinalSize());
        System.out.println("CA average error = " + error.sum / lattice.getLatticeLeavesNum());
        System.out.println("CA worst error = " + error.worst);
        System.out.println("CA execution time: " + (endTime - startTime) + "\n");

        if (bw != null)
            bw.close();
    }


    private static void runOverlappingRectangles(double alpha, int times, boolean outputErrors) throws Exception {
        ORLattice lattice = new ORLattice(hierarchies, alpha);

        int worstSize = 0;
        int allSizes = 0;
        long allExecutionTime = 0;
        ReconstructionError reconstructionError = new ReconstructionError();

        BufferedWriter bw = null;
        if (outputErrors) {
            String filename = "data/SMAPE/OR_" + String.format("%.2f", alpha) + ".txt";
            bw = new BufferedWriter(new FileWriter(filename));
        }

        for (int i = 0; i < times; i++) {
            long startTime = System.currentTimeMillis();
            lattice.findRectangles();
            long endTime = System.currentTimeMillis();
            allExecutionTime += endTime - startTime;

            reconstructionError = lattice.getReconstructionError(reconstructionError, bw);
            allSizes += lattice.getRectanglesSize();
            if (lattice.getRectanglesSize() > worstSize)
                worstSize = lattice.getRectanglesSize();
        }

        System.out.println("K average size = " + allSizes / times);
        System.out.println("K worst size = " + worstSize);
        System.out.println("K average error = " + reconstructionError.sum / times / lattice.getLatticeLeavesNum());
        System.out.println("K worst error = " + reconstructionError.worst / times);
        System.out.println("K average execution time: " + allExecutionTime / times + "\n");

        if (bw != null)
            bw.close();
    }

    private static void runExhaustiveTrees(double alpha) {
        ExhaustiveTrees lattice = new ExhaustiveTrees(hierarchies, alpha);
        lattice.iterate();

        System.out.println("ALL-T size = " + lattice.bestSize);
        System.out.println("ALL-T average error = " + lattice.error.sum / lattice.getLatticeLeavesNum());
        System.out.println("ALL-T worst error = " + lattice.error.worst);
        System.out.println("ALL-T execution time: " + lattice.totalExecutionTime);
        System.out.println("ALL-T number of trees: " + lattice.treeNumber);
    }
}
