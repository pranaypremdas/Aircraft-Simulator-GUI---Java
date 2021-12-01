package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftType;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;
import towersim.util.Tickable;

import java.util.*;

/**
 * Represents a the control tower of an airport.
 * <p>
 * The control tower is responsible for managing the operations of the airport, including arrivals
 * and departures in/out of the airport, as well as aircraft that need to be loaded with cargo
 * at gates in terminals.
 *
 * @ass1
 */
public class ControlTower implements Tickable {
    /**
     * List of all aircraft managed by the control tower.
     */
    private final List<Aircraft> aircraft;

    /**
     * List of all terminals in the airport.
     */
    private final List<Terminal> terminals;

    /**
     * Number of ticks that have elapsed since the tower was first created
     */
    private long ticksElapsed;

    /**
     * Queue of aircraft waiting to land
     */
    private LandingQueue landingQueue;

    /**
     * Queue of aircraft waiting to take off
     */
    private TakeoffQueue takeoffQueue;

    /**
     * Mapping of aircraft that are loading cargo to the number of ticks remaining for loading
     */
    private Map<Aircraft, Integer> loadingAircraft;

    /**
     * Creates a new ControlTower.
     * <p>
     * The number of ticks elapsed, list of aircraft, landing queue, takeoff queue
     * and map of loading
     * aircraft to loading times should all be set to the values passed as parameters.
     *
     * @param ticksElapsed    number of ticks that have elapsed since the tower was first created
     * @param aircraft        list of aircraft managed by the control tower
     * @param landingQueue    queue of aircraft waiting to land
     * @param takeoffQueue    queue of aircraft waiting to take off
     * @param loadingAircraft mapping of aircraft that are loading cargo to the number of ticks
     *                        remaining for loading
     */
    public ControlTower(long ticksElapsed, List<Aircraft> aircraft, LandingQueue landingQueue,
                        TakeoffQueue takeoffQueue, Map<Aircraft, Integer> loadingAircraft) {
        this.aircraft = aircraft;
        this.terminals = new ArrayList<>();
        this.ticksElapsed = ticksElapsed;
        this.landingQueue = landingQueue;
        this.takeoffQueue = takeoffQueue;
        this.loadingAircraft = loadingAircraft;
    }

    /**
     * Adds the given terminal to the jurisdiction of this control tower.
     *
     * @param terminal terminal to add
     * @ass1
     */
    public void addTerminal(Terminal terminal) {
        this.terminals.add(terminal);
    }

    /**
     * Returns a list of all terminals currently managed by this control tower.
     * <p>
     * The order in which terminals appear in this list should be the same as the order in which
     * they were added by calling {@link #addTerminal(Terminal)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all terminals
     * @ass1
     */
    public List<Terminal> getTerminals() {
        return new ArrayList<>(this.terminals);
    }

    /**
     * Adds the given aircraft to the jurisdiction of this control tower.
     * <p>
     * If the aircraft's current task type is {@code WAIT} or {@code LOAD}, it should be parked at a
     * suitable gate as found by the {@link #findUnoccupiedGate(Aircraft)} method.
     * If there is no suitable gate for the aircraft, the {@code NoSuitableGateException} thrown by
     * {@code findUnoccupiedGate()} should be propagated out of this method.
     * <p>
     * After the aircraft has been added, it should be placed in the appropriate queues by calling
     * placeAircraftInQueues(Aircraft).
     *
     * @param aircraft aircraft to add
     * @throws NoSuitableGateException if there is no suitable gate for an aircraft with a current
     *                                 task type of {@code WAIT} or {@code LOAD}
     * @ass1
     */
    public void addAircraft(Aircraft aircraft) throws NoSuitableGateException {
        TaskType currentTaskType = aircraft.getTaskList().getCurrentTask().getType();
        if (currentTaskType == TaskType.WAIT || currentTaskType == TaskType.LOAD) {
            Gate gate = findUnoccupiedGate(aircraft);
            try {
                gate.parkAircraft(aircraft);
            } catch (NoSpaceException ignored) {
                // not possible, gate unoccupied
            }
        }
        this.aircraft.add(aircraft);
        placeAircraftInQueues(aircraft);
    }

