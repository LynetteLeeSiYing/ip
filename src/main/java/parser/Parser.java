package parser;

import socchat.SocchatException;
import socchat.TaskList;
import socchat.task.Task;

import static socchat.Socchat.exit;

/**
 * Provides utility methods for parsing and formatting date and time,
 * as well as tokenizing command strings.
 */
public class Parser {
    private final TaskList taskList;

    public Parser(TaskList taskList) {
        this.taskList = taskList;
    }
    /**
     * Enum representing all available commands in the Socchat application.
     */
    enum Command {
        BYE, LIST, MARK, UNMARK, TODO, EVENT, DEADLINE, DELETE, FIND
    }

    /**
     * Converts the input string to a Command enum value.
     * Throws an exception if the input does not match any command.
     *
     * @param input the command string input from the user
     * @return the corresponding Command enum value
     * @throws SocchatException if the input does not match a valid command
     */
    public static Command getCommand(String input) throws SocchatException {
        try {
            return Command.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SocchatException("Uh Ohh! Socchat does not understand this...");
        }

    }

    public static String[] extractCommand(String input) {
        int firstSpaceIndex = input.indexOf(' ');
        String[] commandTokens = new String[2];

        if (firstSpaceIndex != -1) {
            String command = input.substring(0, firstSpaceIndex);
            commandTokens[0] = command;
            String remaining = input.substring(firstSpaceIndex + 1);
            commandTokens[1] = remaining.trim();
        } else {
            commandTokens[0] = input;
            commandTokens[1] = "";
        }
        return commandTokens;
    }


    /**
     * Gets the corresponding response for each command.
     *
     * @param input the input tokens that have been processed in {@link #extractCommand(String)} processInput
     * @return the generated response as a String.
     */
    public String getResponse(String... input) {
        String respond = "";
        assert input.length == 2;
        try {
            String commandInput = input[0];
            String remaining = input[1];
            Command command = getCommand(commandInput);
            switch (command) {
            case BYE:
                return exit();
            case LIST:
                respond = taskList.list();
                break;
            case MARK:
                respond = taskList.setMark(remaining, true);
                break;
            case UNMARK:
                respond = taskList.setMark(remaining, false);
                break;
            case TODO:
                Task t = TaskParser.todoParser(remaining);
                respond = taskList.addTask(t);
                break;
            case DEADLINE:
                Task d = TaskParser.deadlineParser(remaining);
                respond = taskList.addTask(d);
                break;
            case EVENT:
                Task e = TaskParser.eventParser(remaining);
                respond = taskList.addTask(e);
                break;
            case DELETE:
                respond = taskList.delete(remaining);
                break;
            case FIND:
                respond = taskList.find(remaining);
                break;
            default:
                respond = "Unrecognized command. Please try again.";
                break;
            }

        } catch (SocchatException e) {
            respond = e.getMessage();
        }
        return respond;
    }
}
