import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

//Referenced: http://www.cs.tau.ac.il/~shanir/nir-pubs-web/Papers/OPODIS2006-BA.pdf

public class FineGrainedSkipList extends AbstractSet<Integer> {
    class Node {
        int value;//Value of the node, Also functions as Index
        int level;//Level that the node lives at
        Node[] next;//List of items to the right of the array at different levels

        ReentrantLock lock;//Locks the node
        boolean fullyLinked;//Are the pointers correctly set?
        boolean markedForRemoval;//Is this node being deleted?

        public Node(int value, int level, int maxLevel) {
            this.value = value;
            this.level = level;
            this.next = new Node[maxLevel];//List of pointers to the next node at any given level
            this.fullyLinked = true;
            this.markedForRemoval = false;
            this.lock = new ReentrantLock();
        }

        public String toString() {
            return Integer.toString(value);
        }
    }

    final Node header;
    AtomicInteger currentLevels;
    final int maxLevel;
    AtomicInteger size;

    //Time Complexity: O(l)
    public FineGrainedSkipList(int maxLevel) {
        this.currentLevels = new AtomicInteger(0);
        this.size = new AtomicInteger(0);
        this.maxLevel = maxLevel;
        header = new Node(Integer.MAX_VALUE, 0, maxLevel);
        for (int i = 0; i < maxLevel; i += 1) {
            header.next[i] = header;
        }
    }

    //Time Complexity: O(logn). Worst Case: O(n)
    @Override
    public boolean add(Integer value) {
        return insert(value);
    }

    //Time Complexity: O(logn). Worst Case: O(n)
    private boolean insert(int value) {
        //We need to save all predecessors and successors during the search function for insertion later
        Node[] predecessors = new Node[maxLevel];
        Node[] successors = new Node[maxLevel];

        while (true) {
            //Lets see if it already exists
            int foundNodeLevel = find(value, predecessors, successors);
            
            //If the node is found, see if it is being deleted 
            if (foundNodeLevel != -1) {
                Node foundNode = successors[foundNodeLevel];
                //If the node is not being deleted, check if it is fully linked before returning false;
                if (!foundNode.markedForRemoval) {
                    while (!foundNode.fullyLinked); //Wait for node to not be linked
                    return false;
                }
            }

            int levels = currentLevels.get();
            int newLevel = chooseRandomLevel(); //Flip a coin until "tails", each consecutive "head" is another level

            //If we need to make a new level, increment currentLevels
            //Note: even if newLevel is way higher than our currentLevel, this implementation only adds a singular new level
            if (newLevel > levels) {
                newLevel = currentLevels.incrementAndGet();
                levels = newLevel;
            }

            //We use this value to make unlocking easier
            int highestLockedLevel = -1;//What is the highest level of predecessor we have locked?

            //Attempt to add the value
            try {
                boolean valid = true;//Is this a valid place to put the new value?
                Node predecessor;
                Node successor;
                Node previousPredecessor = null;

                //Find A
                for (int level = 0; (valid && (level <= newLevel)); level += 1) {
                    predecessor = predecessors[level];//Node to make point to our new node
                    successor = successors[level];//Node to make our new node point to

                    if (predecessor != previousPredecessor) {
                        predecessor.lock.lock();//lock the predecessor node, we need to set its forward[level] to our node
                        highestLockedLevel = level;//How far up to we need to go when we unlock
                        previousPredecessor = predecessor;//Ensures we don't relock the same the node
                    }

                    //If the predecessor or successor is marked for removal, we cannot go ahead with insertion
                    valid = !predecessor.markedForRemoval
                            && !successor.markedForRemoval
                            && predecessor.next[level] == successor;
                }

                //If it is not a valid spot, try again
                if (!valid) {
                    continue;
                }

                //Create a new node with searchKey and value at newLevel
                Node newNode = new Node(value, newLevel, maxLevel);
                
                //Set newNodes forward and predeceessor's forward
                for (int level = 0; level <= newLevel; level += 1) {
                    newNode.next[level] = successors[level];
                    predecessors[level].next[level] = newNode;
                }

                newNode.fullyLinked = true;//Set new node to fullyLinked
                size.incrementAndGet();//Increment size of the skiplist

                return true;//upon succsessful insertion, return true
            }
            finally {
                //Unlock all threads that were locked by the insertion.
                for (int level = 0; level <= highestLockedLevel; level += 1) {
                    if (predecessors[level].lock.isHeldByCurrentThread()) {
                        predecessors[level].lock.unlock();
                    }
                }
            }
        }
    }

