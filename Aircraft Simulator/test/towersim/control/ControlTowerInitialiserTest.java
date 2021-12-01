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
import towersim.util.MalformedSaveException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static org.junit.Assert.*;

public class ControlTowerInitialiserTest {

    PassengerAircraft passengerAircraft1;
    PassengerAircraft passengerAircraft2;
    FreightAircraft freightAircraft1;
    FreightAircraft freightAircraft2;
    PassengerAircraft emergencyStatePassengerAircraft;
    TaskList taskList;

    @Before
    public void setup(){
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

    // Tests for a single aircraft
    @Test
    public void loadAircraftValidTestPass1(){
        try{StringJoiner joiner = new StringJoiner(System.lineSeparator());
            joiner.add("1");
            joiner.add("PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@20,TAKEOFF:2702.00:false:100");
            List<Aircraft> expected = new ArrayList<>();
            expected.add(passengerAircraft1);

            List<Aircraft> actual = ControlTowerInitialiser.loadAircraft(
                    new StringReader(joiner.toString()));
            assertEquals(expected, actual);
        }catch (Exception e){
            System.out.println("An unexpected" + e + " exception occurred");
        }
    }

    // Tests for multiple aircrafts
    @Test
    public void loadAircraftValidTestPass2(){
        try{StringJoiner joiner = new StringJoiner(System.lineSeparator());
            joiner.add("4");
            joiner.add("PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@45,TAKEOFF:2702.00:false:100");
            joiner.add("passengerAircraft2:BOEING_787:AWAY,LAND,LOAD@78,TAKEOFF:120206.00:false:200");
            joiner.add("freightAircraft1:BOEING_747_8F:AWAY,LAND,LOAD@89,TAKEOFF:206117.00:false:130000");
            joiner.add("emergencyStatePassengerAircraft:AIRBUS_A320:AWAY,LAND,LOAD@28,TAKEOFF:2702.00:true:100");
            List<Aircraft> expected = new ArrayList<>();
            expected.add(passengerAircraft1);
            expected.add(passengerAircraft2);
            expected.add(freightAircraft1);
            expected.add(emergencyStatePassengerAircraft);

            List<Aircraft> actual = ControlTowerInitialiser.loadAircraft(
                    new StringReader(joiner.toString()));
            assertEquals(expected, actual);
        }catch (Exception e){
            System.out.println("An unexpected" + e + " exception occurred");
        }
    }

    // Should throw a MalformedSaveException as the number input is invalid
    @Test
    public void loadAircraftValidTestFail1() throws IOException{
        boolean error;
        try{StringJoiner joiner = new StringJoiner(System.lineSeparator());
            error = false;
            joiner.add("aircraft");
            joiner.add("PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD,TAKEOFF:2702.00:false:100");
            ControlTowerInitialiser.loadAircraft(
                    new StringReader(joiner.toString()));
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // Checks for tests
    // Valid passenger Aircraft
    @Test
    public void readAircraftTestPass1(){
        boolean error = false;
        try{
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@20,TAKEOFF:2702.00:false:100";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertFalse(error);
    }

    // Valid freight Aircraft
    @Test
    public void readAircraftTestPass2(){
        boolean error = false;
        try{
            String aircraft = "freightAircraft1:BOEING_747_8F:AWAY,LAND,LOAD@30,TAKEOFF:206117.00:false:100";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertFalse(error);
    }
    // Less semicolons are used
    @Test
    public void readAircraftTest1Fail1(){
        boolean error = false;
        try{
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@40,TAKEOFF:2702.00:false";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);

    }
    // More semicolons are used
    @Test
    public void readAircraftTest1Fail2(){
        boolean error;
        try{
            error = false;
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@34,TAKEOFF:2702.00:false:938247n";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // Invalid AircraftCharacteristic is used
    @Test
    public void readAircraftTest1Fail3(){
        boolean error;
        try{
            error = false;
            String aircraft = "PassengerAircraft1:aircraft:AWAY,LAND,LOAD@45,TAKEOFF:2702.00:false:100";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // Invalid fuel amount
    @Test
    public void readAircraftTest1Fail4(){
        boolean error;
        try{
            error = false;
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@56,TAKEOFF:fuel:false:100";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // fuel amount is less than 0
    @Test
    public void readAircraftTest1Fail5(){
        boolean error;
        try{
            error = false;
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@45,TAKEOFF:-8:false:100";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // fuel amount is greater than aircraft capacity
    @Test
    public void readAircraftTest1Fail6(){
        boolean error;
        try{
            error = false;
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@25,TAKEOFF:300000.00:false:100";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // number of passengers is not an integer
    @Test
    public void readAircraftTest1Fail7(){
        boolean error;
        try{
            error = false;
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@45,TAKEOFF:2702.00:false:gate";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // number of passengers is less than 0
    @Test
    public void readAircraftTest1Fail8(){
        boolean error;
        try{
            error = false;
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@45,TAKEOFF:2702.00:false:-4";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // number of passengers is greater than capacity
    @Test
    public void readAircraftTest1Fail9(){
        boolean error;
        try{
            error = false;
            String aircraft = "PassengerAircraft1:AIRBUS_A320:AWAY,LAND,LOAD@39,TAKEOFF:2702.00:false:3000";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }


    // amount of cargo is not an integer
    @Test
    public void readAircraftTest1Fail10(){
        boolean error;
        try{
            error = false;
            String aircraft = "freightAircraft1:BOEING_747_8F:AWAY,LAND,LOAD@56,TAKEOFF:206117.00:false:4gate";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // amount of cargo is less than 0
    @Test
    public void readAircraftTest1Fail11(){
        boolean error;
        try{
            error = false;
            String aircraft = "freightAircraft1:BOEING_747_8F:AWAY,LAND,LOAD@54,TAKEOFF:206117.00:false:-39";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // amount of cargo is greater than capacity
    @Test
    public void readAircraftTest1Fail12(){
        boolean error;
        try{
            error = false;
            String aircraft = "freightAircraft1:BOEING_747_8F:AWAY,LAND,LOAD@54,TAKEOFF:206117.00:false:987394382";
            ControlTowerInitialiser.readAircraft(aircraft);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // read task list tests

    // passing test
    @Test
    public void readTaskListPass1() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@20,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertFalse(error);
    }

    // passing test - Why does this not pass?
    @Test
    public void readTaskListPass2() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@20,TAKEOFF,AWAY,LAND,WAIT,LOAD@50,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertFalse(error);
    }

    // Invalid task type
    @Test
    public void readTaskListFail1() {
        boolean error;
        try{
            error = false;
            String taskList = "boolean,LAND,LOAD@34,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // Loads percentage is not an integer
    @Test
    public void readTaskListFail2() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@hjn,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // A task's load percentage is less than zero.
    @Test
    public void readTaskListFail3() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@-20,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // More than 1 @ is found in the tasks
    @Test
    public void readTaskListFail4() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY@,LAND,LOAD@20,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // The taskList is invalid
    @Test
    public void readTaskListFail5() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LOAD@20,LAND,LOAD@20,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void readTaskListFail6() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,TAKEOFF,LAND,LOAD@20,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void readTaskListFail7() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@20,AWAY,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void readTaskListFail8() {
        boolean error;
        try{
            error = false;
            String taskList = "  ";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void readTaskListFail9() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // Invalid taskList
    @Test
    public void readTaskListFail10() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@20,TAKEOFF,AWAY,LAND";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    // Invalid taskList
    @Test
    public void readTaskListFail11() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@20,TAKEOFF,AWAY,LAND,";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }
    @Test
    public void readTaskListFail12() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@200,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void readTaskListFail13() {
        boolean error;
        try{
            error = false;
            String taskList = "AWAY,LAND,LOAD@20@30,TAKEOFF";
            ControlTowerInitialiser.readTaskList(taskList);
        }catch (MalformedSaveException malformedSaveException){
            error = true;
        }
        assertTrue(error);
    }
}