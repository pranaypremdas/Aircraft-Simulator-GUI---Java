package towersim.control;

import towersim.aircraft.Aircraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a first-in-first-out (FIFO) queue of aircraft waiting to take off.
 * <p>
 * FIFO ensures that the order in which aircraft are allowed to take off is based on
 * long they have been waiting in the queue. An aircraft that has been waiting for longer
 * than another aircraft will always be allowed to take off before the other aircraft.
 */
public class TakeoffQueue extends AircraftQueue {
    /**
     * Arraylist for landing queue
     */
    private List<Aircraft> takeoffQueue;

    /**
     * Constructs a new TakeoffQueue with an initially empty queue of aircraft.
     */
    public TakeoffQueue() {
        this.takeoffQueue = new ArrayList<>();
    }

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        this.takeoffQueue.add(aircraft);
    }

    /**
     * Returns the aircraft at the front of the queue without removing it from the queue, or
     * null if the queue is empty.
     * <p>
     * Aircraft returned by peekAircraft() should be in the same order that they were added via
     * addAircraft().
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft peekAircraft() {
        if (this.takeoffQueue == null || this.takeoffQueue.isEmpty()) {
            return null;
        }
        return this.takeoffQueue.get(0);
    }

    /**
     * Removes and returns the aircraft at the front of the queue. Returns null if the
     * queue is empty.
     * <p>
     * Aircraft returned by removeAircraft() should be in the same order that they were
     * added via addAircraft().
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft removeAircraft() {
        Aircraft firstAircraft = peekAircraft();
        if (takeoffQueue.size() != 0) {
            this.takeoffQueue.remove(0);
        }
        return firstAircraft;
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     * <p>
     * That is, the first element of the returned list should be the first aircraft that
     * would be returned by calling removeAircraft(), and so on.
     * <p>
     * Adding or removing elements from the returned list should not affect the original queue.
     *
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        return new ArrayList<>(this.takeoffQueue);
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        for (Aircraft a : getAircraftInOrder()) {
            if (a.equals(aircraft)) {
                return true;
            }
        }
        return false;
    }
}
