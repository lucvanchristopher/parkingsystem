package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
        double duration = (double) (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (1000 * 60);// in minutes

        // Under-30mn should be free
        if (duration <= 30) {
            ticket.setPrice(0);
            return;
        }

        // Hourly rate
        if (duration < 60) {
            duration = duration / 60.0;
        } else {
            duration = Math.round(duration / 60);
        }
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                break;
            }
            case BIKE: {
                ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}