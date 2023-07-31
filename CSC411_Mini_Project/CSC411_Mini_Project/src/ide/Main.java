/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ide;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class ITstudent {
    String name;
    String studentId;
    String programme;
    Map<String, Integer> courses;

    public ITstudent(String name, String studentId, String programme, Map<String, Integer> courses) {
        this.name = name;
        this.studentId = studentId;
        this.programme = programme;
        this.courses = courses;
    }
}

class Producer implements Runnable {
    private BlockingQueue<Integer> buffer;

    public Producer(BlockingQueue<Integer> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        Random random = new Random();
        while (true) {
            ITstudent student = generateStudent();
            String xmlString = wrapStudent(student);
            int fileNum = random.nextInt(10) + 1;
            String fileName = "student" + fileNum + ".xml";
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(xmlString);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                buffer.put(fileNum);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Produced: " + fileName);
            try {
                Thread.sleep(random.nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ITstudent generateStudent() {
        Random random = new Random();
        String name = "Student " + (random.nextInt(100) + 1);
        String studentId = "" + (random.nextInt(90000000) + 10000000);
        String programme = "Programme " + (random.nextInt(10) + 1);
        Map<String, Integer> courses = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            courses.put("Course " + i, random.nextInt(101));
        }
        return new ITstudent(name, studentId, programme, courses);
    }

    private String wrapStudent(ITstudent student) {
        StringBuilder builder = new StringBuilder();
        builder.append("<student>\n");
        builder.append("  <name>").append(student.name).append("</name>\n");
        builder.append("  <studentId>").append(student.studentId).append("</studentId>\n");
        builder.append("  <programme>").append(student.programme).append("</programme>\n");
        builder.append("  <courses>\n");
        for (Map.Entry<String, Integer> entry : student.courses.entrySet()) {
            builder.append("    <course>\n");
            builder.append("      <name>").append(entry.getKey()).append("</name>\n");
            builder.append("      <mark>").append(entry.getValue()).append("</mark>\n");
            builder.append("    </course>\n");
        }
        builder.append("  </courses>\n");
        builder.append("</student>");
        return builder.toString();
    }
}

class Consumer implements Runnable {
    private BlockingQueue<Integer> buffer;

    public Consumer(BlockingQueue<Integer> buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while (true) {
            int fileNum = 0;
            try {
                fileNum = buffer.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String fileName = "student" + fileNum + ".xml";
            File file = new File(fileName);
            if (!file.exists()) continue;
            ITstudent student = unwrapStudent(file);
            double averageMark =
                    student.courses.values().stream().mapToInt(Integer::intValue).average().orElse(0);
            boolean passed = averageMark >= 50;
            System.out.println("Consumed: " + fileName);
            System.out.println("Name: " + student.name);
            System.out.println("Student ID: " + student.studentId);
            System.out.println("Programme: " + student.programme);
            for (Map.Entry<String, Integer> entry : student.courses.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("Average mark: " + averageMark);
            System.out.println("Passed: " + passed);
            file.delete();
            try {
                Thread.sleep(new Random().nextInt(1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private ITstudent unwrapStudent(File file) {
        // TODO: Implement XML parsing to unwrap student from file
        return null;
    }
}

public class Main {
    public static void main(String[] args) {
        BlockingQueue<Integer> buffer = new LinkedBlockingQueue<>(10);
        Producer producer = new Producer(buffer);
        Consumer consumer = new Consumer(buffer);
        new Thread(producer).start();
        new Thread(consumer).start();
    }
}
