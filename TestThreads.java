import java.util.AbstractSet;

public class TestThreads {
    //Define threads for multi threaded timing tests
    //add array thread
    public class addArrayThread implements Runnable {
        int [] values;
        FineGrainedSkipList list;
        addArrayThread(int [] values, FineGrainedSkipList list) {
            this.values = values;
            this.list = list;
        }
        @Override
        public void run() {
            for (int i = 0; i <= values.length; i++) {
                list.add(values[i]);
            }
        }
    }

    //add all values between thread
    public class addBetweenThread implements Runnable {
        int begin;
        int end;
        AbstractSet<Integer> list;
        addBetweenThread(int begin, int end, AbstractSet<Integer> list) {
            this.begin = begin;
            this.end = end;
            this.list = list;
        }
        @Override
        public void run() {
            for (int i = begin; i <= end; i++) {
                list.add(i);
            }
        }
    }

    //remove all values between thread
    public class removeBetweenThread implements Runnable {
        int begin;
        int end;
        AbstractSet<Integer> list;
        removeBetweenThread(int begin, int end, AbstractSet<Integer> list) {
            this.begin = begin;
            this.end = end;
            this.list = list;
        }
        @Override
        public void run() {
            for (int i = begin; i <= end; i++) {
                list.remove(i);
            }
        }
    }

    //contains all values between thread
    public class containBetweenThread implements Runnable {
        int begin;
        int end;
        AbstractSet<Integer> list;
        containBetweenThread(int begin, int end, AbstractSet<Integer> list) {
            this.begin = begin;
            this.end = end;
            this.list = list;
        }
        @Override
        public void run() {
            for (int i = begin; i <= end; i++) {
                list.contains(i);
            }
        }
    }


    //Make conc. threads to add values
    public void makeAddBetweenNoRepeatsThread(AbstractSet<Integer> list) {
        Thread[] threads = new Thread[3];
        threads[0] = new Thread(new addBetweenThread(100, 200, list));
        threads[1] = new Thread(new addBetweenThread(201, 300, list));
        threads[2] = new Thread(new addBetweenThread(6, 7, list));
        threads[1].start(); 
        threads[0].start(); 
        threads[2].start();

        //join all threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Make conc. threads to add values that also have repeats
    public void makeAddBetweenWithRepeatsThread(AbstractSet<Integer> list) {
        Thread[] threads = new Thread[3];
        threads[0] = new Thread(new addBetweenThread(100, 200, list));
        threads[1] = new Thread(new addBetweenThread(150, 250, list));
        threads[2] = new Thread(new addBetweenThread(50, 150, list));
        threads[1].start(); 
        threads[0].start(); 
        threads[2].start();

        //join all threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //add all values between thread
    public class addBetweenThread2 implements Runnable {
        int begin;
        int end;
        LockFreeSkipList list;
        addBetweenThread2(int begin, int end, LockFreeSkipList list) {
            this.begin = begin;
            this.end = end;
            this.list = list;
        }
        @Override
        public void run() {
            for (int i = begin; i <= end; i++) {
                list.add(i);
            }
        }
    }

    //Make conc. threads to add values that also have repeats
    public void makeAddBetweenWithRepeatsThread2(LockFreeSkipList list) {
        Thread[] threads = new Thread[3];
        threads[0] = new Thread(new addBetweenThread2(100, 200, list));
        threads[1] = new Thread(new addBetweenThread2(150, 250, list));
        threads[2] = new Thread(new addBetweenThread2(50, 150, list));
        threads[1].start(); 
        threads[0].start(); 
        threads[2].start();

        //join all threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Make conc. threads to remove values
    public void makeRemoveBetweenNoRepeatsThread(AbstractSet<Integer> list) {
        Thread[] threads = new Thread[3];
        threads[0] = new Thread(new removeBetweenThread(100, 200, list));
        threads[1] = new Thread(new removeBetweenThread(201, 300, list));
        threads[2] = new Thread(new removeBetweenThread(6, 7, list));
        threads[1].start(); 
        threads[0].start(); 
        threads[2].start();

        //join all threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Make conc. threads to remove values that also have repeats
    public void makeRemoveBetweenWithRepeatsThread(AbstractSet<Integer> list) {
        Thread[] threads = new Thread[3];
        threads[0] = new Thread(new removeBetweenThread(100, 200, list));
        threads[1] = new Thread(new removeBetweenThread(150, 250, list));
        threads[2] = new Thread(new removeBetweenThread(50, 150, list));
        threads[1].start(); 
        threads[0].start(); 
        threads[2].start();

        //join all threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //Make conc. threads to find values, this will be run on a list that has repeats of values
    public void makeContainsBetweenWithRepeatsThread(AbstractSet<Integer> list) {
        Thread[] threads = new Thread[3];
        threads[0] = new Thread(new containBetweenThread(100, 200, list));
        threads[1] = new Thread(new containBetweenThread(150, 250, list));
        threads[2] = new Thread(new containBetweenThread(50, 150, list));
        threads[1].start(); 
        threads[0].start(); 
        threads[2].start();

        //join all threads
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
