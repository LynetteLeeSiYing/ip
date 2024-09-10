package socchat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Scanner;

import socchat.task.Task;
import socchat.task.deadline.Deadline;
import socchat.task.event.Event;
import socchat.task.todo.Todo;


/**
 * Handles the loading and saving of tasks from/to a file.
 * Provides methods to read tasks from a file and write tasks to a file.
 */
public class Storage {
    private static String filePath;

    /**
     * Constructs a Storage instance with the specified file path.
     * Initializes the file path for storing and loading tasks.
     *
     * @param filePath the path to the file where tasks are stored
     */
    public Storage(String filePath) {
        Storage.filePath = filePath;
    }

    /**
     * Loads tasks from the storage file.
     * Parses the file content and creates Task objects based on the data.
     *
     * @return a list of tasks loaded from the file
     * @throws SocchatException if the file is not found or if there is an error parsing the file
     */
    public static ArrayList<Task> processStorageLine() throws SocchatException {
        ArrayList<Task> tasks = new ArrayList<>();
        try {
            File file = new File(filePath);
            Scanner s = new Scanner(file);
            while (s.hasNextLine()) {
                String line = s.nextLine();
                String[] strToken = line.split("\\|");

                assert strToken.length >= 3;
                String type = strToken[0].trim();
                String done = strToken[1].trim();
                String desc = strToken[2].trim();

                Task t;
                boolean isDone;
                isDone = done.equals("Done");

                if (type.equals("T")) {
                    t = createTodo(desc, isDone);
                } else if (type.equals("E")) {
                    assert strToken.length == 4;
                    String date = strToken[3].trim();
                    t = loadEvent(desc, isDone, date);
                } else if (type.equals("D")) {
                    assert strToken.length == 4;
                    String date = strToken[3].trim();
                    t = createDeadline(desc, isDone, date);
                } else {
                    throw new SocchatException("Unknown task type: " + type);
                }
                tasks.add(t);
            }
        } catch (FileNotFoundException e) {
            throw new SocchatException("Storage file not found");
        }

        return tasks;

    }

    public static Task createTodo(String desc, boolean isDone) {
        return new Todo(desc, isDone);
    }

    public static Task loadEvent(String desc, boolean isDone, String date) {
        try {
            String[] dateToken = date.split("to");
            LocalDateTime from = Parser.parseDate(dateToken[0].trim());
            LocalDateTime to = Parser.parseDate(dateToken[1].trim());
            return new Event(desc, from, to, isDone);
        } catch (SocchatException e){
            System.out.println(e.getMessage());
            return null;
        }

    }
    public static Task createDeadline(String desc, boolean isDone, String date) {
        try {
            LocalDateTime by = Parser.parseDate(date);
            return new Deadline(desc, by, isDone);
        } catch (SocchatException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * Updates the storage file with the specified list of tasks.
     * Writes tasks to the file, either appending to the existing content or rewriting the file.
     *
     * @param tasks the list of tasks to be written to the file
     * @param needAppend if true, appends the last task to the file; otherwise, overwrites the file with all tasks
     */
    public static void update(ArrayList<Task>tasks, Boolean needAppend) {
        ArrayList<Task> content = new ArrayList<>();
        if (needAppend) {
            content.add(tasks.get(tasks.size() - 1)); // Append only the last added task
        } else {
            content = tasks; // Rewrite the tasks
        }

        try (FileWriter writer = new FileWriter(filePath, needAppend)) {
            for (Task t : content) {
                writer.write(t.toSave());
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
