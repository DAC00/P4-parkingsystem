package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){

        if( (ticket.getOutTime() == null) || (ticket.getOutTime().isBefore(ticket.getInTime()))){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        LocalDateTime inTime = ticket.getInTime();
        LocalDateTime outTime = ticket.getOutTime();
        Duration durationBetweenInOut = Duration.between(inTime, outTime);

        if(durationBetweenInOut.toMinutes()>=30) {
            double duration = (((double) durationBetweenInOut.toMinutes()) / 60);
            //double duration = (((double) durationBetweenInOut.toMillis()) / 3600) / 1000;
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    if(discount){
                        ticket.setPrice(roundingPrice((duration * Fare.CAR_RATE_PER_HOUR) * 0.95));
                    }else{
                        ticket.setPrice(roundingPrice(duration * Fare.CAR_RATE_PER_HOUR));
                    }
                    break;
                }
                case BIKE: {
                    if(discount){
                        ticket.setPrice(roundingPrice((duration * Fare.BIKE_RATE_PER_HOUR) * 0.95));
                    }else{
                        ticket.setPrice(roundingPrice(duration * Fare.BIKE_RATE_PER_HOUR));
                    }
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }else{
            ticket.setPrice(0);
        }
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }

    private double roundingPrice(double originalPrice){
        BigDecimal priceRounded = new BigDecimal(String.valueOf(originalPrice)).setScale(2, RoundingMode.HALF_UP);
        return priceRounded.doubleValue();
    }
}