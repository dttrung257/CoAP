package test.runnable;

import java.util.ArrayList;

public class Management {
    public static void main(String[] args) {
        Enrollment enrollment = Enrollment.getInstance(new ArrayList<>());
        Thread thread = new Thread(enrollment, "th1");
        thread.start();
    }
}
