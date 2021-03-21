package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;


import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParkingSpotDAOTest {
    private static final DataBaseConfig  dataBaseTestConfig = new DataBaseConfig();

    @BeforeAll
    private static void setUp() throws Exception {

    }
    @BeforeEach
    private void setUpPerTest() throws Exception {
        Connection con = dataBaseTestConfig.getConnection();
        Statement ps = con.createStatement();
        // Reset all parking spots availability status
        ps.executeUpdate("UPDATE parking SET available=true WHERE PARKING_NUMBER >= 1");
    }

    @Test
    public void testGetNextAvailableSlotForBike() {
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        int parkingSpotIndex = parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE);
        assertEquals(parkingSpotIndex, 4);
    }

    @Test
    public void testGetNextAvailableSlotForCar() {
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        int parkingSpotIndex = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertEquals(parkingSpotIndex, 1);
    }

    @Test
    public void testUpdateParking() {
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ParkingSpot parkingSpot = new ParkingSpot(4, ParkingType.BIKE, false);
        boolean isInserted = parkingSpotDAO.updateParking(parkingSpot);
        assertEquals(isInserted, true);
    }
    @Test
    public void testUpdateParkingWithUnknownData() {
        ParkingSpotDAO parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ParkingSpot parkingSpot = new ParkingSpot(7, ParkingType.BIKE, false);
        boolean isInserted = parkingSpotDAO.updateParking(parkingSpot);
        assertEquals(isInserted, false);
    }
}
