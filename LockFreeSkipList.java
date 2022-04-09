import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.atomic.*;

public class LockFreeSkipList extends AbstractSet<Integer>{
    class Node  {
        final int value;
        final AtomicMarkableReference<Node >[] next;
        private int topLevel;

        public Node(int value, int height) {
            this.value = value;
            next = (AtomicMarkableReference<Node>[]) new AtomicMarkableReference<?>[height + 1];
            for (int i = 0; i < next.length; i++) {
                next[i] = new AtomicMarkableReference<Node >(null, false);
            }
            topLevel = height;
        }
    }

    private int maxLevels; 
    final Node head;
    final Node tail;
    AtomicInteger size;
    public LockFreeSkipList(int maxLevels) {
        this.maxLevels = maxLevels;
        this.head = new Node(Integer.MIN_VALUE, maxLevels);
        this.tail = new Node(Integer.MAX_VALUE, maxLevels);
        this.size = new AtomicInteger(0);
        for (int i = 0; i < head.next.length; i++) {
            head.next[i] = new AtomicMarkableReference<Node>(tail, false);
        }
    }

    //Time Complexity: O(logn). Worst Case: O(n)
    @Override
    public boolean add(Integer x) {
        return insert(x);
    }

    //Time Complexity: O(logn). Worst Case: O(n)
    private boolean insert(int x) {
        int topLevel = chooseRandomLevel();//Flip a coin until "tails", each consecutive "head" is another level
        int bottomLevel = 0;

        Node[] predecessors = (Node []) new Node[this.maxLevels + 1];
        Node[] successors = (Node []) new Node[this.maxLevels + 1];
        
        while (true) {
            boolean found = find(x, predecessors, successors);
            if (found) {
                // Do not add the value as already present: end and return false
                return false;
            } else {
                // The new node to add
                Node newNode = new Node(x, topLevel);

                // For all the levels, update the 'next' array of the node
                for (int level = bottomLevel; level <= topLevel; level++) {
                    Node  succ = successors[level];
                    newNode.next[level].set(succ, false);
                }
                
                // Start at the lowest level predecessor and succesor
                Node pred = predecessors[bottomLevel];
                Node succ = successors[bottomLevel];

                // Set the next of the predecessor to the new node, if still the found succesor
                // Otherwise restart process
                if (!pred.next[bottomLevel].compareAndSet(succ, newNode, false, false)) {
                    continue;
                }
                for (int level = bottomLevel + 1; level <= topLevel; level++) {
                    // For each level, update the succesor of the previous node (until success of CAS)
                    while (true) {
                        pred = predecessors[level];
                        succ = successors[level];
                        if (pred.next[level].compareAndSet(succ, newNode, false, false))
                            break;
                        find(x, predecessors, successors);
                    }
                }
                size.incrementAndGet();
                return true;
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

        int bottomLevel = 0;
        Node[] predecessors = (Node []) new Node[this.maxLevels + 1];
        Node[] successors = (Node []) new Node[this.maxLevels + 1];
        Node succ;
        //Try to remove until succsessful
        while (true) {
            boolean found = find(value, predecessors, successors);
            if (!found) {
                return false;//If it is not in here, return false
            } else {
                Node nodeToRemove = successors[bottomLevel];
                for (int level = nodeToRemove.topLevel; level >= bottomLevel + 1; level--) {
                    boolean[] marked = { false };//Due to pair issues in AtomicMarkable Reference, marks are returned in array
                    succ = nodeToRemove.next[level].get(marked);
                    while (!marked[0]) {
                        nodeToRemove.next[level].compareAndSet(succ, succ, false, true);
                        succ = nodeToRemove.next[level].get(marked);
                    }
                }
                boolean[] marked = { false };
                succ = nodeToRemove.next[bottomLevel].get(marked);
                while (true) {
                    boolean iMarkedIt = nodeToRemove.next[bottomLevel].compareAndSet(succ, succ, false, true);
                    succ = successors[bottomLevel].next[bottomLevel].get(marked);
                    if (iMarkedIt) {
                        find(value, predecessors, successors);
                        return true;
                    } else if (marked[0])
                        return false;
                }
            }
        }
    }

    //Time Complexity: O(logn). Worst Case: O(n)
    public boolean find(Object value, Node[] predecessors, Node[] successors) {
        //If the key is not an integer, it cannot possibly be in this skiplist
        if (! (value instanceof Integer)) {
            return false;
        }

        int bottomLevel = 0;
        int key = (Integer) value;

        boolean[] marked = { false };
        boolean valid;

        Node  pred = null;
        Node curr = null;
        Node succ = null;
        //Until we get an uninterrupted read, try again
        retry: while (true) {
            pred = head;
            for (int level = this.maxLevels; level >= bottomLevel; level--) {
                curr = pred.next[level].getReference();
                while (true) {
                    succ = curr.next[level].get(marked);
                    while (marked[0]) { //While the successor is marked, try to update predeccesor's next
                        valid = pred.next[level].compareAndSet(curr, succ, false, false);
                        if (!valid)
                            continue retry;
                        curr = pred.next[level].getReference();
                        succ = curr.next[level].get(marked);
                    }
                    if (curr.value < key) {
                        pred = curr;
                        curr = succ;
                    } else {
                        break;
                    }
                }
                predecessors[level] = pred;
                successors[level] = curr;
            }
            return (curr.value == key);
        }
    }

    //Time Complexity: O(logn). Worst Case: O(n)
    @Override
    public boolean contains(Object value) {
        //If the key is not an integer, it cannot possibly be in this skiplist
        if (! (value instanceof Integer)) {
            return false;
        }

        int bottomLevel = 0;
        int v = (Integer) value;
        boolean[] marked = { false };
        Node  pred = head, curr = null, succ = null;
        for (int level = this.maxLevels; level >= bottomLevel; level--) {
            curr = pred.next[level].getReference(); // Replace with pred ?
            while (true) {
                succ = curr.next[level].get(marked);
                while (marked[0]) {
                    curr = pred.next[level].getReference();
                    succ = curr.next[level].get(marked);
                }
                if (curr.value < v) {
                    pred = curr;
                    curr = succ;
                } else {
                    break;
                }
            }
        }
        return (curr.value == v);
    }

    //Used for debugging purposes
    public String stringify() {
        String result = "{";

        int bottomLevel = 0;
        final int lastValue = Integer.MAX_VALUE;
        Node  pred = head, curr = null;

        for (int level = this.maxLevels; level >= bottomLevel; level--) {
            result += "\n    "+level+": ";
            curr = pred.next[level].getReference();
            while (curr.value < lastValue) {
                result += curr.value+" ";
                curr = curr.next[level].getReference();
            }
        }

        result += "\n}";

        return result;
    }

    //Decide how high the inserted node should go by flipping a coin over and over again
    private int chooseRandomLevel() {
        int newLevel = 0;
        int j;
        while (newLevel < this.maxLevels - 1){
            j = (int) (2*Math.random());//flip a coin
            if(j == 1){
                newLevel += 1;
            } else {
                break;
            }
        }
        return newLevel;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private Node current = head.next[0].getReference();

            @Override
            public boolean hasNext() {
                return current.value != Integer.MAX_VALUE;
            }

            @Override
            public Integer next() {
                Integer value = current.value;
                current = current.next[0].getReference();
                return value;
            }
        };
    }

    @Override
    public int size() {
        return size.get();
    }

    public String toString(){
        String output = "[";
        final int lastValue = Integer.MAX_VALUE;
        Node  pred = head, curr = null;

        curr = pred.next[0].getReference();
        while (curr.value < lastValue) {
            output += curr.value+", ";
            curr = curr.next[0].getReference();
        }
    
        output += "]";
        return output;
    }
}