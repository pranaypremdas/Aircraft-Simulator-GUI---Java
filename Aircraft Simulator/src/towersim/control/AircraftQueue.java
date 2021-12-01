package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.util.Encodable;

import java.util.List;
import java.util.StringJoiner;

/**
 * Abstract representation of a queue containing aircraft.
 * Aircraft can be added to the queue, and aircraft at the front of the queue can be queried
 * or removed. A list of all aircraft contained in the queue (in queue order) can be obtained.
 * The queue can be checked for containing a specified aircraft.
 * <p>
 * The order that aircraft are removed from the queue depends on the chosen concrete implementation
 * of the AircraftQueue.
 */
public abstract class AircraftQueue implements Encodable {

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    public abstract void addAircraft(Aircraft aircraft);

    /**
     * Removes and returns the aircraft at the front of the queue.
     * Returns null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public abstract Aircraft removeAircraft();

    /**
     * Returns the aircraft at the front of the queue without removing it from
     * the queue, or null if the queue is empty.
     *
     * @return aircraft at front of queue
     */
    public abstract Aircraft peekAircraft();

    /**
     * Returns a list containing all aircraft in the queue, in order.
     * That is, the first element of the returned list should be the first
     * aircraft that would be returned by calling removeAircraft(), and so on.
     * <p>
     * Adding or removing elements from the returned list should not affect the original queue.
     *
     * @return list of all aircraft in queue, in queue order
     */
    public abstract List<Aircraft> getAircraftInOrder();

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    public abstract boolean containsAircraft(Aircraft aircraft);

    /**
     * Returns the human-readable string representation of this aircraft queue.
     * The format of the string to return is
     * <p>
     * QueueType [callsign1, callsign2, ..., callsignN]
     * where QueueType is the concrete queue class (i.e. LandingQueue or TakeoffQueue) and callsign1
     * through callsignN are the callsigns of all aircraft in the queue,
     * in queue order (see getAircraftInOrder()).
     * For example, "LandingQueue [ABC123, XYZ987, BOB555]".
     *
     * @return string representation of this queue
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        for (Aircraft aircraft : getAircraftInOrder()) {
            joiner.add(aircraft.getCallsign());
        }
        if (getAircraftInOrder().isEmpty()) {
            return getClass().getSimpleName() + " []";
        }
        return getClass().getSimpleName() + " ["
                + joiner + "]";
    }

    /**
     * Returns the machine-readable string representation of this aircraft queue.
     * The format of the string to return is
     * <p>
     * QueueType:numAircraft
     * callsign1,callsign2,...,callsignN
     * <p>
     * where:
     * - QueueType is the simple class name of this queue, e.g. LandingQueue
     * - numAircraft is the number of aircraft currently waiting in the queue
     * - callsignX is the callsign of the Xth aircraft in the queue, in the same order
     * as returned by getAircraftInOrder(),
     * for X between 1 and N inclusive, where N is the number of aircraft in the queue
     * <p>
     * For example:
     * LandingQueue:0
     * <p>
     * For example:
     * TakeoffQueue:3
     * ABC101,QWE456,XYZ789
     *
     * @return encoded string representation of this aircraft queue
     */
    public String encode() {
        StringJoiner joiner = new StringJoiner(",");
        for (Aircraft aircraft : getAircraftInOrder()) {
            joiner.add(aircraft.getCallsign());
        }
        return this.getClass().getSimpleName() + ":" + getAircraftInOrder().size()
                + System.lineSeparator() + joiner;
    }


}