    /**
     * Returns a list of all aircraft currently managed by this control tower.
     * <p>
     * The order in which aircraft appear in this list should be the same as the order in which
     * they were added by calling {@link #addAircraft(Aircraft)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all aircraft
     * @ass1
     */
    public List<Aircraft> getAircraft() {
        return new ArrayList<>(this.aircraft);
    }

    /**
     * Attempts to find an unoccupied gate in a compatible terminal for the given aircraft.
     * <p>
     * Only terminals of the same type as the aircraft's AircraftType (see
     * {@link towersim.aircraft.AircraftCharacteristics#type}) should be considered. For example,
     * for an aircraft with an AircraftType of {@code AIRPLANE}, only AirplaneTerminals may be
     * considered.
     * <p>
     * For each compatible terminal, the {@link Terminal#findUnoccupiedGate()} method should be
     * called to attempt to find an unoccupied gate in that terminal. If
     * {@code findUnoccupiedGate()} does not find a suitable gate, the next compatible terminal
     * in the order they were added should be checked instead, and so on.
     * <p>
     * If no unoccupied gates could be found across all compatible terminals, a
     * {@code NoSuitableGateException} should be thrown.
     *
     * @param aircraft aircraft for which to find gate
     * @return gate for given aircraft if one exists
     * @throws NoSuitableGateException if no suitable gate could be found
     * @ass1
     */
    public Gate findUnoccupiedGate(Aircraft aircraft) throws NoSuitableGateException {
        AircraftType aircraftType = aircraft.getCharacteristics().type;
        for (Terminal terminal : terminals) {
            // Only accepts terminals that are currently not in a state of emergency.
            if (!terminal.hasEmergency()) {
                /*
                 * Only check for available gates at terminals that are of the same aircraft type as
                 * the aircraft
                 */
                if ((terminal instanceof AirplaneTerminal && aircraftType == AircraftType.AIRPLANE)
                        || (terminal instanceof HelicopterTerminal
                        && aircraftType == AircraftType.HELICOPTER)) {
                    try {
                        // This terminal found a gate, return it
                        return terminal.findUnoccupiedGate();
                    } catch (NoSuitableGateException noSuitableGateException) {
                        // If this terminal has no unoccupied gates, try the next one
                    }
                }
            }
        }
        throw new NoSuitableGateException("No gate available for aircraft");
    }

    /**
     * Finds the gate where the given aircraft is parked, and returns null if the aircraft is
     * not parked at any gate in any terminal.
     *
     * @param aircraft aircraft whose gate to find
     * @return gate occupied by the given aircraft; or null if none exists
     * @ass1
     */
    public Gate findGateOfAircraft(Aircraft aircraft) {
        for (Terminal terminal : this.terminals) {
            for (Gate gate : terminal.getGates()) {
                if (Objects.equals(gate.getAircraftAtGate(), aircraft)) {
                    return gate;
                }
            }
        }
        return null;
    }

    /**
     * Advances the simulation by one tick.
     * <p>
     * On each tick, the control tower should perform the following actions in this order:
     * <p>
     * 1. Call Aircraft.tick() on all aircraft.
     * 2. Move all aircraft with a current task type of AWAY or WAIT to their next task.
     * 3. Process loading aircraft by calling loadAircraft().
     * 4. On every second tick, attempt to land an aircraft by calling tryLandAircraft().
     * If an aircraft cannot be landed, attempt to allow an aircraft to take off instead
     * by calling tryTakeOffAircraft(). Note that this begins from the second time tick()
     * is called and every second tick thereafter.
     * 5. If this is not a tick where the control tower is attempting to land an aircraft, an
     * aircraft should be allowed to take off instead. This ensures that aircraft wishing to
     * take off and land are given an equal share of the runway.
     * 6. Place all aircraft in their appropriate queues by calling placeAllAircraftInQueues()
     */
    @Override
    public void tick() {
        // Increments the number of ticks that have passed since the tower has been created
        ticksElapsed++;

        // Call tick() on all other sub-entities
        for (Aircraft aircraft : this.aircraft) {
            aircraft.tick();
            // Move all aircraft with a current task type of AWAY or WAIT to their next task.
            switch (aircraft.getTaskList().getCurrentTask().getType()) {
                case AWAY:
                case WAIT:
                    aircraft.getTaskList().moveToNextTask();
            }
        }
        // Process loading aircraft by calling loadAircraft().
        loadAircraft();
        // Attempts to land or takeoff aircraft.
        if (ticksElapsed % 2 == 0) {
            if (!tryLandAircraft()) {
                tryTakeOffAircraft();
            }
        } else {
            tryTakeOffAircraft();
        }
        placeAllAircraftInQueues();
    }

