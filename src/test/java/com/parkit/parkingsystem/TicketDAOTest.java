package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TicketDAOTest {
    private static final DataBaseConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static final String vehicleRegistrationNumber = "ABCD";
    private static final TicketDAO ticketDAO = new TicketDAO();

    @BeforeAll
    private static void setUp() throws Exception {
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        // Clean ticket data in DB
        Connection con = dataBaseTestConfig.getConnection();
        Statement ps = con.createStatement();
        ps.executeUpdate("TRUNCATE TABLE ticket");
    }

    @Test
    public void testSaveUpdateGetTicket () {
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(vehicleRegistrationNumber);
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, true));
        ticket.setPrice(1.0);

        ticket.setInTime(LocalDateTime.now());
        ticket.setOutTime(null);

        // Should persist ticket in DB
        assertTrue(ticketDAO.saveTicket(ticket));

        // Should return ticket for the given vehicle registration number
        Ticket fetchedTicket = ticketDAO.getTicket(vehicleRegistrationNumber);
        assertEquals(fetchedTicket.getPrice(), ticket.getPrice());
        assertEquals(fetchedTicket.getParkingSpot().getId(), ticket.getParkingSpot().getId());
        assertEquals(fetchedTicket.getParkingSpot().getParkingType(), ticket.getParkingSpot().getParkingType());

        // MySQL doesn't support milli, micro, nano seconds as `DateTime` for now
        LocalDateTime inTimeWithoutMillis = ticket.getInTime().truncatedTo(ChronoUnit.SECONDS).plus(Math.round(ticket.getInTime().getNano()/1e9), ChronoUnit.SECONDS);
        assertEquals(fetchedTicket.getInTime(), inTimeWithoutMillis);

        assertEquals(fetchedTicket.getOutTime(), ticket.getOutTime());

        // Let's unitary update the fetched ticket
        fetchedTicket.setOutTime(LocalDateTime.now().plus(1, ChronoUnit.HOURS));
        fetchedTicket.setPrice(10.0);
        // Should have updated the ticket in DB
        assertTrue(ticketDAO.updateTicket(fetchedTicket));

        Ticket updatedFetchedTicket = ticketDAO.getTicket(vehicleRegistrationNumber);
        assertEquals(updatedFetchedTicket.getPrice(), fetchedTicket.getPrice());

        // MySQL doesn't support milli, micro, nano seconds as `DateTime` for now
        LocalDateTime outTimeWithoutMillis = fetchedTicket.getOutTime().truncatedTo(ChronoUnit.SECONDS).plus(Math.round(fetchedTicket.getOutTime().getNano()/1e9), ChronoUnit.SECONDS);
        assertEquals(updatedFetchedTicket.getOutTime(), outTimeWithoutMillis);
    }

    @Test
    public void testGetTicketsCount(){
        int expectedCount = 7;
        for (int i = 0; i < expectedCount; i++) {
            Ticket ticket = new Ticket();
            ticket.setVehicleRegNumber(vehicleRegistrationNumber);
            ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, true));
            ticket.setPrice(1.0);

            ticket.setInTime(LocalDateTime.now());
            ticket.setOutTime(null);

            ticketDAO.dataBaseConfig = dataBaseTestConfig;
            // Should persist ticket in DB
            assertTrue(ticketDAO.saveTicket(ticket));
        }
        assertEquals(ticketDAO.getTicketsCount(vehicleRegistrationNumber), expectedCount);
    }
}
