import java.util.Random;

// package termproject;

/**
 * Title:        Term Project: 2-4 Trees
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Cedarville University
 * @author       Sam Davis, Sam Marshall, Willem Vandermeij
 * @version 1.0
 */

public class TwoFourTree implements Dictionary {

    private Comparator treeComp;
    private int size = 0;
    private TFNode treeRoot = null;

    public TwoFourTree(Comparator comp) {
        treeComp = comp;
    }

    private TFNode root() {
        return treeRoot;
    }

    private void setRoot(TFNode root) {
        treeRoot = root;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Finds the first index that is greater than or equal to the key
     * 
     * @param node to be searched in
     * @param key to be compared to
     * @return index of the current node
     */
    private int findFirst(TFNode node, Object key) {
        int i;
        for (i = 0; i < node.getNumItems(); i++) {
            if (treeComp.isGreaterThanOrEqualTo(node.getItem(i).key(), key)) {
                break;
            }
        }
        return i;
    }

    /**
     * Finds the index where the current child is located
     * 
     * @param child the current node
     * @return the index of the parent node
     */
    private int whatChild(TFNode child) {
        TFNode parent = child.getParent();
        int i;
        for (i = 0; i <= parent.getNumItems(); i++) {
            if (parent.getChild(i) == child) { // changed to not use treeComp
                break;
            }
        }
        return i;
    }

    
    /**
     * Searches for the node where the key belongs
     * 
     * @param node to be searched in
     * @param key to be searched for
     * @return node where the key is first greater than or equal to is
     */
    private TFNode search(TFNode node, Object key) {
        int index = findFirst(node, key);
        if (node.getChild(index) == null) { // || node.getItem(index).key() == key so we could use search for remove element
            return node;
        }
        return search(node.getChild(index), key);
    }


    /**
     * Corrects overflow within the tree recursively
     * 
     * @param node node to be corrected
     */
    private void overflow(TFNode node) {

        // guard clause for when a node is not overflowed
        if (node.getNumItems() != 4) {
            return;
        }

        // correct root node overflow
        if (node == treeRoot) {
            node.setParent(new TFNode());
            setRoot(node.getParent()); // changed to use setRoot() method provided
            treeRoot.setChild(0, node);
        }

        TFNode newNode = new TFNode();
        TFNode parent = node.getParent();
        newNode.setParent(node.getParent());

        // set children's parents if children exist
        if (node.getChild(0) != null) {
            newNode.setChild(1, node.getChild(4));
            newNode.getChild(1).setParent(newNode);
            newNode.setChild(0, node.getChild(3));
            newNode.getChild(0).setParent(newNode);
        }
        
        newNode.addItem(0, node.getItem(3)); // changed to addItem so that children are not shifted
        node.deleteItem(3); // changed from removeItem to deleteItem

        int whatChild = whatChild(node);
        parent.insertItem(whatChild, node.getItem(2));
        parent.setChild(whatChild + 1, newNode);
        node.deleteItem(2); // changed from remove to delete

        overflow(node.getParent()); // check if parent node has been overflowed
    }
    

    private TFNode indorderSuccr(TFNode parent, int index) {

        // initialize the node that will be returned to parent
        TFNode retNode = parent;

        // if the child to the right of the key is not null set it as the return node
        if (retNode.getChild(index) != null) {
            retNode = retNode.getChild(index);

            // set the return node to the left child until the left child is null
            while (parent.getChild(0) != null) {
                retNode = retNode.getChild(0);
            }
        }
        return retNode;
    }


    private void leftTransfer(TFNode node, int index) {

        TFNode parent = node.getParent();
        TFNode leftSib = parent.getChild(index - 1);

        // handle kids
        node.setChild(1, node.getChild(0)); /* left child must be shifted */
        node.setChild(0, leftSib.getChild(leftSib.getNumItems()));
        if(node.getChild(0) != null) { /* added null check */
            node.getChild(0).setParent(node);
        }
        // leftSib.getChild(leftSib.getNumItems()).setParent(node);
        // leftSib.setChild(leftSib.getNumItems(), null);

        // move greatest value from left sibling to parent node
        // and the least value from the parent node to the current node
        node.addItem(0, parent.replaceItem(index - 1, leftSib.getItem(leftSib.getNumItems() - 1))); /* added - 1, changed to addItem */
        leftSib.removeItem(leftSib.getNumItems() - 1); /* added - 1 */
    }

    private void rightTransfer(TFNode node, int index) {

        TFNode parent = node.getParent();
        TFNode rightSib = parent.getChild(index + 1);

        // handle kids
        node.setChild(1, rightSib.getChild(0));
        if(node.getChild(0) != null) { /* added null check */
            node.getChild(1).setParent(node);
        }

        // move least value from right sibling to parent node
        // and the greatest value from the parent node to the current node
        node.addItem(0, parent.replaceItem(index, rightSib.getItem(0))); /* changed to addItem */
        rightSib.removeItem(0);
    }

    private void leftFusion(TFNode node, int index) {
        TFNode parent = node.getParent();
        TFNode leftSib = parent.getChild(index - 1);
        Item borrow = parent.removeItem(index - 1);
        leftSib.addItem(1, borrow); /* changed to index 1  from leftSib.getNumItems() - 1 */
        leftSib.setChild(2, node.getChild(0));
        if(leftSib.getChild(0) != null) {
            leftSib.getChild(2).setParent(leftSib);
        }
        leftSib.setParent(parent);
        parent.setChild(whatChild(node), leftSib);
    }

    private void rightFusion(TFNode node, int index) {
        TFNode parent = node.getParent();
        TFNode rightSib = parent.getChild(index + 1);
        Item borrow = parent.removeItem(index);
        rightSib.insertItem(0, borrow);
        rightSib.setChild(0, node.getChild(0));
        if(rightSib.getChild(0) != null) {
            rightSib.getChild(0).setParent(rightSib);
        }
    }


    /**
     * Corrects underflow within the tree using transfers and fusions
     * 
     * @param node node to be corrected
     */
    private void underflow(TFNode node) {

        // guard clause for when a node is not underflowed
        if (node.getNumItems() > 0) {
            return;
        }
        
        // handle underflow at root
        if (node == treeRoot) {
            setRoot(node.getChild(0)); // changed to use setRoot() method provided
            return;
        }

        int whatChild = whatChild(node);
        if (whatChild > 0 && node.getParent().getChild(whatChild - 1).getNumItems() > 2) {
            leftTransfer(node, whatChild);
        } else if (node.getParent().getChild(whatChild + 1).getNumItems() > 2) {
            rightTransfer(node, whatChild);
        } else if (whatChild > 0) {
            leftFusion(node, whatChild);
            underflow(node.getParent()); // check for potential underflow on parent node
        } else {
            rightFusion(node, whatChild);
            underflow(node.getParent()); //  check for potential underflow on parent node
        }
    }


    /**
     * Searches dictionary to determine if key is present
     * 
     * @param key to be searched for
     * @return object corresponding to key; null if not found
     */
    public Object findElement(Object key) {
        
        if (!treeComp.isComparable(key)) {
            throw new InvalidIntegerException("Key is of wrong type");
        }
       
        TFNode node = root();
        while (node != null) {
            int index = findFirst(node, key);
            if (treeComp.isEqual(node.getItem(index).key(), key)) {
                return node.getItem(index).element();
            }
            node = node.getChild(index); // set equal to correct child
        }
        return null;
    }


    /**
     * Inserts provided element into the Dictionary
     * 
     * @param key of object to be inserted
     * @param element to be inserted
     */
    public void insertElement(Object key, Object element) {
        if (!treeComp.isComparable(key)) {
            throw new InvalidIntegerException("Key is of wrong type");
        }
        
        // first, check to see if the tree is empty
        if (size == 0) {
            setRoot(new TFNode()); // changed to use setRoot() method provided
            treeRoot.addItem(0, new Item(key, element));
        }

        // search for the correct node, find the right index, and insert the item
        else {
            TFNode node = search(root(), key);
            int index = findFirst(node, key);
            node.insertItem(index, new Item(key, element));
            // check for overflow
            overflow(node);
        }
        size++;
    }


    /**
     * Searches dictionary to determine if key is present, then
     * removes and returns corresponding object
     * 
     * @param key of data to be removed
     * @return object corresponding to key
     * @exception ElementNotFoundException if the key is not in dictionary
     */
    public Object removeElement(Object key) throws ElementNotFoundException {
        // Check for a comparable key
        if(!treeComp.isComparable(key)) {
            throw new InvalidIntegerException("Key is of wrong type");
        }

        // Checks for empty tree
        if (size == 0) {
            throw new ElementNotFoundException("ERROR: Tree is empty");
        }

        // Find the node and index
        TFNode node = root();
        int index = -1;
        while (node != null) { // changed from using search because search only returns leaf nodes
            index = findFirst(node, key);
            if (treeComp.isEqual(node.getItem(index).key(), key)) {
                break;
            }
            node = node.getChild(index); // set equal to correct child
        }
        if (node == null) {
            throw new ElementNotFoundException("ERROR: No such item in tree");
        }

        // Checks for external node
        Item removed;
        if (node.getChild(0) == null) {
            removed = node.removeItem(index);
        }

        // Otherwise, the node is internal
        else {
            TFNode successorNode = indorderSuccr(node, index);
            Item successorItem = successorNode.removeItem(0);
            removed = node.getItem(index);
            node.insertItem(index, successorItem);
            node = successorNode;
        }

        // Check for underflow and return the removed item element
        underflow(node);
        return removed.element();
    }

    public static void main(String[] args) {
        Comparator myComp = new IntegerComparator();
        TwoFourTree myTree = new TwoFourTree(myComp);
        final int TEST_SIZE = 10000;

        // Integer myInt1 = new Integer(47);
        // myTree.insertElement(myInt1, myInt1);

        // Integer myInt2 = new Integer(83);
        // myTree.insertElement(myInt2, myInt2);

        // Integer myInt3 = new Integer(22);
        // myTree.insertElement(myInt3, myInt3);

        // Integer myInt4 = new Integer(16);
        // myTree.insertElement(myInt4, myInt4);

        // Integer myInt5 = new Integer(49);
        // myTree.insertElement(myInt5, myInt5);

        // Integer myInt6 = new Integer(100);
        // myTree.insertElement(myInt6, myInt6);

        // Integer myInt7 = new Integer(38);
        // myTree.insertElement(myInt7, myInt7);

        // Integer myInt8 = new Integer(3);
        // myTree.insertElement(myInt8, myInt8);

        // Integer myInt9 = new Integer(53);
        // myTree.insertElement(myInt9, myInt9);

        // Integer myInt10 = new Integer(66);
        // myTree.insertElement(myInt10, myInt10);

        // Integer myInt11 = new Integer(19);
        // myTree.insertElement(myInt11, myInt11);

        // Integer myInt12 = new Integer(23);
        // myTree.insertElement(myInt12, myInt12);

        // Integer myInt13 = new Integer(24);
        // myTree.insertElement(myInt13, myInt13);

        // Integer myInt14 = new Integer(88);
        // myTree.insertElement(myInt14, myInt14);

        // Integer myInt15 = new Integer(1);
        // myTree.insertElement(myInt15, myInt15);

        // Integer myInt16 = new Integer(97);
        // myTree.insertElement(myInt16, myInt16);

        // Integer myInt17 = new Integer(94);
        // myTree.insertElement(myInt17, myInt17);

        // Integer myInt18 = new Integer(35);
        // myTree.insertElement(myInt18, myInt18);

        // Integer myInt19 = new Integer(51);
        // myTree.insertElement(myInt19, myInt19);

        // myTree.printAllElements();
        // System.out.println("done");

        // myTree = new TwoFourTree(myComp);
        // final int TEST_SIZE = 10000;

        // for (int i = 0; i < TEST_SIZE; i++) {
        //     myTree.insertElement(new Integer(i), new Integer(i));
        //     // myTree.printAllElements();
        //     // myTree.checkTree();
        // }

        // Insert and check tree with random numbers
        Random randNum = new Random();
        int elementArray[] = new int[TEST_SIZE];
        for (int i = 0; i < TEST_SIZE; i++) {
            elementArray[i] = randNum.nextInt(TEST_SIZE);
            myTree.insertElement(elementArray[i], elementArray[i]);
            // myTree.printAllElements();
            myTree.checkTree();
        }

        System.out.println("removing");
        for (int i = 0; i < TEST_SIZE; i++) {
            int out = (Integer) myTree.removeElement(i); // Changed from Integer()
            if (out != i) {
                throw new TwoFourTreeException("main: wrong element removed");
            }
            if (i > TEST_SIZE - 15) {
                myTree.printAllElements();
            }
            myTree.checkTree();
        }
        System.out.println("done");
    }

    public void printAllElements() {
        int indent = 0;
        if (root() == null) {
            System.out.println("The tree is empty");
        } else {
            printTree(root(), indent);
        }
    }

    public void printTree(TFNode start, int indent) {
        if (start == null) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
        printTFNode(start);
        indent += 4;
        int numChildren = start.getNumItems() + 1;
        for (int i = 0; i < numChildren; i++) {
            printTree(start.getChild(i), indent);
        }
    }

    public void printTFNode(TFNode node) {
        int numItems = node.getNumItems();
        for (int i = 0; i < numItems; i++) {
            System.out.print(((Item) node.getItem(i)).element() + " ");
        }
        System.out.println();
    }

    // Checks if tree is properly hooked up, i.e., children point to parents
    public void checkTree() {
        checkTreeFromNode(treeRoot);
    }

    private void checkTreeFromNode(TFNode start) {
        if (start == null) {
            return;
        }

        if (start.getParent() != null) {
            TFNode parent = start.getParent();
            int childIndex = 0;
            for (childIndex = 0; childIndex <= parent.getNumItems(); childIndex++) {
                if (parent.getChild(childIndex) == start) {
                    break;
                }
            }
            // if child wasn't found, print problem
            if (childIndex > parent.getNumItems()) {
                System.out.println("Child to parent confusion");
                printTFNode(start);
            }
        }

        if (start.getChild(0) != null) {
            for (int childIndex = 0; childIndex <= start.getNumItems(); childIndex++) {
                if (start.getChild(childIndex) == null) {
                    System.out.println("Mixed null and non-null children");
                    printTFNode(start);
                } else {
                    if (start.getChild(childIndex).getParent() != start) {
                        System.out.println("Parent to child confusion");
                        printTFNode(start);
                    }
                    for (int i = childIndex - 1; i >= 0; i--) {
                        if (start.getChild(i) == start.getChild(childIndex)) {
                            System.out.println("Duplicate children of node");
                            printTFNode(start);
                        }
                    }
                }

            }
        }

        int numChildren = start.getNumItems() + 1;
        for (int childIndex = 0; childIndex < numChildren; childIndex++) {
            checkTreeFromNode(start.getChild(childIndex));
        }
    }
}