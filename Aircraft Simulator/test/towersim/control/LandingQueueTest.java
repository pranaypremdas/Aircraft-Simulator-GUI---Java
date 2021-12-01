package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class LandingQueueTest {
    LandingQueue landingQueue;
    TakeoffQueue takeoffQueue;
    PassengerAircraft passengerAircraft1;
    PassengerAircraft passengerAircraft2;
    FreightAircraft freightAircraft1;
    FreightAircraft freightAircraft2;
    PassengerAircraft emergencyStatePassengerAircraft;
    TaskList taskList;

    @Before
    public void setup(){
        landingQueue = new LandingQueue();
        takeoffQueue = new TakeoffQueue();
        taskList = new TaskList(List.of(
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND),
                new Task(TaskType.LOAD),
                new Task(TaskType.TAKEOFF)));
        passengerAircraft1 = new PassengerAircraft("PassengerAircraft1",
                AircraftCharacteristics.AIRBUS_A320, taskList, 2702.00, 100);
        passengerAircraft2 = new PassengerAircraft("passengerAircraft2",
                AircraftCharacteristics.BOEING_787, taskList, 120206.00, 200);
        freightAircraft1 = new FreightAircraft("freightAircraft1",
                AircraftCharacteristics.BOEING_747_8F, taskList, 206117.00, 130000);
        freightAircraft2 = new FreightAircraft("freightAircraft2",
                AircraftCharacteristics.BOEING_747_8F, taskList, 206117.00, 130);
        emergencyStatePassengerAircraft = new PassengerAircraft("emergencyStatePassengerAircraft",
                AircraftCharacteristics.AIRBUS_A320, taskList, 2702.00, 100);
        emergencyStatePassengerAircraft.declareEmergency();
    }

    @Test
    public void addAircraftTrue() {
        landingQueue.addAircraft(passengerAircraft1);
        takeoffQueue.addAircraft(passengerAircraft2);
        takeoffQueue.addAircraft(freightAircraft1);
        assertEquals(true, landingQueue.containsAircraft(passengerAircraft1));
    }


    @Test
    public void addAircraftFalse() {
        assertEquals(false, landingQueue.containsAircraft(passengerAircraft1));
    }

    @Test
    public void peekAircraftTest() {
        passengerAircraft1.declareEmergency();
        freightAircraft1.declareEmergency();
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(freightAircraft1);
        assertEquals(passengerAircraft1, landingQueue.peekAircraft());
    }

    @Test
    public void removeAircraftNull(){
        assertEquals(null, landingQueue.removeAircraft());
    }

    @Test
    public void removeAircraftNotNull(){
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        assertEquals(passengerAircraft2, landingQueue.removeAircraft());
    }

    @Test
    public void getAircraftInOrderTestTakeOffQueue(){
        takeoffQueue.addAircraft(passengerAircraft2);
        takeoffQueue.addAircraft(passengerAircraft1);
        takeoffQueue.addAircraft(freightAircraft1);
        List<Aircraft> expected = new ArrayList<>();
        expected.add(passengerAircraft2);
        expected.add(passengerAircraft1);
        expected.add(freightAircraft1);
        assertEquals(expected, takeoffQueue.getAircraftInOrder());
    }

    @Test
    public void getAircraftInOrderTestLandingQueue1(){
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(emergencyStatePassengerAircraft);
        List<Aircraft> expected = new ArrayList<>();
        expected.add(emergencyStatePassengerAircraft);
        expected.add(passengerAircraft1);
        expected.add(passengerAircraft2);
        expected.add(freightAircraft1);
        assertEquals(expected, landingQueue.getAircraftInOrder());
    }

    @Test
    public void getAircraftInOrderTestLandingQueue2(){
        landingQueue.addAircraft(passengerAircraft2);
        landingQueue.addAircraft(passengerAircraft1);
        landingQueue.addAircraft(emergencyStatePassengerAircraft);
        List<Aircraft> expected = new ArrayList<>();
        expected.add(emergencyStatePassengerAircraft);
        expected.add(passengerAircraft1);
        expected.add(passengerAircraft2);
        assertEquals(expected, landingQueue.getAircraftInOrder());

    }

    @Test
    public void containsAircraftTestFalse(){
        takeoffQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        assertEquals(false, landingQueue.containsAircraft(freightAircraft1));
    }

    @Test
    public void containsAircraftTestTrue(){
        takeoffQueue.addAircraft(freightAircraft1);
        landingQueue.addAircraft(passengerAircraft1);
        assertEquals(true, takeoffQueue.containsAircraft(freightAircraft1));
    }

}