    //Time Complexity: O(logn). Worst Case: O(n)
    @Override
    public boolean remove(Object value) {
        //If the key is not an integer, it cannot be in the skiplist
        if (! (value instanceof Integer)) {
            return false;
        }

        Node[] predecessors = new Node[maxLevel];
        Node[] successors = new Node[maxLevel];
        Node nodeToRemove = null;
        boolean inProcessOfRemoving = false;
        int highestLevelFound = -1;

        while (true) {
            int foundNodeLevel = find(value, predecessors, successors);//Find the node and get predecessors and successors

            //Can we delete or are we in the process of deleting?
            if (inProcessOfRemoving || (foundNodeLevel != -1 && canDelete(successors[foundNodeLevel], foundNodeLevel))) {
                //If this is the first time we are looping through, lock the node
                if (!inProcessOfRemoving) {
                    nodeToRemove = successors[foundNodeLevel];//Get the node
                    highestLevelFound = nodeToRemove.level;
                    nodeToRemove.lock.lock();//Lock the node

                    //If the node is already being deleted, unlock and return false
                    if (nodeToRemove.markedForRemoval) {
                        nodeToRemove.lock.unlock();
                        return false;
                    }

                    //Mark the node for removal and set inProcessOfRemovingFlag
                    nodeToRemove.markedForRemoval = true;
                    inProcessOfRemoving = true;
                }

                int highestLockedLevel = -1;//For unlocking faster

                try {
                    boolean valid = true;
                    Node predecessor;
                    Node successor;
                    Node previousPredecessor = null;//Prevent locking the same predecessor twice

                    //lock all predeccesors
                    for (int level = 0; (valid && (level <= highestLevelFound)); level += 1) {
                        predecessor = predecessors[level];
                        successor = successors[level];

                        if (predecessor != previousPredecessor) {
                            predecessor.lock.lock();
                            highestLockedLevel = level;
                            previousPredecessor = predecessor;
                        }

                        //Make sure this is a valid operation, 
                        //i.e. predecessor is not being deleted and predecessor points to us.
                        valid = !predecessor.markedForRemoval
                                && predecessor.next[level] == successor;
                    }

                    //If we have invalid predecessors, try again
                    if (!valid) {
                        continue;
                    }

                    //Set predecessor's forwards to our node's forwards
                    for (int level = highestLevelFound; level >= 0; level -= 1) {
                        predecessors[level].next[level] = nodeToRemove.next[level];
                    }

                    nodeToRemove.lock.unlock();//Unlock our node, it is no longer references
                    size.decrementAndGet();//Decrement size of skiplist

                    return true;
                }
                finally {
                    for (int level = 0; level <= highestLockedLevel; level += 1) {
                        if (predecessors[level].lock.isHeldByCurrentThread()) {
                            predecessors[level].lock.unlock();
                        }
                    }
                }
            }
            else {
                return false;
            }
        }
    }

    /**
     * Determines if node was found at the highest level, not marked for removal, and fully linked.
     * Time Complexity: O(1)
     */
    public boolean canDelete(Node node, int highestLevelFound) {
        return !node.markedForRemoval
                && node.fullyLinked
                && node.level == highestLevelFound;
    }

    /**
     * Determines if the node is in the skiplist
     * Time Complexity: O(logn). Worst Case: O(n)
     */
    @Override
    public boolean contains(Object value) {
        //If the key is not an integer, it cannot possibly be in this skiplist
        if (! (value instanceof Integer)) {
            return false;
        }

        int highestLevel = -1;
        int searchKey = (Integer) value;
        Node predecessor = this.header; //Set first value to sentinel
        Node current;

        //Go down levels, looking for key, return highest level the node reaches.
        //If item to the right is > than key, go down
        //If item is not found, return 
        for (int level = maxLevel - 1; level >= 0; level -= 1) {
            current = predecessor.next[level];//Look right

            //While item to the right is not less than search key, go to the right
            while (current.value < searchKey) {
                predecessor = current;
                current = predecessor.next[level];
            }

            //If we found it for the first time, set highestLevel to level
            if (highestLevel == -1 && current.value == searchKey) {
                highestLevel = level;
                break;
            }
        }

        if(highestLevel != -1){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Finds "value" in the skiplist, returning highest level of occurrence or -1 if absent.
     * Time Complexity: O(logn). Worst Case: O(n)
     */
    public int find(Object value, Node[] predecessors, Node[] successors) {
        //If the key is not an integer, it cannot possibly be in this skiplist
        if (! (value instanceof Integer)) {
            return -1;
        }

        int highestLevel = -1;
        int searchKey = (Integer) value;
        Node predecessor = this.header; //Set first value to sentinel
        Node current;

        //Go down levels, looking for key, return highest level the node reaches.
        //If item to the right is > than key, go down
        //If item is not found, return 
        for (int level = maxLevel - 1; level >= 0; level -= 1) {
            current = predecessor.next[level];//Look right

            //While item to the right is not less than search key, go to the right
            while (current.value < searchKey) {
                predecessor = current;
                current = predecessor.next[level];
            }

            //If we found it for the first time, set highestLevel to level
            if (highestLevel == -1 && current.value == searchKey) {
                highestLevel = level;
            }

            //Save these for insertion
            predecessors[level] = predecessor;
            successors[level] = current;
        }

        //Return the level found
        return highestLevel;
    }

    //Decide how high the inserted node should go by flipping a coin over and over again
    private int chooseRandomLevel() {
        int newLevel = 0;
        int j;
        while (newLevel < maxLevel - 1){
            j = (int) (2*Math.random());//flip a coin
            if(j == 1){
                newLevel += 1;
            } else {
                break;
            }
        }
        return newLevel;
    }

    public int size() {
        return size.get();
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private Node current = header.next[0];

            @Override
            public boolean hasNext() {
                return current.value != Integer.MAX_VALUE;
            }

            @Override
            public Integer next() {
                Integer value = current.value;
                current = current.next[0];
                return value;
            }
        };
    }

    //Used for debugging purposes
    public String stringify() {
        String result = "{";

        int bottomLevel = 0;
        final int lastValue = Integer.MAX_VALUE;
        Node pred = header, curr = null;

        for (int level = this.maxLevel; level >= bottomLevel; level--) {
            result += "\n    "+level+": ";
            curr = pred.next[level];
            while (curr.value < lastValue) {
                result += curr.value+" ";
                curr = curr.next[level];
            }
        }

        result += "\n}";

        return result;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        for (Integer i : this) {
            s.append(i).append(", ");
        }
        s.append(']');
        return s.toString();
    }
}