    /**
     * Returns the number of ticks that have elapsed for this control tower.
     * <p>
     * If the control tower was created with a non-zero number of elapsed ticks, this number
     * should be taken into account in the return value of this method.
     * <p>
     * For example, if the control tower was created with 5 ticks elapsed, and tick() has been
     * called three times since creation, then this method should return 8
     *
     * @return number of ticks elapsed
     */
    public long getTicksElapsed() {
        return this.ticksElapsed;
    }

    /**
     * Returns the queue of aircraft waiting to land.
     *
     * @return landing queue
     */
    public AircraftQueue getLandingQueue() {
        return this.landingQueue;
    }

    /**
     * Returns the queue of aircraft waiting to take off.
     *
     * @return takeoff queue
     */
    public AircraftQueue getTakeoffQueue() {
        return this.takeoffQueue;
    }

    /**
     * Returns the mapping of loading aircraft to their remaining load times.
     *
     * @return loading aircraft map
     */
    public Map<Aircraft, Integer> getLoadingAircraft() {
        return this.loadingAircraft;
    }

    /**
     * Attempts to land one aircraft waiting in the landing queue and park it at a suitable gate.
     * If there are no aircraft in the landing queue waiting to land, then the method should return
     * false and no further action should be taken.
     * <p>
     * If there is at least one aircraft in the landing queue, then a suitable gate should be
     * found for the aircraft at the front of the queue (see findUnoccupiedGate(Aircraft)).
     * If there is no suitable gate, the aircraft should not be landed and should remain in
     * the queue, and the method should return false and no further action should be taken.
     * <p>
     * If there is a suitable gate, the aircraft should be removed from the queue and it
     * should be parked at
     * that gate. The aircraft's passengers or freight should be unloaded immediately,
     * by calling Aircraft.unload().
     * <p>
     * Finally, the landed aircraft should move on to the next task in its task list and
     * the method should
     * return true.
     *
     * @return true if an aircraft was successfully landed and parked; false otherwise
     */
    public boolean tryLandAircraft() {
        try {
            if (getLandingQueue().getAircraftInOrder().isEmpty()) {
                return false;
            }
            // finds an unoccupied gate for the first aircraft
            Gate unoccupiedGate = findUnoccupiedGate(getLandingQueue().peekAircraft());

            // If an Unoccupied gate is found it is stored in unoccupiedGate else
            // a NoSuitableGate exception is thrown.

            // parks aircraft
            unoccupiedGate.parkAircraft(getLandingQueue().peekAircraft());
            getLandingQueue().peekAircraft().unload();
            getLandingQueue().removeAircraft().getTaskList().moveToNextTask();

            // Error caught when there is no suitable exception - thrown by find unoccupied gate
            //
            // NoSpaceException is thrown by parkAircraft if a gate is occupied. However,
            // this should not be a problem as a gates occupancy is determined by the
            // findUnoccupiedGate method
            // and a NoSuitableGateException is thrown if there is no suitable gate available.
        } catch (NoSpaceException noSpaceException) {
            return false;
        } catch (NoSuitableGateException noSuitableGateException) {
            noSuitableGateException.printStackTrace();
        }
        return true;
    }

    /**
     * Attempts to allow one aircraft waiting in the takeoff queue to take off.
     * <p>
     * If there are no aircraft waiting in the takeoff queue, then the method should return.
     * Otherwise, the aircraft at the front of the takeoff queue should be removed from the queue
     * and it should move to the next task in its task list.
     */
    public void tryTakeOffAircraft() {
        if (getTakeoffQueue().getAircraftInOrder().isEmpty()) {
            return;
        }
        getTakeoffQueue().removeAircraft().getTaskList().moveToNextTask();
    }

