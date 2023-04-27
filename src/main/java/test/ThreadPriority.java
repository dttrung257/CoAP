package test;

public class ThreadPriority {
    private static class EducationThread extends Thread {

        @Override
        public void run() {
            System.out.println("Thread name: " + Thread.currentThread().getName() + ", Thread priority: " + this.getPriority());
        }
    }
    public static void main(String[] args) {
        EducationThread etA = new EducationThread();
        EducationThread etB = new EducationThread();
        etA.setName("A");
        etB.setName("B");
        etA.setPriority(Thread.MAX_PRIORITY); // Chỉ là mong muốn thôi chứ chưa chắc jre đã thực hiện như vậy
        etB.setPriority(Thread.MIN_PRIORITY);
//        etA.start();
//        etB.start();
        etA.run();
        etB.run();
    }
}
