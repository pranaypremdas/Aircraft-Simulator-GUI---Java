package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.PassengerAircraft;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rule-based queue of aircraft waiting in the air to land.
 * <p>
 * The rules in the landing queue are designed to ensure that aircraft are prioritised for
 * landing based on "urgency" factors such as remaining fuel onboard, emergency status and
 * cargo type.
 */
public class LandingQueue extends AircraftQueue {

    /**
     * Arraylist for landing queue
     */
    private List<Aircraft> landingQueue;

    /**
     * Constructs a new LandingQueue with an initially empty queue of aircraft.
     */
    public LandingQueue() {
        this.landingQueue = new ArrayList<>();
    }

    /**
     * Adds the given aircraft to the queue.
     *
     * @param aircraft aircraft to add to queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        this.landingQueue.add(aircraft);
    }

    /**
     * Returns the aircraft at the front of the queue without removing it from the queue,
     * or null if the queue is empty.
     * <p>
     * The rules for determining which aircraft in the queue should be returned next are as follows:
     * 1. If an aircraft is currently in a state of emergency, it should be returned. If more
     * than one aircraft are in an emergency, return the one added to the queue first.
     * 2. If an aircraft has less than or equal to 20 percent fuel remaining, a critical level,
     * it should be returned (see Aircraft.getFuelPercentRemaining()). If more than one aircraft
     * have a critical level of fuel onboard, return the one added to the queue first.
     * 3. If there are any passenger aircraft in the queue, return the passenger aircraft that
     * was added to the queue first.
     * 4. If this point is reached and no aircraft has been returned, return the aircraft that
     * was added to the queue first.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft peekAircraft() {
        if (this.landingQueue.isEmpty()) {
            return null;
        }
        // Checks if any aircraft is in a state of emergency
        for (Aircraft aircraft : this.landingQueue) {
            if (aircraft.hasEmergency()) {
                return aircraft;
            }
        }
        // Checks if any aircraft has lower than 20 percent fuel
        for (Aircraft aircraft : this.landingQueue) {
            if (aircraft.getFuelPercentRemaining() <= 20) {
                return aircraft;
            }
        }
        // checks if aircraft is an instance of Passenger aircraft
        for (Aircraft aircraft : this.landingQueue) {
            if (aircraft instanceof PassengerAircraft) {
                return aircraft;
            }
        }
        return this.landingQueue.get(0);
    }

    /**
     * Removes and returns the aircraft at the front of the queue. Returns null if
     * the queue is empty.
     *
     * @return aircraft at front of queue
     */
    @Override
    public Aircraft removeAircraft() {
        if (this.landingQueue.isEmpty()) {
            return null;
        }
        Aircraft returningAircraft = peekAircraft();
        landingQueue.remove(peekAircraft());
        return returningAircraft;
    }

    /**
     * Returns a list containing all aircraft in the queue, in order.
     * That is, the first element of the returned list should be the first aircraft that
     * would be returned by calling removeAircraft(), and so on.
     * <p>
     * Adding or removing elements from the returned list should not affect the original queue.
     *
     * @return list of all aircraft in queue, in queue order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        List<Aircraft> duplicate = new ArrayList<>(this.landingQueue);
        List<Aircraft> aircrafts = new ArrayList<>();
        for (int i = 0; i < duplicate.size(); i++) {
            aircrafts.add(removeAircraft());
        }
        this.landingQueue = duplicate;
        return aircrafts;
    }

    /**
     * Returns true if the given aircraft is in the queue.
     *
     * @param aircraft aircraft to find in queue
     * @return true if aircraft is in queue; false otherwise
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        for (Aircraft aircraftn : getAircraftInOrder()) {
            if (aircraftn.equals(aircraft)) {
                return true;
            }
        }
        return false;
    }
}
