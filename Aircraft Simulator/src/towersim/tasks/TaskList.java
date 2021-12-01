package towersim.tasks;

import towersim.util.Encodable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a circular list of tasks for an aircraft to cycle through.
 *
 * @ass1
 */
public class TaskList implements Encodable {
    /**
     * List of tasks to cycle through.
     */
    private final List<Task> tasks;
    /**
     * Index of current task in tasks list.
     */
    private int currentTaskIndex;

    // Lists of all the types of tasks that cannot come after a task
    /**
     * A list containing all the tasks that cannot come after the away task
     */
    private static final List<TaskType> awayTaskViolations
            = new ArrayList<>(Arrays.asList(TaskType.WAIT, TaskType.LOAD, TaskType.TAKEOFF));
    /**
     * A list containing all the tasks that cannot come after the land task
     */
    private static final List<TaskType> landTaskViolations
            = new ArrayList<>(Arrays.asList(TaskType.AWAY, TaskType.TAKEOFF, TaskType.LAND));
    /**
     * A list containing all the tasks that cannot come after the wait task
     */
    private static final List<TaskType> waitTaskViolations
            = new ArrayList<>(Arrays.asList(TaskType.TAKEOFF, TaskType.LAND, TaskType.AWAY));
    /**
     * A list containing all the tasks that cannot come after the load task
     */
    private static final List<TaskType> loadTaskViolations
            = new ArrayList<>(Arrays.asList(TaskType.WAIT, TaskType.LAND,
            TaskType.LOAD, TaskType.AWAY));
    /**
     * A list containing all the tasks that cannot come after the takeoff task
     */
    private static final List<TaskType> takeOffTaskViolations
            = new ArrayList<>(Arrays.asList(TaskType.TAKEOFF, TaskType.LAND,
            TaskType.WAIT, TaskType.LOAD));

    /**
     * Creates a new TaskList with the given list of tasks.
     * <p>
     * Initially, the current task (as returned by {@link #getCurrentTask()}) should be the first
     * task in the given list.
     *
     * @param tasks list of tasks
     * @throws IllegalArgumentException if an invalid task is returned
     * @ass1
     */
    public TaskList(List<Task> tasks) {
        this.tasks = tasks;
        this.currentTaskIndex = 0;
        // Checks if the list is empty
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException();
        }
        // loops thorough all the elements and checks if the next task is invalid
        for (int i = 0; i < tasks.size(); i++, moveToNextTask()) {
            switch (getCurrentTask().getType()) {
                case AWAY:
                    if (awayTaskViolations.contains(getNextTask().getType())) {
                        throw new IllegalArgumentException();
                    }
                    continue;
                case LAND:
                    if (landTaskViolations.contains(getNextTask().getType())) {
                        throw new IllegalArgumentException();
                    }
                    continue;
                case WAIT:
                    if (waitTaskViolations.contains(getNextTask().getType())) {
                        throw new IllegalArgumentException();
                    }
                    continue;
                case LOAD:
                    if (loadTaskViolations.contains(getNextTask().getType())) {
                        throw new IllegalArgumentException();
                    }
                    continue;
                case TAKEOFF:
                    if (takeOffTaskViolations.contains(getNextTask().getType())) {
                        throw new IllegalArgumentException();
                    }
            }
        }
    }

    /**
     * Returns the current task in the list.
     *
     * @return current task
     * @ass1
     */
    public Task getCurrentTask() {
        return this.tasks.get(this.currentTaskIndex);
    }

    /**
     * Returns the task in the list that comes after the current task.
     * <p>
     * After calling this method, the current task should still be the same as it was before calling
     * the method.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * this method should return the first element of the list.
     *
     * @return next task
     * @ass1
     */
    public Task getNextTask() {
        int nextTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
        return this.tasks.get(nextTaskIndex);
    }

    /**
     * Moves the reference to the current task forward by one in the circular task list.
     * <p>
     * After calling this method, the current task should be the next task in the circular list
     * after the "old" current task.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * the new current task should be the first element of the list.
     *
     * @ass1
     */
    public void moveToNextTask() {
        this.currentTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
    }

    /**
     * Returns the human-readable string representation of this task list.
     * <p>
     * The format of the string to return is
     * <pre>TaskList currently on currentTask [taskNum/totalNumTasks]</pre>
     * where {@code currentTask} is the {@code toString()} representation of the current task as
     * returned by {@link Task#toString()},
     * {@code taskNum} is the place the current task occurs in the task list, and
     * {@code totalNumTasks} is the number of tasks in the task list.
     * <p>
     * For example, a task list with the list of tasks {@code [AWAY, LAND, WAIT, LOAD, TAKEOFF]}
     * which is currently on the {@code WAIT} task would have a string representation of
     * {@code "TaskList currently on WAIT [3/5]"}.
     *
     * @return string representation of this task list
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("TaskList currently on %s [%d/%d]",
                this.getCurrentTask(),
                this.currentTaskIndex + 1,
                this.tasks.size());
    }

    /**
     * Returns the machine-readable string representation of this task list.
     * <p>
     * The format of the string to return is
     * encodedTask1,encodedTask2,...,encodedTaskN
     * where encodedTaskX is the encoded representation of the Xth task in the task list,
     * for X between 1 and N inclusive, where N is the number of tasks in the task list and
     * encodedTask1 represents the current task.
     * <p>
     * For example, for a task list with 6 tasks and a current task of WAIT:
     * WAIT,LOAD@75,TAKEOFF,AWAY,AWAY,LAND
     *
     * @return encoded string representation of this task list
     */
    @Override
    public String encode() {
        StringJoiner joiner = new StringJoiner(",");
        for (Task task : tasks) {
            joiner.add(task.encode());
        }
        return joiner.toString();
    }
}
