package edu.course.gradebook;

import java.util.*;

public class Gradebook {

    private final Map<String, List<Integer>> gradesByStudent = new HashMap<>();
    private final Deque<UndoAction> undoStack = new ArrayDeque<>();
    private final LinkedList<String> activityLog = new LinkedList<>();

    public Optional<List<Integer>> findStudentGrades(String name) {
        return Optional.ofNullable(gradesByStudent.get(name));
    }

    public boolean addStudent(String name) {
        if (gradesByStudent.containsKey(name)) {
            activityLog.add("Student " + name + " already exists");
            return false;
        }
        gradesByStudent.put(name, new ArrayList<Integer>());
        activityLog.add("Added student " + name);
        return true;
    }

    public boolean addGrade(String name, int grade) {
        if (!gradesByStudent.containsKey(name)) {
            activityLog.add("Student " + name + " not found");
            return false;
        }
        gradesByStudent.get(name).add(grade);
        int index = gradesByStudent.get(name).size()-1;
        //push an action onto undo stack to remove this grade
        UndoAction undo = gradebook -> gradebook.gradesByStudent.get(name).remove(index);
        undoStack.push(undo);
        activityLog.add("Added grade " + grade);
        return true;

    }

    public boolean removeStudent(String name) {
        if (gradesByStudent.containsKey(name)) {
            gradesByStudent.remove(name);
            UndoAction undo = gradebook -> gradebook.addStudent(name);
            undoStack.push(undo);
            activityLog.add("student " + name + " removed");
            return true;
        }
        activityLog.add("Student " + name + " not found");
        return false;
    }

    public Optional<Double> averageFor(String name) {

        Optional<List<Integer>> grades = findStudentGrades(name);
        if (grades.get().isEmpty()) {
            return Optional.empty();
        }
        int sum = 0;
        for (Integer grade : grades.orElse(null)) {
            sum += grade;
        }
        Double average = ((double) sum / (grades.get().size()));
        return Optional.of(average);
    }

    public Optional<String> letterGradeFor(String name) {

        Optional<Double> avg = averageFor(name);
        if (!avg.get().isNaN()) {
            Double doubleAvg = avg.get();
            String grade = switch (doubleAvg) {
                case Double d when d >= 90 -> {

                    yield "A";
                }
                case Double d when d >= 80 && d < 90 -> {
                    yield "B";
                }
                case Double d when d >= 70 && d < 80 -> {
                    yield "C";
                }
                case Double d when d >= 60 && d < 70 -> {
                    yield "D";
                }
                default -> "F";
            };
            return Optional.of(grade);
        }
        return Optional.empty();
    }

    public Optional<Double> classAverage() {
        int sum = 0;
        int gradeAmt = 0;
        Students:
        for (String student : gradesByStudent.keySet()) {
            List<Integer> grades = gradesByStudent.get(student);
            if (grades.isEmpty()) {
                continue Students;
            }
            StudentsGrades:
            for (Integer grade : grades) {
                sum += grade;
                gradeAmt++;
            }
        }
        Double average = (double) (sum / (double) gradeAmt);
        if (gradeAmt < 1) {
            return Optional.empty();
        }
        return Optional.of(average);
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            activityLog.add("No action to undo");
            return false;

        }
        UndoAction last = undoStack.pop();
        last.undo(this);
        activityLog.add("Undid action");
        return true;

    }

    public List<String> recentLog(int maxItems) {
        //I wasn't sure how to use the descending Iterator so I had AI give me a similar example and then I adpated it
        Iterator<String> descendingIterator = activityLog.descendingIterator();
        List<String> log = new ArrayList<>();
        while (descendingIterator.hasNext() && log.size() < maxItems) {
            log.add(descendingIterator.next());
        }
        return log;
    }
}
