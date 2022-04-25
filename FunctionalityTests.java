import java.util.AbstractSet;
public class FunctionalityTests {
    public static void main(String[] args) {
        
        //multiThreadInsertTest1("Fine Grained");
        //singleThreadInsertTestLockFree(15);
        //multiThreadInsertTest1("Lock-Free");
        multiThreadInsertTest2("Fine Grained");
        multiThreadInsertTest2("Lock-Free");
        //multiThreadRemoveTest1("Fine Grained");
        //multiThreadRemoveTest1("Lock-Free");
    }

    //Tests to see if the list can take multiple threads inserting at once, no repeats in values
    public static void multiThreadInsertTest1(String listType){
        AbstractSet<Integer> list;
        if(listType.equals("Fine Grained")){
            list = new FineGrainedSkipList(10); 
            System.out.println("Fine Grained Skiplist created");
        } else {
            list = new LockFreeSkipList(10); 
        }
        TestThreads test = new TestThreads();
        test.makeAddBetweenNoRepeatsThread(list);
        System.out.println(list.toString());
    }

    //Tests to see if the list can take multiple threads inserting at once, with repeats in values
    public static void multiThreadInsertTest2(String listType){
        AbstractSet<Integer> list;
        if(listType.equals("Fine Grained")){
            list = new FineGrainedSkipList(10); 
            System.out.println("Fine Grained Skiplist created");
        } else {
            list = new LockFreeSkipList(10);
            System.out.println("Lock Free Skiplist created");
        }
        TestThreads test = new TestThreads();
        test.makeAddBetweenWithRepeatsThread(list);
        System.out.println(list.toString());
    }

    //Tests to see if the list can take multiple threads removing at once, no repeats in values
    public static void multiThreadRemoveTest1(String listType){
        AbstractSet<Integer> list;
        if(listType.equals("Fine Grained")){
            list = new FineGrainedSkipList(10); 
            System.out.println("Fine Grained Skiplist created");
        } else {
            list = new LockFreeSkipList(10);
        }
        TestThreads test = new TestThreads();
        test.makeAddBetweenNoRepeatsThread(list);
        System.out.println(list.toString());
        System.out.println("Now running remove");
        test.makeRemoveBetweenNoRepeatsThread(list);
        System.out.println(list.toString());
    }
}
