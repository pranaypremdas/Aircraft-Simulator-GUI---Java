package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import towersim.util.NoSpaceException;

import java.io.*;
import java.util.*;

/**
 * Utility class that contains static methods for loading a control tower and associated
 * entities from files.
 */
public class ControlTowerInitialiser {
    /**
     * Loads the number of ticks elapsed from the given reader instance.
     * The contents of the reader should match the format specified in the tickWriter row of in the
     * table shown in ViewModel.saveAs().
     * <p>
     * For an example of valid tick reader contents, see the provided saves/tick_basic.txt and
     * saves/tick_default.txt files.
     * <p>
     * The contents read from the reader are invalid if any of the following conditions are true:
     * <p>
     * The number of ticks elapsed is not an integer (i.e. cannot be parsed by
     * Long.parseLong(String)).
     * The number of ticks elapsed is less than zero.
     *
     * @param reader reader from which to load the number of ticks elapsed
     * @return number of ticks elapsed
     * @throws MalformedSaveException if the format of the text read from the reader is invalid
     *                                according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the reader
     */
    public static long loadTick(Reader reader) throws MalformedSaveException, IOException {
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            long tick = Long.parseLong(bufferedReader.readLine());
            if (tick < 0) {
                throw new MalformedSaveException();
            }
            bufferedReader.close();
            return tick;
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Loads the list of all aircraft managed by the control tower from the given reader instance.
     * The contents of the reader should match the format specified in the aircraftWriter row of
     * in the table shown in ViewModel.saveAs().
     * <p>
     * For an example of valid aircraft reader contents, see the provided saves/aircraft_basic.txt
     * and saves/aircraft_default.txt files.
     * <p>
     * The contents read from the reader are invalid if any of the following conditions are true:
     * <p>
     * The number of aircraft specified on the first line of the reader is not an integer (i.e.
     * cannot be parsed by Integer.parseInt(String)).
     * The number of aircraft specified on the first line is not equal to the number of aircraft
     * actually read from the reader.
     * <p>
     * Any of the conditions listed in the Javadoc for readAircraft(String) are true.
     * This method should call readAircraft(String).
     *
     * @param reader reader from which to load the list of aircraft
     * @return list of aircraft read from the reader
     * @throws IOException            if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws MalformedSaveException if an IOException is encountered when reading from the reader
     */
    public static List<Aircraft> loadAircraft(Reader reader) throws IOException,
            MalformedSaveException {
        List<Aircraft> aircraft = new ArrayList<>(); // List of aircraft's in the file

        try {
            BufferedReader bufferedReader = new BufferedReader(reader);

            // Counts the number of aircraft that are in the file
            int count = 0;
            // The number of airplanes stated in file
            int numAirplanes = Integer.parseInt(bufferedReader.readLine());

            //checks if the number of planes is 0. If this is the case an empty list is returned.
            if (numAirplanes == 0) {
                return aircraft;
            }
            String aircraftLine;
            while ((aircraftLine = bufferedReader.readLine()) != null) {
                aircraft.add(readAircraft(aircraftLine));
                count++;
            }
            if (count != numAirplanes) {
                throw new IOException();
            }
        } catch (NumberFormatException ioException) {
            throw new MalformedSaveException();
        }
        return aircraft;
    }

    /**
     * Reads an aircraft from its encoded representation in the given string.
     * If the AircraftCharacteristics.passengerCapacity of the encoded aircraft is greater
     * than zero, then a PassengerAircraft should be created and returned. Otherwise, a
     * FreightAircraft should be created and returned.
     * <p>
     * The format of the string should match the encoded representation of an aircraft,
     * as described in Aircraft.encode().
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <p>
     * - More/fewer colons (:) are detected in the string than expected.
     * - The aircraft's AircraftCharacteristics is not valid, i.e. it is not one of those
     * listed in AircraftCharacteristics.values().
     * - The aircraft's fuel amount is not a double (i.e. cannot be parsed by
     * Double.parseDouble(String)).
     * - The aircraft's fuel amount is less than zero or greater than the aircraft's maximum
     * fuel capacity.
     * - The amount of cargo (freight/passengers) onboard the aircraft is not an integer
     * (i.e. cannot be parsed by Integer.parseInt(String)).
     * - The amount of cargo (freight/passengers) onboard the aircraft is less than zero or
     * greater than the aircraft's maximum freight/passenger capacity.
     * - Any of the conditions listed in the Javadoc for readTaskList(String) are true.
     * <p>
     * This method should call readTaskList(String).
     *
     * @param line line of text containing the encoded aircraft
     * @return decoded aircraft instance
     * @throws MalformedSaveException if the format of the given string is invalid according
     *                                to the rules above
     */
    public static Aircraft readAircraft(String line) throws MalformedSaveException {
        List<String> strings = Arrays.asList(line.split(":"));
        // Checks if there are 6 elements in the list
        // The length 6 is chosen as the format for the aircraft's given has 6 elements
        // i.e. callsign:model:taskListEncoded:fuelAmount:emergency:numPassengers or CargoAmount
        // therefore if the size does not equal 6 it is an invalid string.
        if (strings.size() != 6) {
            throw new MalformedSaveException();
        }

        // amount of fuel in aircraft in litres
        double fuelAmount;

        // number of passengers or cargo amount in an aircraft
        int passengerOrCargo;

        // State of emergency of the aircraft
        boolean emergency;

        // List of tasks for the aircraft to complete
        TaskList taskList;

        try {
            // A list of all the models of an Aircraft from AircraftCharacteristics
            List<AircraftCharacteristics> typesOfAircraft
                    = Arrays.asList(AircraftCharacteristics.values());

            // the model of the aircraft
            AircraftCharacteristics model = AircraftCharacteristics.valueOf(strings.get(1));

            // Checks if a valid AircraftCharacteristic is in strings.
            if (!typesOfAircraft.contains(model)) {
                throw new MalformedSaveException();
            }
            // checks if a valid task list is put in
            taskList = readTaskList(strings.get(2));

            // Checks if the fuel amount is a double
            fuelAmount = Double.parseDouble(strings.get(3));

            // determines the state of emergency of the aircraft
            emergency = Boolean.parseBoolean(strings.get(4));

            //checks if the number of passengers or cargo amount is an integer
            passengerOrCargo = Integer.parseInt(strings.get(5));

            // Checks for invalid fuel amount
            if (fuelAmount < 0 || fuelAmount > model.fuelCapacity) {
                throw new MalformedSaveException();
            }

            // checks for invalid passenger capacity
            if (model.passengerCapacity > 0) {
                if (passengerOrCargo < 0 || passengerOrCargo > model.passengerCapacity) {
                    throw new MalformedSaveException();
                }
                PassengerAircraft passengerAircraft = new PassengerAircraft(strings.get(0),
                        model, taskList, fuelAmount, passengerOrCargo);
                if (emergency) {
                    passengerAircraft.declareEmergency();
                }
                return passengerAircraft;
                // Checks for invalid freight amount
            } else {
                if (passengerOrCargo < 0 || passengerOrCargo > model.freightCapacity) {
                    throw new MalformedSaveException();
                }
                FreightAircraft freightAircraft = new FreightAircraft(strings.get(0),
                        model, taskList, fuelAmount, passengerOrCargo);
                if (emergency) {
                    freightAircraft.declareEmergency();
                }
                return freightAircraft;
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads a task list from its encoded representation in the given string.
     * The format of the string should match the encoded representation of a task list,
     * as described in TaskList.encode().
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <p>
     * - The task list's TaskType is not valid (i.e. it is not one of those listed in
     * TaskType.values()).
     * - A task's load percentage is not an integer (i.e. cannot be parsed by
     * Integer.parseInt(String)).
     * - A task's load percentage is less than zero.
     * - More than one at-symbol (@) is detected for any task in the task list.
     * - The task list is invalid according to the rules specified in TaskList(List).
     *
     * @param taskListPart string containing the encoded task list
     * @return decoded task list instance
     * @throws MalformedSaveException if the format of the given string is invalid according
     *                                to the rules above
     */
    public static TaskList readTaskList(String taskListPart) throws MalformedSaveException {
        if (taskListPart == null) {
            throw new MalformedSaveException();
        }
        // List of all the types of tasks that is considered valid
        List<TaskType> taskList = Arrays.asList(TaskType.values());

        // List of all the tasks in taskListPart
        String[] tasks = taskListPart.split(",");

        // String representing the integer value in in the load string
        String load;

        // list of tasks that will be later validated by TaskList(list) to determine if
        // it follows the requirements of a task list
        List<Task> validate = new ArrayList<>();

        // Checks if the task list's TaskType is not valid (i.e. it is not one of
        // those listed in TaskType.values()).
        try {
            for (String task : tasks) {
                // Checks the load task for a valid load percent
                if (task.contains("LOAD")) {
                    if (!task.contains("@")) {
                        throw new MalformedSaveException();
                    }
                    List<String> loadSplit = Arrays.asList(task.split("@"));
                    if (loadSplit.size() != 2) {
                        throw new MalformedSaveException();
                    }
                    int loadPercent = Integer.parseInt(loadSplit.get(1));
                    if (loadPercent < 0 || loadPercent > 100) {
                        throw new MalformedSaveException();
                    }
                    // Adds task to validate to check for an invalid task list in TaskList(List)
                    validate.add(new Task(TaskType.LOAD, loadPercent));
                    continue;
                }
                // Checks for a valid task
                if (!taskList.contains(TaskType.valueOf(task))) {
                    throw new MalformedSaveException();
                }

                // Adds task to validate to check for an invalid task list in TaskList(List)
                validate.add(new Task(TaskType.valueOf(task)));
            }
            // Catches number format exception from trying to convert a string to an integer
        } catch (IllegalArgumentException exception) {
            throw new MalformedSaveException();
        }
        // Checks if the task list is invalid according to the rules specified in TaskList(List).
        // if valid it returns the valid taskList
        TaskList valid;
        try {
            valid = new TaskList(validate);
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new MalformedSaveException();
        }
        return valid;
    }

    /**
     * Loads the takeoff queue, landing queue and map of loading aircraft from the given
     * reader instance.
     * Rather than returning a list of queues, this method does not return anything. Instead, it
     * modifies the given takeoff queue, landing queue and loading map by adding aircraft, etc.
     * <p>
     * The contents of the reader should match the format specified in the queuesWriter row
     * of in the table shown in ViewModel.saveAs().
     * <p>
     * For an example of valid queues reader contents, see the provided saves/queues_basic.txt and
     * saves/queues_default.txt files.
     * <p>
     * The contents read from the reader are invalid if any of the conditions listed in the Javadoc
     * for readQueue(BufferedReader, List, AircraftQueue) and readLoadingAircraft(BufferedReader,
     * List, Map) are true.
     * <p>
     * This method should call readQueue(BufferedReader, List, AircraftQueue) and
     * readLoadingAircraft(BufferedReader, List, Map).
     *
     * @param reader          reader from which to load the queues and loading map
     * @param aircraft        list of all aircraft, used when validating that callsigns exist
     * @param takeoffQueue    empty takeoff queue that aircraft will be added to
     * @param landingQueue    empty landing queue that aircraft will be added to
     * @param loadingAircraft empty map that aircraft and loading times will be added to
     * @throws MalformedSaveException if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the
     *                                reader
     */
    public static void loadQueues(Reader reader, List<Aircraft> aircraft, TakeoffQueue takeoffQueue,
                                  LandingQueue landingQueue, Map<Aircraft, Integer> loadingAircraft)
            throws MalformedSaveException, IOException {
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            readQueue(bufferedReader, aircraft, takeoffQueue);
            readQueue(bufferedReader, aircraft, landingQueue);
            readLoadingAircraft(bufferedReader, aircraft, loadingAircraft);
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Reads an aircraft queue from the given reader instance.
     * Rather than returning a queue, this method does not return anything. Instead,
     * it should modify the given aircraft queue by adding aircraft to it.
     * <p>
     * The contents of the text read from the reader should match the encoded representation
     * of an aircraft queue, as described in AircraftQueue.encode().
     * <p>
     * The contents read from the reader are invalid if any of the following conditions are true:
     * <p>
     * - The first line read from the reader is null.
     * - The first line contains more/fewer colons (:) than expected.
     * - The queue type specified in the first line is not equal to the simple class name of
     * the queue provided as a parameter.
     * - The number of aircraft specified on the first line is not an integer (i.e. cannot be
     * parsed by Integer.parseInt(String)).
     * - The number of aircraft specified is greater than zero and the second line read is null.
     * - The number of callsigns listed on the second line is not equal to the number of aircraft
     * specified on the first line.
     * - A callsign listed on the second line does not correspond to the callsign of any aircraft
     * contained in the list of aircraft given as a parameter.
     *
     * @param reader   reader from which to load the aircraft queue
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @param queue    empty queue that aircraft will be added to
     * @throws IOException            if the format of the text read from the reader is
     *                                invalid according to the rules above
     * @throws MalformedSaveException if an IOException is encountered when reading from the reader
     */
    public static void readQueue(BufferedReader reader, List<Aircraft> aircraft,
                                 AircraftQueue queue)
            throws IOException, MalformedSaveException {
        try {
            String firstLine = readLine(reader);
            // number of callsigns in the string
            int number = Integer.parseInt(firstLine.replaceAll("[^0-9]", ""));
            if (number == 0) {
                return;
            }
            // Type of queue
            String queueType = firstLine.replaceAll("[^a-zA-Z]", "");

            // Second line in the file reader
            String secondLine = readLine(reader);
            List<String> callsigns = Arrays.asList(secondLine.split(","));

            // If the number of ":" do not match or the aircraft queue type given does not match
            // the String it is invalid or if the second line is null as it will never reach here
            // unless the number > 0
            if (callsigns.size() != number
                    || !queueType.equals(queue.getClass().getSimpleName())) {
                throw new MalformedSaveException();
            }
            // validates and adds aircraft
            // it is valid if the callsign is one of the aircrafts callsign
            // If callsign is invalid a MalformedSaveException is thrown
            for (String callsign : callsigns) {
                queue.addAircraft(callsignValidate(aircraft, callsign));
            }
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }
    }

    /**
     * Validates the callsign passed through the methods. If the callsign is used in any
     * of the aircraft in the list aircrafts then the aircraft that the callsign matches to
     * is returned
     *
     * @param aircrafts list of all the aircrafts to compare the callsign to
     * @param callsign  the callsign to compare each aircraft to
     * @return aircraft if a matching callsign is found
     * @throws MalformedSaveException if an aircraft with the given callsign cannot be found
     */
    private static Aircraft callsignValidate(List<Aircraft> aircrafts, String callsign)
            throws MalformedSaveException {
        for (Aircraft aircraft : aircrafts) {
            if (aircraft.getCallsign().equals(callsign)) {
                return aircraft;
            }
        }
        // reaches this point if no callsign matches the aircraft's
        throw new MalformedSaveException();
    }

    /**
     * Reads the map of currently loading aircraft from the given reader instance.
     * Rather than returning a map, this method does not return anything. Instead,
     * it modifies the given map by adding entries (aircraft/integer pairs) to it.
     * <p>
     * The contents read from the reader are invalid if any of the following
     * conditions are true:
     * <p>
     * - The first line read from the reader is null.
     * - The number of colons (:) detected on the first line is more/fewer than
     * expected.
     * - The number of aircraft specified on the first line is not an integer
     * (i.e. cannot be parsed by Integer.parseInt(String)).
     * - The number of aircraft is greater than zero and the second line read
     * from the reader is null.
     * - The number of aircraft specified on the first line is not equal to the
     * number of callsigns read on the second line.
     * - For any callsign/loading time pair on the second line, the number of
     * colons detected is not equal to one. For example, ABC123:5:9 is invalid.
     * - A callsign listed on the second line does not correspond to the callsign
     * of any aircraft contained in the list of aircraft given as a parameter.
     * - Any ticksRemaining value on the second line is not an integer (i.e. cannot
     * be parsed by Integer.parseInt(String)).
     * - Any ticksRemaining value on the second line is less than one (1).
     *
     * @param reader          reader from which to load the map of loading aircraft
     * @param aircraft        list of all aircraft, used when validating that callsigns exist
     * @param loadingAircraft empty map that aircraft and their loading times will be added to
     * @throws IOException            if the format of the text read from the reader is invalid
     *                                according to the rules above
     * @throws MalformedSaveException if an IOException is encountered when reading from the
     *                                reader
     */
    public static void readLoadingAircraft(BufferedReader reader, List<Aircraft> aircraft,
                                           Map<Aircraft, Integer> loadingAircraft)
            throws IOException, MalformedSaveException {
        try {
            // First line in the file reader
            String firstLine = readLine(reader);
            int number = Integer.parseInt(firstLine.replaceAll("[^0-9]", ""));
            if (number == 0) {
                return;
            }
            // Second line in the file reader
            String secondLine = readLine(reader);
            String[] pairs = (secondLine.split(","));

            // If the number of ":" do not match or the aircraft queue type given does not match
            // the String it is invalid or if the second line is null as it will never reach here
            // unless the number > 0
            if (Arrays.asList(pairs).size() != number) {
                throw new MalformedSaveException();
            }

            for (String s : pairs) {
                // List containing only the key and value of a single pair i.e.
                // single.get(0) = key and single.get(1) = value
                List<String> pair = Arrays.asList(s.split(":"));
                // determines if the pair is valid or not i.e. if there is more that
                // 2 elements in the string
                if (pair.size() != 2) {
                    throw new MalformedSaveException();
                }
                // Checks if a valid callsign is entered.
                // returns aircraft if a matching callsign is found
                Aircraft key = callsignValidate(aircraft, pair.get(0));
                // Integer value of the value pair in the list pair
                int value = Integer.parseInt(pair.get(1));
                if (value < 1) {
                    throw new MalformedSaveException();
                }
                loadingAircraft.put(key, value);
            }
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }
    }

    /**
     * reads line and determines if it is null
     * if not, it returns the string
     *
     * @param reader reader to read the line
     * @return the string that is in the line
     * @throws MalformedSaveException if the line is null
     */
    private static String readLine(BufferedReader reader)
            throws MalformedSaveException, IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new MalformedSaveException();
        }
        return line;
    }

    /**
     * Loads the list of terminals and their gates from the given reader instance.
     * <p>
     * The contents read from the reader are invalid if any of the following conditions are true:
     * <p>
     * - The number of terminals specified at the top of the file is not an integer
     * (i.e. cannot be parsed by Integer.parseInt(String)).
     * - The number of terminals specified is not equal to the number of terminals
     * actually read from the reader.
     * - Any of the conditions listed in the Javadoc for readTerminal(String, BufferedReader, List)
     * and readGate(String, List) are true.
     *
     * @param reader   reader from which to load the list of terminals and their gates
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return list of terminals (with their gates) read from the reader
     * @throws MalformedSaveException if the format of the text read from the reader
     *                                is invalid according to the rules above
     * @throws IOException            if an IOException is encountered when reading from the reader
     */
    public static List<Terminal> loadTerminalsWithGates(Reader reader, List<Aircraft> aircraft)
            throws MalformedSaveException, IOException {
        List<Terminal> terminals = new ArrayList<>();
        // Counts the number of times a terminal was successfully added to the list
        int terminalCount = 0;
        // The number of terminals declared in the file
        int noOfTerminals;
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            noOfTerminals = Integer.parseInt(bufferedReader.readLine());
            String terminalLine;
            while ((terminalLine = bufferedReader.readLine()) != null) {
                terminals.add(readTerminal(terminalLine, bufferedReader, aircraft));
                terminalCount++;
            }
        } catch (NumberFormatException numberFormatException) {
            throw new MalformedSaveException();
        }
        if (terminalCount != noOfTerminals) {
            throw new MalformedSaveException();
        }
        return terminals;
    }

