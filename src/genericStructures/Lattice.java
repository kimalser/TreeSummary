package genericStructures;

import cascadingAnalysts.CALattice;
import exhaustiveTrees.ExhaustiveTreesNode;
import main.Main;
import treeSummary.TSLatticeNode;
import cascadingAnalysts.CALatticeNode;
import overlappingRectangles.ORLatticeNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

// Lattice is a product of tree hierarchies
// Because lattice construction is pretty much the same for all approaches, it's a single class that is extended by particular problem instances.
// However, in all approaches, the LatticeNodes are quite different, that's why we use switch cases below.

public class Lattice {
    protected LatticeNode root;
    protected Map<String, LatticeNode> map; // name of node as key, node as value
    public static int dims;
    private SummaryType type;

    public Lattice(String[] files) {
        dims = files.length;
        map = new HashMap<>();
    }

    public Map<Integer, TreeNode> createLattice(String[] files, SummaryType type) {
        this.type = type;
        Map<Integer, TreeNode> trees = new HashMap<>(); // dimension # as key, treenode as value

        for (int i = 0; i < files.length; i++) {
            TreeNode tree = readStructure(files[i]);
            trees.put(i, tree);
        }
        switch (type) {
            case TreeSummary:
                root = new TSLatticeNode(getNodeName(trees));
                break;
            case CascadingAnalysts:
                root = new CALatticeNode(getNodeName(trees));
                break;
            case CascadingAnalystsLevelByLevel:
                root = new CALatticeNode(getNodeName(trees));
                CALattice.levelMap.put(0, new ArrayList<>());
                CALattice.levelMap.get(0).add((CALatticeNode) root);
                break;
            case OverlappingRectangles:
                root = new ORLatticeNode(getNodeName(trees));
                break;
            case ExhaustiveTrees:
                root = new ExhaustiveTreesNode(getNodeName(trees));
                break;
            default:
                root = new LatticeNode(getNodeName(trees));
        }

        map.put(root.name, root);
        if (type.equals(SummaryType.CascadingAnalystsLevelByLevel))
            createLatticeHelperCASpaceEfficient(root, trees, 1);
        else createLatticeHelper(root, trees);
        return trees;
    }

    private void createLatticeHelperCASpaceEfficient(LatticeNode latticeNode, Map<Integer, TreeNode> trees, int level) {
        for (Integer curTreeKey : trees.keySet()) {
            for (TreeNode curChild : trees.get(curTreeKey).getChildren()) {
                String name = getNodeName(trees, curChild, curTreeKey);
                if (!map.containsKey(name)) {
                    CALatticeNode newNode = new CALatticeNode(name);
                    map.put(name, newNode);

                    if (!CALattice.levelMap.containsKey(level))
                        CALattice.levelMap.put(level, new ArrayList<>());
                    CALattice.levelMap.get(level).add(newNode);

                    Map<Integer, TreeNode> temp = copyMap(trees);
                    temp.put(curTreeKey, curChild);
                    createLatticeHelperCASpaceEfficient(newNode, temp, level + 1);
                }
                latticeNode.addChild(map.get(name), curTreeKey);
                map.get(name).setParent(latticeNode, curTreeKey);
            }

            if (!hasChildren(trees.values())) { // if the node is a leaf
                latticeNode.setValue(Main.valMap.get(latticeNode.name));
            } else {
                latticeNode.setValue(Double.NaN);
            }
        }
    }

    private void createLatticeHelper(LatticeNode latticeNode, Map<Integer, TreeNode> trees) {
        for (Integer curTreeKey : trees.keySet()) {
            for (TreeNode curChild : trees.get(curTreeKey).getChildren()) {
                String name = getNodeName(trees, curChild, curTreeKey);
                if (!map.containsKey(name)) {
                    LatticeNode newNode;
                    switch (type) {
                        case TreeSummary:
                            newNode = new TSLatticeNode(name);
                            break;
                        case CascadingAnalysts:
                            newNode = new CALatticeNode(name);
                            break;
                        case OverlappingRectangles:
                            newNode = new ORLatticeNode(name);
                            break;
                        case ExhaustiveTrees:
                            newNode = new ExhaustiveTreesNode(name);
                            break;
                        default:
                            newNode = new LatticeNode(name);
                    }
                    map.put(name, newNode);
                    Map<Integer, TreeNode> temp = copyMap(trees);
                    temp.put(curTreeKey, curChild);
                    createLatticeHelper(newNode, temp);
                }
                latticeNode.addChild(map.get(name), curTreeKey);
                map.get(name).setParent(latticeNode, curTreeKey);
            }

            if (!hasChildren(trees.values())) { // if the node is a leaf
                latticeNode.setValue(Main.valMap.get(latticeNode.name));
            } else if (type.equals(SummaryType.CascadingAnalysts))
                latticeNode.setValue(Double.NaN);
        }
    }

    private TreeNode readStructure(String filename) {
        Set<String> parsedLines = new HashSet<>(); // some lines may repeat. do not add those nodes
        TreeNode tree = null;
        try {
            File file = new File(filename);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (!parsedLines.contains(line) && !line.isEmpty()) {
                    parsedLines.add(line);
                    String[] splitArray = line.split(";");
                    if (splitArray.length != 2) {
                        throw new Exception("Line '" + line + "' was not parsed correctly!");
                    }
                    if (tree == null) {
                        tree = new TreeNode(null, splitArray[0]);
                    }
                    TreeNode parent = tree.findNode(splitArray[0]);
                    if (parent != null) {
                        TreeNode child = new TreeNode(parent, splitArray[1]);
                        parent.addChild(child);
                    }
                }
            }
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tree;
    }

    public LatticeNode getRoot() {
        return root;
    }

    public Map<String, LatticeNode> getMap() {
        return map;
    }

    private String getNodeName(Map<Integer, TreeNode> trees) {
        String name = "";
        for (int d = 0; d < dims - 1; d++)
            name += trees.get(d).getName() + ",";
        name += trees.get(dims - 1).getName();
        return name;
    }

    private String getNodeName(Map<Integer, TreeNode> trees, TreeNode child, int dim) {
        String name = "";
        for (int d = 0; d < dims - 1; d++) {
            if (d != dim)
                name += trees.get(d).getName() + ",";
            else name += child.getName() + ",";
        }
        if (dims - 1 != dim)
            name += trees.get(dims - 1).getName();
        else name += child.getName();
        return name;
    }

    private Map<Integer, TreeNode> copyMap(Map<Integer, TreeNode> map) {
        Map<Integer, TreeNode> copy = new HashMap<>();
        for (int key : map.keySet()) {
            copy.put(key, map.get(key));
        }
        return copy;
    }

    private boolean hasChildren(Collection<TreeNode> trees) {
        for (TreeNode t : trees)
            if (!t.getChildren().isEmpty())
                return true;
        return false;
    }

    public int getLatticeSize() {
        return map.size();
    }

    public int getLatticeLeavesNum() {
        int count = 0;
        for (LatticeNode n : map.values()) {
            if (n.isLeaf)
                count++;
        }
        return count;
    }
}
