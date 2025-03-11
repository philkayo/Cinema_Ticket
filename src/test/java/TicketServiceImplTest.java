import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketServiceImplTest {

    @Mock
    private TicketPaymentService paymentService;

    @Mock
    private SeatReservationService reservationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPurchaseTickets_ValidRequest() throws InvalidPurchaseException {
        // Arrange
        Long accountId = 1L;
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        // Act
        ticketService.purchaseTickets(accountId, requests);

        // Assert
        verify(paymentService, times(1)).makePayment(accountId, 65); // 2 adults * 25 + 1 child * 15 = 65
        verify(reservationService, times(1)).reserveSeat(accountId, 3); // 2 adults + 1 child = 3 seats
    }

    @Test
    void testPurchaseTickets_InvalidAccountId() {
        // Arrange
        Long invalidAccountId = 0L;
        TicketTypeRequest[] requests = { new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1) };

        // Act & Assert
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(invalidAccountId, requests));
    }

    @Test
    void testPurchaseTickets_ExceedMaxTickets() {
        // Arrange
        Long accountId = 1L;
        TicketTypeRequest[] requests = { new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26) };

        // Act & Assert
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, requests));
    }

    @Test
    void testPurchaseTickets_ChildOrInfantWithoutAdult() {
        // Arrange
        Long accountId = 1L;
        TicketTypeRequest[] requests = { new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1) };

        // Act & Assert
        assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, requests));
    }

    @Test
    void testCalculateTotalAmount() {
        // Arrange
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        // Act
        int totalAmount = ticketService.calculateTotalAmount(requests);

        // Assert
        assertEquals(65, totalAmount); // 2 adults * 25 + 1 child * 15 + 1 infant * 0 = 65
    }

    @Test
    void testCalculateSeatsToAllocate() {
        // Arrange
        TicketTypeRequest[] requests = {
                new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2),
                new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1),
                new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1)
        };

        // Act
        int seatsToAllocate = ticketService.calculateSeatsToAllocate(requests);

        // Assert
        assertEquals(3, seatsToAllocate); // 2 adults + 1 child = 3 seats (infants don't count)
    }
}


