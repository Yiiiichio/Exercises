import org.omg.PortableServer.THREAD_POLICY_ID;

import java.math.BigInteger;

public class Main2 {

    public static void main(String[] args) {

        //when the given base and power is too large, it's gonna take a long time to calculate
        Thread thread = new Thread(new LongComputationTask(new BigInteger("2"), new BigInteger("100")));

        thread.start();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        thread.interrupt();
    }

    private static class LongComputationTask implements Runnable {
        private BigInteger base;
        private BigInteger power;

        public LongComputationTask(BigInteger base, BigInteger power) {
            this.base = base;
            this.power = power;
        }

        @Override
        public void run() {
            System.out.println(base + "^" + power + " = " + pow(base, power));
        }

        private BigInteger pow(BigInteger base, BigInteger power) {
            BigInteger result = BigInteger.ONE;

            for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Prematurely interrupted computation");
                    return result;
                }
                result = result.multiply(base);
            }

            return result;
        }
    }
}
