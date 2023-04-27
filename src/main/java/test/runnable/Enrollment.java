package test.runnable;

import java.util.ArrayList;
import java.util.List;

class Student {
}

public class Enrollment implements Runnable {
    private final List<Student> students;
    private static Enrollment instance = null;

    private Enrollment(List<Student> students) {
        this.students = students;
    }

    public static Enrollment getInstance(List<Student> students) {
        if (instance == null) {
            synchronized (Enrollment.class) {
                if (instance == null) {
                    instance = new Enrollment(students);
                }
            }
        }
        return instance;
    }

    @Override
    public void run() {
        if (students != null) {
            System.out.println("Student list size: " + this.students.size());
            System.out.println("Thread name: " + Thread.currentThread().getName());
            System.out.println("Thread priority: " + Thread.currentThread().getPriority());
        }
    }
}
