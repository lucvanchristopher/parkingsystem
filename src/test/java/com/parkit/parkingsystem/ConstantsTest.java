package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConstantsTest {

    @Test
    void fareTest() {
        Fare fare = new Fare();
        double result = Fare.BIKE_RATE_PER_HOUR;
        Assertions.assertEquals(1.0, result);
    }
}