    /**
     * Reads a terminal from the given string and reads its gates from the given reader instance.
     * The format of the given string and the text read from the reader should match the encoded
     * representation of a terminal, as described in Terminal.encode().
     * <p>
     * The encoded terminal is invalid if any of the following conditions are true:
     * <p>
     * - The number of colons (:) detected on the first line is more/fewer than expected.
     * - The terminal type specified on the first line is neither AirplaneTerminal nor
     * HelicopterTerminal.
     * - The terminal number is not an integer (i.e. cannot be parsed by
     * Integer.parseInt(String)).
     * - The terminal number is less than one (1).
     * - The number of gates in the terminal is not an integer.
     * - The number of gates is less than zero or is greater than Terminal.MAX_NUM_GATES.
     * - A line containing an encoded gate was expected, but EOF (end of file) was received
     * (i.e. BufferedReader.readLine() returns null).
     * - Any of the conditions listed in the Javadoc for readGate(String, List) are true.
     *
     * @param line     string containing the first line of the encoded terminal
     * @param reader   reader from which to load the gates of the terminal (subsequent lines)
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded terminal with its gates added
     * @throws IOException            if the format of the given string or the text read from
     *                                the reader is invalid according to the rules above
     * @throws MalformedSaveException if an IOException is encountered when reading from the
     *                                reader
     */
    public static Terminal readTerminal(String line, BufferedReader reader, List<Aircraft> aircraft)
            throws IOException, MalformedSaveException {
        // Terminal that will be returned
        Terminal terminal;
        // list of elements in line i.e.
        // TerminalType:terminalNumber:emergency:numGates
        List<String> elements;
        try {
            elements = Arrays.asList(line.split(":"));
            if (elements.size() != 4) {
                throw new MalformedSaveException();
            }
            // validates Terminal type
            if (!(elements.get(0).equals("AirplaneTerminal"))
                    && !(elements.get(0).equals("HelicopterTerminal"))) {
                throw new MalformedSaveException();
            }
            // Terminal number
            int terminalNumber = Integer.parseInt(elements.get(1));

            // number of gates in terminal
            int noOfGates = Integer.parseInt(elements.get(3));

            // The size 4 is chosen as the encode for terminal uses the format
            // TerminalType:terminalNumber:emergency:numGates in the first line
            // splitting the string by the regex ":" should therefore return
            // 4 elements
            if (terminalNumber < 1 || noOfGates > Terminal.MAX_NUM_GATES
                    || noOfGates < 0) {
                throw new MalformedSaveException();
            }
            // Checks if it is a airplane terminal
            if (elements.get(0).equals("AirplaneTerminal")) {
                terminal = new AirplaneTerminal(terminalNumber);
                // codes for a helicopter terminal
            } else {
                terminal = new HelicopterTerminal(terminalNumber);
            }
            for (int i = 0; i < noOfGates; i++) {
                // String representing the encoded gate
                String gate = reader.readLine();
                terminal.addGate(readGate(gate, aircraft));
            }
        } catch (NumberFormatException | NoSpaceException | EOFException exceptions) {
            throw new MalformedSaveException();
        }
        // Checks if the terminal has an emergency
        if (Boolean.parseBoolean(elements.get(2))) {
            terminal.declareEmergency();
        }
        return terminal;
    }

