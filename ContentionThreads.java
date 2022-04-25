import java.util.AbstractSet;
import java.util.Random;

public class ContentionThreads {
    //Time how long it takes to add an array to an abstract list
    public class timedAddArrayThread implements Runnable {
        int [] values;
        AbstractSet<Integer> list;
        private volatile long timeTaken;
        timedAddArrayThread(int [] values, AbstractSet<Integer> list) {
            this.values = values;
            this.list = list;
        }
        @Override
        public void run() {
            long time = System.nanoTime();
            for (int i = 0; i < values.length; i++) {
                list.add(values[i]);
            }
            timeTaken = System.nanoTime() - time;
        }

        public long getTimeTaken(){
            return timeTaken;
        }
    }

    //Time how long it takes to delete an array to an abstract list
    public class timedDeleteArrayThread implements Runnable {
        int [] values;
        AbstractSet<Integer> list;
        private volatile long timeTaken;
        timedDeleteArrayThread(int [] values, AbstractSet<Integer> list) {
            this.values = values;
            this.list = list;
        }
        @Override
        public void run() {
            long time = System.nanoTime();
            for (int i = 0; i < values.length; i++) {
                list.remove(values[i]);
            }
            timeTaken = System.nanoTime() - time;
        }

        public long getTimeTaken(){
            return timeTaken;
        }
    }

    //Time how long it takes to add an array to an abstract list
    public class timedFindArrayThread implements Runnable {
        int [] values;
        AbstractSet<Integer> list;
        private volatile long timeTaken;
        timedFindArrayThread(int [] values, AbstractSet<Integer> list) {
            this.values = values;
            this.list = list;
        }
        @Override
        public void run() {
            long time = System.nanoTime();
            for (int i = 0; i < values.length; i++) {
                list.contains(values[i]);
            }
            timeTaken = System.nanoTime() - time;
        }

        public long getTimeTaken(){
            return timeTaken;
        }
    }

    //Generate a array of int length size of random integers in a set of bounds
    public int [] generateRandomNumberArray(int length, int min, int max){
        int[] array = new int[length];
        Random rand = new Random();
        for(int i = 0; i<length; i++){
            array[i] = rand.nextInt(max) - min;
        }
        return array;
    }

    //Get average time of single insertion with various levels of contention
    public long contentionInsert(AbstractSet<Integer> list, int threadCount, int min, int max, int amount){
        Thread[] threads = new Thread[threadCount];
        timedAddArrayThread [] times = new timedAddArrayThread [threadCount];
        for(int i = 0; i<threadCount; i++){
            int[] randomSet = generateRandomNumberArray(amount, min, max);
            timedAddArrayThread testThread = new timedAddArrayThread(randomSet, list);
            threads[i] = new Thread(testThread);
            times[i] = testThread;
        }
        //Now start all threads
        for(int i = 0; i<threadCount; i++){
            threads[i].start();
        }
        //Wait for all threads to finish
        for(Thread thread : threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Get times
        long sumTime = 0;
        for(int i = 0; i<threadCount; i++){
            sumTime += (times[i].getTimeTaken()/amount);
        }
        return sumTime/threadCount;
    }

    public long contentionDelete(AbstractSet<Integer> list, int threadCount, int min, int max, int amount){
        Thread[] threads = new Thread[threadCount];
        Thread[] deleteThreads = new Thread[threadCount];
        timedDeleteArrayThread [] times = new timedDeleteArrayThread [threadCount];
        for(int i = 0; i<threadCount; i++){
            int[] randomSet = generateRandomNumberArray(amount, min, max);
            timedAddArrayThread testThread = new timedAddArrayThread(randomSet, list);
            timedDeleteArrayThread deleteThread = new timedDeleteArrayThread(randomSet, list);
            threads[i] = new Thread(testThread);
            deleteThreads[i] = new Thread(deleteThread);
            times[i] = deleteThread;
        }
        //Now start all add threads
        for(int i = 0; i<threadCount; i++){
            threads[i].start();
        }
        //Wait for all threads to finish
        for(Thread thread : threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Now start all delete threads
        for(int i = 0; i<threadCount; i++){
            deleteThreads[i].start();
        }
        //Wait for all threads to finish
        for(Thread thread : deleteThreads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Get times
        long sumTime = 0;
        for(int i = 0; i<threadCount; i++){
            sumTime += (times[i].getTimeTaken()/amount);
        }
        return sumTime/threadCount;
    }

    public long contentionFind(AbstractSet<Integer> list, int threadCount, int min, int max, int amount){
        Thread[] threads = new Thread[threadCount];
        Thread[] findThreads = new Thread[threadCount];
        timedFindArrayThread [] times = new timedFindArrayThread [threadCount];
        for(int i = 0; i<threadCount; i++){
            int[] randomSet = generateRandomNumberArray(amount, min, max);
            timedAddArrayThread testThread = new timedAddArrayThread(randomSet, list);
            timedFindArrayThread fineThread = new timedFindArrayThread(randomSet, list);
            threads[i] = new Thread(testThread);
            findThreads[i] = new Thread(fineThread);
            times[i] = fineThread;
        }
        //Now start all add threads
        for(int i = 0; i<threadCount; i++){
            threads[i].start();
        }
        //Wait for all threads to finish
        for(Thread thread : threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Now start all find threads
        for(int i = 0; i<threadCount; i++){
            findThreads[i].start();
        }
        //Wait for all threads to finish
        for(Thread thread : findThreads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Get times
        long sumTime = 0;
        for(int i = 0; i<threadCount; i++){
            sumTime += (times[i].getTimeTaken()/amount);
        }
        return sumTime/threadCount;
    }
}
