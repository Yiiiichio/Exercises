//The entire app is waiting for the thread to finish,
//even the main thread is already long gone

public class Main1 {
    public static void main(String [] args) {
        //another method to create a thread
        Thread thread = new Thread(new BlockingTask());

        thread.start();

        //this interrupt() function will throw an interrupted exception to that thread
        //thread.interrupt();
    }

    private static class BlockingTask implements Runnable {

        @Override
        public void run() {
            //do things
            try {
                Thread.sleep(500000);
            } catch (InterruptedException e) {
                System.out.println("Existing blocking thread");
            }
        }
    }
}