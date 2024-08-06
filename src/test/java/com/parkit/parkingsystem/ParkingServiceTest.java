package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;
    private Ticket ticket;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    @BeforeEach
    public void setUpPerTest() {
            ticket = new Ticket();
            ticket.setInTime(Date.from(Instant.now()));
            ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
            ticket.setVehicleRegNumber("ABCDEF");
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processExitingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            parkingService.processExitingVehicle();

            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void processExitingVehicleDiscountTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            ticket.setInTime(Date.from(Instant.now().minus(10, ChronoUnit.HOURS)));
            parkingService.processExitingVehicle();

            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
            assertEquals(14.25, ticket.getPrice());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void processExitingVehicleUnableUpdateTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            parkingService.processExitingVehicle();

            verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void processIncomingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
            parkingService.processIncomingVehicle();

            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void processIncomingBikeForTheSecondTimeTest(){
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(2);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(1);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);
            when(ticketDAO.getNbTicket(anyString())).thenReturn(2);
            parkingService.processIncomingVehicle();

            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getNextParkingNumberIfAvailableTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        ParkingSpot newParkingSpot = parkingService.getNextParkingNumberIfAvailable();

        assertEquals(1, newParkingSpot.getId());
        assertEquals(ParkingType.CAR, newParkingSpot.getParkingType());
        assertTrue(newParkingSpot.isAvailable());
    }

    @Test
    public void getNextParkingNumberIfAvailableParkingNumberNotFoundTest() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
        assertNull(parkingService.getNextParkingNumberIfAvailable());
    }

    @Test
    public void getNextParkingNumberIfAvailableParkingNumberWrongArgumentTest() {
        when(inputReaderUtil.readSelection()).thenReturn(3);
        assertNull(assertDoesNotThrow(() -> parkingService.getNextParkingNumberIfAvailable()));
    }
}