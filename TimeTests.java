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
        
        /*
        contentionInsertTest1("Fine Grained");
        contentionInsertTest1("Lock-Free");
        contentionInsertTest1("Java-Util");
        */
        //contentionInsertTest2();
        //contentionDeleteTest1();
        contentionFindTest2();

        /* 3 thread tests
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
        */
        
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

    public static void contentionInsertTest(String listType){
        AbstractSet<Integer> list = createList(listType);
        ContentionThreads threads = new ContentionThreads();
        for(int i = 1; i < 9; i*=2){
            long time = threads.contentionInsert(list, i, -100, 100, 100);
            System.out.println(listType + ": " + time);
            list = createList(listType);
        }
    }

    public static void contentionInsertTest1(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionInsert(list, i, -100, 100, 100);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionInsert(list, i, -100, 100, 100);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
    }

    public static void contentionInsertTest2(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionInsert(list, i, -100, 100, 1000);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionInsert(list, i, -100, 100, 1000);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
    }

    public static void contentionInsertTest3(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionInsert(list, i, -100, 100, 10000);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionInsert(list, i, -100, 100, 10000);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
    }

    public static void contentionDeleteTest1(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionDelete(list, i, -100, 100, 100);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionDelete(list, i, -100, 100, 100);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
    }

    public static void contentionDeleteTest2(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionDelete(list, i, -100, 100, 1000);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionDelete(list, i, -100, 100, 1000);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
    }

    public static void contentionDeleteTest3(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionDelete(list, i, -100, 100, 10000);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionDelete(list, i, -100, 100, 10000);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
    }

    public static void contentionFindTest1(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionFind(list, i, -100, 100, 100);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionFind(list, i, -100, 100, 100);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
    }

    public static void contentionFindTest2(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionFind(list, i, -100, 100, 1000);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionFind(list, i, -100, 100, 1000);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
    }

    public static void contentionFindTest3(){
        ContentionThreads threads = new ContentionThreads();

        long [] LockFreeTimes = new long[4];
        AbstractSet<Integer> list = createList("Lock-Free");
        int j = 0;
        for(int i = 1; i < 9; i*=2){
            LockFreeTimes[j] = threads.contentionFind(list, i, -100, 100, 10000);
            j++;
        }

        long [] FineGrainedTimes = new long[4];
        list = createList("Fine Grained");
        j = 0;
        for(int i = 1; i < 9; i*=2){
            FineGrainedTimes[j] = threads.contentionFind(list, i, -100, 100, 10000);
            j++;
        }

        j = 0;
        for(int i = 1; i < 9; i*=2){
            long margin = (LockFreeTimes[j] - FineGrainedTimes[j])/(LockFreeTimes[j]/100);
            System.out.println("LockFreeOverFG for " + i + " threads:" + margin);
            j++;
        }
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
