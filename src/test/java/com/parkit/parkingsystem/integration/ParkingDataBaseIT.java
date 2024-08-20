package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private static final String carRegistrationNumber = "ABCDEF";

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(carRegistrationNumber);
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void testParkingACar(){
        when(inputReaderUtil.readSelection()).thenReturn(1);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        Ticket ticket = ticketDAO.getTicket(carRegistrationNumber);

        assertEquals(ticket.getVehicleRegNumber(), carRegistrationNumber);
        assertEquals(0.0, ticket.getPrice());
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertEquals(2,parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR));
    }

    @Test
    public void testParkingLotExit(){
        LocalDateTime inTime = LocalDateTime.now().minusHours(3).truncatedTo(ChronoUnit.SECONDS);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticketTest = new Ticket();
        ticketTest.setVehicleRegNumber(carRegistrationNumber);
        ticketTest.setParkingSpot(new ParkingSpot(1,ParkingType.CAR,false));
        ticketTest.setPrice(0);
        ticketTest.setInTime(inTime);
        ticketTest.setOutTime(null);
        ticketDAO.saveTicket(ticketTest);

        parkingService.processExitingVehicle();

        Ticket ticketFromDataBase = ticketDAO.getTicket(carRegistrationNumber);

        assertEquals(inTime,ticketFromDataBase.getInTime());
        assertNotNull(ticketFromDataBase.getOutTime());
        assertEquals(4.5, ticketFromDataBase.getPrice());
    }

    @Test
    public void  testParkingLotExitRecurringUser(){
        testParkingLotExit();

        LocalDateTime inTime = LocalDateTime.now().minusHours(1).truncatedTo(ChronoUnit.SECONDS);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        FareCalculatorService fareCalculatorService = new FareCalculatorService();

        Ticket ticketTest = new Ticket();
        ticketTest.setVehicleRegNumber(carRegistrationNumber);
        ticketTest.setParkingSpot(new ParkingSpot(1,ParkingType.CAR,false));
        ticketTest.setPrice(0);
        ticketTest.setInTime(inTime);
        ticketTest.setOutTime(null);

        ticketDAO.saveTicket(ticketTest);

        parkingService.processExitingVehicle();

        Ticket ticketFromDataBase = ticketDAO.getTicket(carRegistrationNumber);

        ticketTest.setOutTime(ticketFromDataBase.getOutTime());
        fareCalculatorService.calculateFare(ticketTest,true);

        assertEquals(ticketTest.getPrice(), ticketFromDataBase.getPrice());
        assertNotNull(ticketFromDataBase.getOutTime());
    }
}