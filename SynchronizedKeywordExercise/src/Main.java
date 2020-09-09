public class Main {
    public static void main(String[] args) throws InterruptedException {
        InventoryCounter inventoryCounter = new InventoryCounter();
        IncrementingThread incrementingThread = new IncrementingThread(inventoryCounter);
        DecrementingThread decrementingThread = new DecrementingThread(inventoryCounter);

        incrementingThread.start();
        decrementingThread.start();

        incrementingThread.join();
        decrementingThread.join();

        System.out.println("We currently have " + inventoryCounter.getItems() + " items");
    }

    public static class DecrementingThread extends Thread {

        private InventoryCounter inventoryCounter;

        public DecrementingThread(InventoryCounter inventoryCounter) {
            this.inventoryCounter = inventoryCounter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10000; i++) {
                inventoryCounter.decrement();
            }
        }
    }

    public static class IncrementingThread extends Thread {

        private InventoryCounter inventoryCounter;

        public IncrementingThread(InventoryCounter inventoryCounter) {
            this.inventoryCounter = inventoryCounter;
        }

        @Override
        public void run() {
            for (int i = 0; i < 10000; i++) {
                inventoryCounter.increment();
            }
        }
    }



    //First way to use synchronized keyword. (it's called monitor)

    //My note***IMPORTANT*** When use synchronized keyword on methodA and methodB in an object, if thread1 is using methodA, thread2 can use neither
    //methodA nor methodB. Because synchronized is applied per object.

//        private static class InventoryCounter {
//            private int items = 0;
//
//            Object lock = new Object();
//
//            public synchronized void increment() {
//                    items++;
//            }
//
//            public synchronized void decrement() {
//                    items--;
//            }
//
//            public int getItems() {
//                    return items;
//            }
//        }




    //Second way to use synchronized keyword:
    //define a block of code i consider as critical section, without making the entire method synchronized
    private static class InventoryCounter {
        private int items = 0;

        //create an object to synchronize on which will serve as a lock
        //Any synchronized block synchronized on the same object will allow only one thread to execute inside that block.
        Object lock = new Object();

        //i can have different synchronized blocks, and use them for different synchronized blocks
        //this way of using synchronized keyword provides a lot more flexibility

//        Object lock1 = new Object();
//        Object lock2 = new Object();

        public void increment() {
            synchronized (this.lock) {
                items++;
            }
        }

        //Add synchronized keyword on method(first method used above) is equivalent to this below:

//        public void increment() {
//            synchronized (this) {
//            items++;
//            }
//        }


        public void decrement() {
            synchronized (this.lock) {
                items--;
            }
        }

        //Add synchronized keyword on method is equivalent to this below:

//        public void decrement() {
//            synchronized (this) {
//                items--;
//            }
//        }

        public int getItems() {
                return items;
        }
    }

//One last thing to remember:
//Synchronized block is Reentrant
//If threadA is accessing a synchronized method while already being in a different synchronized method or block,
//it will be able to access that synchronized method with no problem

}