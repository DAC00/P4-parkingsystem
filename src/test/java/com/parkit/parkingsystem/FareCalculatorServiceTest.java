package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private static Instant outTime;
    private Ticket ticket;

    @BeforeAll
    public static void setUp() {
        fareCalculatorService = new FareCalculatorService();
        outTime = Instant.now();
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
    }

    @Test
    public void calculateFareCar(){
        Instant inTime = outTime.minus(1,ChronoUnit.HOURS);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike(){
        Instant inTime = outTime.minus(1,ChronoUnit.HOURS);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnknownType(){
        Instant inTime = outTime.minus(1,ChronoUnit.HOURS);
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Instant inTime = outTime.plus(1,ChronoUnit.HOURS);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Instant inTime = outTime.minus(30, ChronoUnit.MINUTES);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0.50, ticket.getPrice() );
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Instant inTime = outTime.minus(30, ChronoUnit.MINUTES);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0.75, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Instant inTime = outTime.minus(1, ChronoUnit.DAYS);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(36, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime(){
        Instant inTime = outTime.minus(29, ChronoUnit.MINUTES);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime(){
        Instant inTime = outTime.minus(29, ChronoUnit.MINUTES);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithDiscount(){
        Instant inTime = outTime.minus(2, ChronoUnit.DAYS);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket,true);
        assertEquals(68.4, ticket.getPrice());
    }

    @Test
    public void calculateFareBikeWithDiscount(){
        Instant inTime = outTime.minus(2, ChronoUnit.DAYS);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(Date.from(inTime));
        ticket.setOutTime(Date.from(outTime));
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);
        assertEquals(45.6, ticket.getPrice());
    }
}