    /**
     * Updates the time remaining to load on all currently loading aircraft and removes aircraft
     * from their gate once finished loading.
     * <p>
     * Any aircraft in the loading map should have their time remaining decremented by one tick.
     * <p>
     * If any aircraft's time remaining is now zero, it has finished loading and should be removed
     * from the loading map. Additionally, it should leave the gate it is parked at and should move
     * on to its next task.
     */
    public void loadAircraft() {
        // Duplicate map created to prevent concurrent exception
        Map<Aircraft, Integer> duplicateLoadingAircraft
                = new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));
        duplicateLoadingAircraft.putAll(loadingAircraft);

        for (Map.Entry<Aircraft, Integer> map : duplicateLoadingAircraft.entrySet()) {
            Aircraft aircraft = map.getKey();
            int loadingTime = map.getValue();
            loadingTime--;
            loadingAircraft.put(aircraft, loadingTime);
            duplicateLoadingAircraft.put(aircraft, loadingTime);
            // When loading time = 0 aircraft is removed from map and gate
            // as it has finished loading
            if (loadingTime == 0) {
                loadingAircraft.remove(aircraft);
                findGateOfAircraft(aircraft).aircraftLeaves();
                aircraft.getTaskList().moveToNextTask();

            }
        }
    }

    /**
     * Calls placeAircraftInQueues(Aircraft) on all aircraft managed by the control tower.
     */
    public void placeAllAircraftInQueues() {
        for (Aircraft aircraft : getAircraft()) {
            placeAircraftInQueues(aircraft);
        }
    }

    /**
     * Moves the given aircraft to the appropriate queue based on its current task.
     * - If the aircraft's current task type is LAND and the landing queue does not already
     * contain the aircraft,
     * it should be added to the landing queue.
     * <p>
     * - If the aircraft's current task type is TAKEOFF and the takeoff queue does not already
     * contain the aircraft, it should be added to the takeoff queue.
     * <p>
     * - If the aircraft's current task type is LOAD and the loading map does not already contain
     * the aircraft, it should be added to the loading map with an associated value of
     * Aircraft.getLoadingTime() (this is the number of ticks it will remain in the
     * loading phase).
     *
     * @param aircraft aircraft to move to appropriate queue
     */
    public void placeAircraftInQueues(Aircraft aircraft) {
        switch (aircraft.getTaskList().getCurrentTask().getType()) {
            case LAND:
                if (!getLandingQueue().containsAircraft(aircraft)) {
                    getLandingQueue().addAircraft(aircraft);
                }
                break;
            case TAKEOFF:
                if (!getTakeoffQueue().containsAircraft(aircraft)) {
                    getTakeoffQueue().addAircraft(aircraft);
                }
                break;
            case LOAD:
                if (!getLoadingAircraft().containsKey(aircraft)) {
                    getLoadingAircraft().put(aircraft, aircraft.getLoadingTime());
                }
                break;
        }
    }

    /**
     * Returns the human-readable string representation of this control tower.
     * The format of the string to return is
     * <p>
     * ControlTower: numTerminals terminals, numAircraft total aircraft (numLanding LAND,
     * numTakeoff TAKEOFF, numLoad LOAD)
     * <p>
     * where numTerminals is the number of terminals, numAircraft is the number of aircraft,
     * numLanding is the number of aircraft in the landing queue, numTakeoff is the number of
     * aircraft in the takeoff queue, and numLoad is the number of aircraft in the loading map.
     * <p>
     * For example: "ControlTower: 3 terminals, 12 total aircraft (3 LAND, 4 TAKEOFF, 2 LOAD)".
     *
     * @return string representation of this control tower
     */
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        joiner.add(terminals.size() + " terminals");
        joiner.add(getAircraft().size() + " total aircraft ("
                + getLandingQueue().getAircraftInOrder().size() + " LAND");
        joiner.add(getTakeoffQueue().getAircraftInOrder().size() + " TAKEOFF");
        joiner.add(getLoadingAircraft().size() + " LOAD)");
        return "ControlTower: " + joiner;
    }
}
