import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //StandardStack<Integer> stack = new StandardStack<>();
        LockFreeStack<Integer> stack = new LockFreeStack<>();
        Random random = new Random();

        for (int i = 0; i < 100000; i++) {
            stack.push(random.nextInt());
        }

        List<Thread> threads = new ArrayList<>();

        int pushingThreads = 2;
        int poppingThreads = 2;

        //the pushing threads run a loop forever to push random integers into the stack
        for (int i = 0; i < pushingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.push(random.nextInt());
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        //the popping threads run a loop forever to pop random integer into the stack
        for (int i = 0; i < poppingThreads; i++) {
            Thread thread = new Thread(() -> {
                while (true) {
                    stack.pop();
                }
            });

            thread.setDaemon(true);
            threads.add(thread);
        }

        //start all the threads
        for (Thread thread : threads) {
            thread.start();
        }

        //let popping and pushing threads run for 10 seconds
        Thread.sleep(10000);

        //after 10 seconds, print out how many operations on the stack those threads were able to perform in 10 seconds
        System.out.println(String.format("%,d operations were performed in 10 seconds ", stack.getCounter()));
    }

    public static class LockFreeStack<T> {
        private AtomicReference<StackNode<T>> head = new AtomicReference<>();
        private AtomicInteger counter = new AtomicInteger(0);

        public void push(T value) {
            StackNode<T> newHeadNode = new StackNode<>(value);

            while (true) {
                StackNode<T> currentHeadNode = head.get();
                newHeadNode.next = currentHeadNode;
                if (head.compareAndSet(currentHeadNode, newHeadNode)) {
                    //the compareAndSet succeeds, that means the head was not modified by any other thread
                    //between reading the head and writing to the head
                    break;
                } else {
                    //the compareAndSet return false, that means between reading from the head and trying to update the head
                    //with the new head node, the head has changed.
                    //so need to do all those operations again
                    //wait for one nanosecond
                    LockSupport.parkNanos(1);
                }
            }
            counter.incrementAndGet();
        }

        public T pop() {
            StackNode<T> currentHeadNode = head.get();
            StackNode<T> newHeadNode;

            //if currentHeadNode is null, it means the stack is empty, we got nothing to pop out
            while (currentHeadNode != null) {
                newHeadNode = currentHeadNode.next;
                if (head.compareAndSet(currentHeadNode, newHeadNode)) {
                    break;
                } else {
                    //THe compareAndSet fails which means other thread changed the head. need to do all over again.
                    LockSupport.parkNanos(1);
                    currentHeadNode = head.get();
                }
            }
            counter.incrementAndGet();
            return currentHeadNode != null ? currentHeadNode.value : null;
        }

        public int getCounter() {
            return counter.get();
        }
    }

    public static class StandardStack<T> {
        private StackNode<T> head; //reference to the head
        private int counter = 0;

        //use synchronized keyword to make this function atomic, to avoid race conditions
        public synchronized void push(T value) {
            StackNode<T> newHead = new StackNode<>(value);
            newHead.next = head;
            head = newHead;
            counter++;
        }

        //use synchronized keyword to make this function atomic, to avoid race conditions
        public synchronized T pop() {
            if (head == null) {
                counter++;
                return null;
            }

            T value = head.value;
            head = head.next;
            counter++;
            return value;
        }

        public int getCounter() {
            return counter;
        }
    }

    //Generic linked list stack
    public static class StackNode<T> {
        public T value;
        public StackNode<T> next; //reference to the next node

        public StackNode(T value) {
            this.value = value;
            this.next = next;
        }
    }
}
