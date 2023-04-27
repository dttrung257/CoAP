package test;

/*
* start(): init 1 thread
* run(): run thread
* setName(): set name for thread
* join(): join 2 thread
* setPriority(): set priority for thread
* interrupt(): stop stop halfway
* isAlive(): check if the thread is still running
 */
public class ThreadSample {
    private static class CustomerThread extends Thread {

        @Override
        public void run() {
            int count = 0;
            while (count++ < 5) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // System.out.format("Customer Thread %s is running\n", Thread.currentThread().getName());
                System.out.format("Customer Thread %s is running %d\n", this.getName(), count);
            }
        }
    }

    public static void main(String[] args) {
        CustomerThread customerThreadA = new CustomerThread();
        CustomerThread customerThreadB = new CustomerThread();
        customerThreadA.setName("A");
        customerThreadB.setName("B");
        customerThreadA.start();
        customerThreadB.start();
        System.out.println(customerThreadA.isAlive());
    }
}

