import java.util.AbstractSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class TimeTests {
    public static void main(String[] args) {
        //singleThreadInsertTimingTestFineGrained(100);
        //singleThreadInsertTimingTestLockFree(100);
        //singleThreadContainsTimingTest1("Fine Grained");
        //singleThreadContainsTimingTest1("Lock-Free");
        //singleThreadContainsTimingTest2("Fine Grained");
        //singleThreadContainsTimingTest2("Lock-Free");
        
        multiThreadInsertTimingTest1("Fine Grained");
        multiThreadInsertTimingTest1("Lock-Free");
        multiThreadInsertTimingTest1("Java-Util");
        multiThreadInsertTimingTest2("Fine Grained");
        multiThreadInsertTimingTest2("Lock-Free");
        multiThreadInsertTimingTest2("Java-Util");
        multiThreadRemoveTimingTest1("Fine Grained");
        multiThreadRemoveTimingTest1("Lock-Free");
        multiThreadRemoveTimingTest1("Java-Util");
        multiThreadRemoveTimingTest2("Fine Grained");
        multiThreadRemoveTimingTest2("Lock-Free");
        multiThreadRemoveTimingTest2("Java-Util");
        multiThreadContainsTimingTest1("Fine Grained");
        multiThreadContainsTimingTest1("Lock-Free");
        multiThreadContainsTimingTest1("Java-Util");
        
    }

    public static AbstractSet<Integer> createList(String listType){
        AbstractSet<Integer> list;
        if(listType.equals("Fine Grained")){
            list = new FineGrainedSkipList(20); 
        } else if(listType.equals("Lock-Free")) {
            list = new LockFreeSkipList(20); //TODO: deadlock issues
        } else {
            list = new ConcurrentSkipListSet<Integer>();
        }
        return list;
    }

    //Tests a single contains method time
    public static void singleThreadContainsTimingTest1(String listType){
        AbstractSet<Integer> list = createList(listType);
        //first add in values
        TestThreads test = new TestThreads();
        test.makeAddBetweenNoRepeatsThread(list);
        //now time contains
        long time = System.nanoTime();
        list.contains(150);
        System.out.println("Single Thread Contains Timing Test 1 of a " + listType + " skiplist took " + (System.nanoTime() - time) + " nanoseconds");
    }

    //Tests a single contains method time
    public static void singleThreadContainsTimingTest2(String listType){
        AbstractSet<Integer> list = createList(listType);
        //first add in values
        TestThreads test = new TestThreads();
        test.makeAddBetweenWithRepeatsThread(list);
        //now time contains
        long time = System.nanoTime();
        list.contains(150);
        System.out.println("Single Thread Contains Timing Test 2 of a " + listType + " skiplist took " + (System.nanoTime() - time) + " nanoseconds");
    }

    //Tests to see if the list can sucsessfully take multiple threads inserting at once, no repeats in values
    public static void multiThreadInsertTimingTest1(String listType){
        AbstractSet<Integer> list = createList(listType);
        TestThreads test = new TestThreads();
        long time = System.nanoTime();
        test.makeAddBetweenNoRepeatsThread(list);
        System.out.println("Multi Thread Insertion Timing Test 1 of a " + listType + " skiplist took " + (System.nanoTime() - time) + " nanoseconds");
    }

    //Tests to see if the list can sucsessfully take multiple threads inserting at once, with repeats in values
    public static void multiThreadInsertTimingTest2(String listType){
        AbstractSet<Integer> list = createList(listType);
        TestThreads test = new TestThreads();
        long time = System.nanoTime();
        test.makeAddBetweenWithRepeatsThread(list);
        System.out.println("Multi Thread Insertion Timing Test 2 of a " + listType + " skiplist took " + (System.nanoTime() - time) + " nanoseconds");
    }

    //Tests to see if the list can sucsessfully take multiple threads removing at once, no repeats in values
    public static void multiThreadRemoveTimingTest1(String listType){
        AbstractSet<Integer> list = createList(listType);
        //first add in values
        TestThreads test = new TestThreads();
        test.makeAddBetweenNoRepeatsThread(list);
        //now time removal
        long time = System.nanoTime();
        test.makeRemoveBetweenNoRepeatsThread(list);
        System.out.println("Multi Thread Removal Timing Test 1 of a " + listType + " skiplist took " + (System.nanoTime() - time) + " nanoseconds");
    }

    //Tests to see if the list can sucsessfully take multiple threads removing at once, with repeats in values
    public static void multiThreadRemoveTimingTest2(String listType){
        AbstractSet<Integer> list = createList(listType);
        //first add in values
        TestThreads test = new TestThreads();
        test.makeAddBetweenWithRepeatsThread(list);
        //now time removal
        long time = System.nanoTime();
        test.makeRemoveBetweenWithRepeatsThread(list);
        System.out.println("Multi Thread Removal Timing Test 2 of a " + listType + " skiplist took " + (System.nanoTime() - time) + " nanoseconds");
    }

    //Tests to see if the list can sucsessfully take multiple threads running contains at once, no repeats in values
    public static void multiThreadContainsTimingTest1(String listType){
        AbstractSet<Integer> list = createList(listType);
        //first add in values
        TestThreads test = new TestThreads();
        test.makeAddBetweenWithRepeatsThread(list);
        //now time contains
        long time = System.nanoTime();
        test.makeContainsBetweenWithRepeatsThread(list);
        System.out.println("Multi Thread Contains Timing Test 1 of a " + listType + " skiplist took " + (System.nanoTime() - time) + " nanoseconds");
    }
}
