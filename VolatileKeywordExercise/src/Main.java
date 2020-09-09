import java.util.Random;

/*
Some notes from the course:
Which operations are atomic?
- Assignments to reference and primitives (for example, a=b. This means that most of the getters and setters are atomic,
long and doubles are exceptions)
- All assignments to primitive types are safe except long and double (this means reading and writing to int, short. byte,
float, char, boolean are without the need to synchronize). Long and double are 64 bits long, java does not guarantee
the upper 32 bits and the lower 32 bits can be completed in one single operation by the CPU.
 */

/*
Declare a long or double variable with Volatile keyword, then read from and write to those variables become atomic
and thread safe. THey are guaranteed to be performed in one single operation in hardware.
 */

public class Main {
    public static void main(String[] args) {
        Metrics metrics = new Metrics();

        BusinessLogic businessLogicThread1 = new BusinessLogic(metrics);

        BusinessLogic businessLogicThread2 = new BusinessLogic(metrics);

        MetricsPrinter metricsPrinter = new MetricsPrinter(metrics);

        businessLogicThread1.start();
        businessLogicThread2.start();
        metricsPrinter.start();
    }

    //This MetricsPrinter class will run in parallel to the BusinessLogic class,
    //capture the average time the business is taking and print it to the screen.
    public static class MetricsPrinter extends Thread {
        private Metrics metrics;

        public MetricsPrinter(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void run() {
            //repeat many times
            while (true) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
                //The getAverage() method is not synchronized, so the MetricsPrinter will not slow down the businessLogic thread.
                //These two threads can be run in 100% parallel.
                double currentAverage = metrics.getAverage();

                System.out.println("Current Average is " + currentAverage);
            }
        }
    }

    public static class BusinessLogic extends Thread {
        private Metrics metrics;
        private Random random = new Random();

        public BusinessLogic(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void run() {
            //repeat many times
            while (true) {
                long start = System.currentTimeMillis();

                try {
                    //set the sleep time to be a random between 0 to 10, so when the program starts to run,
                    //the average will be close to 5 little by little
                    Thread.sleep(random.nextInt(10));
                } catch (InterruptedException e) {
                }

                long end = System.currentTimeMillis();

                metrics.addSample(end - start);
            }
        }
    }

    public static class Metrics {
        private long count = 0;
        //use volatile keyword to make getAverage() atomic
        private volatile double average = 0.0;

        //add the synchronized keyword to make addSample() atomic
        public synchronized void addSample(long sample) {
            double currentSum = average * count;
            count++;
            average = (currentSum + sample) / count;
        }

        //no need to use synchronized keyword since i already used the volatile keyword on average variable declaration
        public double getAverage() {
            return average;
        }
    }

}
