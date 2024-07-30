package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){

        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        Instant inTime = ticket.getInTime().toInstant();
        Instant outTime = ticket.getOutTime().toInstant();
        Duration durationBetweenInOut = Duration.between(inTime, outTime);

        if(durationBetweenInOut.toMinutes()>=30) {
            double duration = ((double) durationBetweenInOut.toMinutes()) / 60;
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
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        }else{
            ticket.setPrice(0);
        }
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }

    public double roundingPrice(double originalPrice){
        BigDecimal priceRounded = new BigDecimal(String.valueOf(originalPrice)).setScale(2, RoundingMode.HALF_UP);
        return priceRounded.doubleValue();
    }
}