    /**
     * Reads a gate from its encoded representation in the given string.
     * <p>
     * The encoded string is invalid if any of the following conditions are true:
     * <p>
     * - The number of colons (:) detected was more/fewer than expected.
     * - The gate number is not an integer (i.e. cannot be parsed by Integer.parseInt(String)).
     * - The gate number is less than one (1).
     * - The callsign of the aircraft parked at the gate is not empty and the callsign
     * does not correspond to the callsign of any aircraft contained in the list of aircraft
     * given as a parameter.
     *
     * @param line     string containing the encoded gate
     * @param aircraft list of all aircraft, used when validating that callsigns exist
     * @return decoded gate instance
     * @throws MalformedSaveException if the format of the given string is invalid according
     *                                to the rules above
     */
    public static Gate readGate(String line, List<Aircraft> aircraft)
            throws MalformedSaveException {
        // The gate to be returned at the end of this method.
        Gate newGate;
        // List containing the gate number,callsign pair as separate elements
        int gateNumber;
        try {
            List<String> gate = Arrays.asList(line.split(":"));
            if (gate.size() != 2) {
                throw new MalformedSaveException();
            }
            gateNumber = Integer.parseInt(gate.get(0));
            if (gateNumber < 0) {
                throw new MalformedSaveException();
            }
            newGate = new Gate(gateNumber);
            String callsign = gate.get(1);
            if (!callsign.equals("empty")) {
                Aircraft aircraftAtGate = callsignValidate(aircraft, callsign);
                newGate.parkAircraft(aircraftAtGate);
            }
        } catch (NumberFormatException | NoSpaceException numberFormatException) {
            throw new MalformedSaveException();
        }
        return newGate;
    }

