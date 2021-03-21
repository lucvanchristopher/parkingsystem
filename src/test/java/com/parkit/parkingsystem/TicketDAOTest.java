package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

public class TicketDAOTest {
    private static final DataBaseConfig dataBaseTestConfig = new DataBaseConfig();

    @BeforeAll
    private static void setUp() throws Exception {

    }
    @BeforeEach
    private void setUpPerTest() throws Exception {
        Connection con = dataBaseTestConfig.getConnection();
        Statement ps = con.createStatement();
        System.out.println("****** Before-Each *********");
        System.out.println(ps.executeUpdate("insert into ticket(PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME) values(1,2,3,40000,50000)"));
    }

    @Test
    public void testSaveTicket () {

    }
    @Test
    public void testGetTicket(){

    }

    @Test
    public void testUpdateTicket(){

    }

    @Test
    public void testGetTicketsCount(){

    }
}
