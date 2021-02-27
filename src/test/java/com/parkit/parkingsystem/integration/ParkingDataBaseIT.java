package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {


    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
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
    private static void tearDown() {

    }

    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability

        Date referenceDate = new Date();
        referenceDate.setTime(referenceDate.getTime() + 1000); // now plus 1 second
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket);// assert that ticket exists and not null
        Assertions.assertFalse(ticket.getParkingSpot().isAvailable()); // assert that parking spot related to this ticket shold be occupied
        Assertions.assertTrue(ticket.getInTime().before(referenceDate));// assert that ticket `inTime` is before reference date
        Assertions.assertEquals(ticket.getPrice(), 0); // assert that ticket price is 0
        Assertions.assertEquals(ticket.getOutTime(), null);// assert that ticket price is 0
    }

    @Test
    public void testParkingLotExit() throws InterruptedException {
        testParkingACar();

        Thread.sleep(1000L);// wait for 1 second
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database

        Date referenceDate = new Date();
        referenceDate.setTime(referenceDate.getTime() + 1000);// now plus 1 second

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket);// assert that ticket exists and not null
        Assertions.assertEquals(ticket.getPrice(), 0);// assert that price is 0 (under 30mn case)
        Assertions.assertTrue(ticket.getInTime().before(ticket.getOutTime()));// assert that ticket `inTime` is before `outTime`
        Assertions.assertTrue(ticket.getOutTime().before(referenceDate));// assert that ticket `outTime` is before reference date
    }

    @Test
    public void testRecurringParkingLotExit() throws InterruptedException {
        // Create fake data in database
        int count = Fare.RECURRING_COUNT;
        for (int i = 0; i < count; i++) {
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

            Ticket ticket = new Ticket();
            ticket.setParkingSpot(parkingSpot);

            Date inTime = new Date();
            inTime.setTime(System.currentTimeMillis() - ((count - i) * 60 * 1000));
            ticket.setInTime(inTime);

            Date outTime = new Date();
            outTime.setTime(inTime.getTime() + (30 * 60 * 1000));// outTime is 30mn after `inTime`
            ticket.setInTime(new Date());

            ticket.setPrice(0);

            // This is the key to check the recurrence
            ticket.setVehicleRegNumber("ABCDEF");

            ticketDAO.saveTicket(ticket);
        }
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        ParkingService parkingServiceSpy = spy(parkingService);
        Date referenceDate = new Date();
        //fake an inTime current date
        when(parkingServiceSpy.currentDate()).thenReturn(referenceDate);

        // Process a parking-in of a vehicle
        parkingServiceSpy.processIncomingVehicle();

        //fake an outTime of inTime + 2 hours
        referenceDate.setTime(referenceDate.getTime() + (2 * 60 * 60 * 1000));
        when(parkingServiceSpy.currentDate()).thenReturn(referenceDate);
        parkingServiceSpy.processExitingVehicle();

        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        Assertions.assertNotNull(ticket);// assert that ticket exists and not null
        Assertions.assertEquals(ticket.getPrice(),
                2 * Fare.CAR_RATE_PER_HOUR * (1 - Fare.RECURRING_DISCOUNT_RATE)); // assert price to have a discount

    }

}