    /**
     * Creates a control tower instance by reading various airport entities from
     * the given readers.
     * The following methods should be called in this order, and their results stored
     * temporarily, to load information from the readers:
     * <p>
     * - loadTick(Reader) to load the number of elapsed ticks
     * - loadAircraft(Reader) to load the list of all aircraft
     * - loadTerminalsWithGates(Reader, List) to load the terminals and their gates
     * - loadQueues(Reader, List, TakeoffQueue, LandingQueue, Map) to load the takeoff queue,
     * landing queue and map of loading aircraft to their loading time remaining
     *
     * @param tick               reader from which to load the number of ticks elapsed
     * @param aircraft           reader from which to load the list of aircraft
     * @param queues             reader from which to load the aircraft queues and map
     *                           of loading aircraft
     * @param terminalsWithGates reader from which to load the terminals and their gates
     * @return control tower created by reading from the given readers
     * @throws MalformedSaveException if reading from any of the given readers results in
     *                                a MalformedSaveException, indicating the contents of
     *                                that reader are invalid.
     * @throws IOException            if an IOException is encountered when reading from any of the
     *                                readers
     */
    public static ControlTower createControlTower(Reader tick, Reader aircraft,
                                                  Reader queues, Reader terminalsWithGates)
            throws MalformedSaveException, IOException {

        TakeoffQueue takeoffQueue = new TakeoffQueue();
        LandingQueue landingQueue = new LandingQueue();
        TreeMap<Aircraft, Integer> loadingAircraft
                = new TreeMap<>(Comparator.comparing(Aircraft::getCallsign));

        long ticks = loadTick(tick);
        List<Aircraft> aircrafts = loadAircraft(aircraft);
        List<Terminal> terminals = loadTerminalsWithGates(terminalsWithGates, aircrafts);
        loadQueues(queues, aircrafts, takeoffQueue, landingQueue, loadingAircraft);

        // New control tower
        ControlTower controlTower = new ControlTower(ticks, aircrafts,
                landingQueue, takeoffQueue, loadingAircraft);
        for (Terminal terminal : terminals) {
            controlTower.addTerminal(terminal);
        }
        return controlTower;
    }
}

