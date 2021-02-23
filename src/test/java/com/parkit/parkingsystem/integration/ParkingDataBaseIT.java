package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown(){

    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability

        Date referenceDate = new Date();
        referenceDate.setTime(referenceDate.getTime() + 1000); // now plus 1 second

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket); // assert that ticket exists and not null
        Assertions.assertFalse(ticket.getParkingSpot().isAvailable());// assert that parking spot related to this ticket should be occupied
        Assertions.assertTrue(ticket.getInTime().before(referenceDate));// assert that ticket `inTime` is before reference date
        Assertions.assertEquals(ticket.getPrice(),0);//assert that ticket price is 0
        Assertions.assertEquals(ticket.getOutTime(), null);// assert that the vehicle hasn't yet exited the parking
    }

    @Test
    public void testParkingLotExit() throws InterruptedException {
        testParkingACar();

        Thread.sleep(1000L);// wait for 1 second

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database

        Date referenceDate = new Date();
        referenceDate.setTime(referenceDate.getTime() + 1000); // now plus 1 second

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket); // assert that ticket exists and not null
        Assertions.assertTrue(ticket.getPrice() > 0); // assert that price is not 0
        Assertions.assertTrue(ticket.getInTime().before(ticket.getOutTime())); // assert that ticket `inTime` is before `outTime`
        Assertions.assertTrue(ticket.getOutTime().before(referenceDate)); // assert that ticket `outTime` is before reference date
    }

}
