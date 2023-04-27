package test;

public class Interruption {
    private static class BankingThread extends Thread {

        @Override
        public void run() {
            int count = 0;
            while (count++ < 5) {
                try {
                    Thread.sleep(1000);
                    if (count == 3) {
                        this.interrupt(); // Ngừng hoạt động hiện tại để làm một việc gì đó (ở đây là ngừng sleep)
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }
                // System.out.format("Customer Thread %s is running\n", Thread.currentThread().getName());
                System.out.format("Banking Thread %s is running %d\n", this.getName(), count);
            }
        }
    }

    public static void main(String[] args) {
        BankingThread bt = new BankingThread();
        bt.setName("A");
        bt.start();
    }
}
