package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    /**
     * Calculate the parking fare
     *
     * @param ticket   {{@link Ticket}} The ticket from which we should compute the fare
     * @param discount {double} a discount rate
     */
    public void calculateFare(Ticket ticket, double discount) {
        if (discount > 1 || discount < 0) {
            throw new IllegalArgumentException("Invalid discount rate");
        }
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
        double duration = (double) (ticket.getOutTime().getTime() - ticket.getInTime().getTime()) / (1000 * 60);// in minutes

        // Under-30mn should be free (other criterion of fare)
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
        double perHourPrice = 0;
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                perHourPrice = Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                perHourPrice = Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }

        double price = duration * perHourPrice * (1 - discount);
        ticket.setPrice(price);
    }

    /**
     * Calculate the parking fare
     *
     * @param ticket {{@link Ticket}} The ticket from which we should compute the fare
     */
    public void calculateFare(Ticket ticket) {
        // Call the above function with a default discount of 0 (no discount)
        this.calculateFare(ticket, 0);
    }
}