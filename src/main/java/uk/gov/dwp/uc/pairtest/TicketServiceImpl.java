package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    private static final int MAX_TICKETS = 25;
    private static final int INFANT_PRICE = 0;
    private static final int CHILD_PRICE = 15;
    private static final int ADULT_PRICE = 25;

    private final TicketPaymentService paymentService;
    private final SeatReservationService reservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService reservationService) {
        this.paymentService = paymentService;
        this.reservationService = reservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (accountId == null || accountId <= 0) {
            throw new InvalidPurchaseException();
        }

        validateTicketRequests(ticketTypeRequests);

        int totalAmount = calculateTotalAmount(ticketTypeRequests);
        int seatsToAllocate = calculateSeatsToAllocate(ticketTypeRequests);

        paymentService.makePayment(accountId, totalAmount);
        reservationService.reserveSeat(accountId, seatsToAllocate);
    }

    @Override
    public void validateTicketRequests(TicketTypeRequest... ticketTypeRequests) {
        if (ticketTypeRequests == null || ticketTypeRequests.length == 0) {
            throw new InvalidPurchaseException();
        }

        int totalTickets = 0;
        boolean hasAdult = false;
        boolean hasChildOrInfant = false;

        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request == null || request.getNoOfTickets() <= 0) {
                throw new InvalidPurchaseException();
            }
            totalTickets += request.getNoOfTickets();
            if (request.getTicketType() == TicketTypeRequest.Type.ADULT) {
                hasAdult = true;
            } else {
                hasChildOrInfant = true;
            }
        }

        if (totalTickets > MAX_TICKETS) {
            throw new InvalidPurchaseException();
        }

        if (hasChildOrInfant && !hasAdult) {
            throw new InvalidPurchaseException();
        }
    }

    @Override
    public int calculateTotalAmount(TicketTypeRequest... ticketTypeRequests) {
        int totalAmount = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            switch (request.getTicketType()) {
                case INFANT -> totalAmount += INFANT_PRICE * request.getNoOfTickets();
                case CHILD -> totalAmount += CHILD_PRICE * request.getNoOfTickets();
                case ADULT -> totalAmount += ADULT_PRICE * request.getNoOfTickets();
            }
        }
        return totalAmount;
    }

    @Override
    public int calculateSeatsToAllocate(TicketTypeRequest... ticketTypeRequests) {
        int seatsToAllocate = 0;
        for (TicketTypeRequest request : ticketTypeRequests) {
            if (request.getTicketType() != TicketTypeRequest.Type.INFANT) {
                seatsToAllocate += request.getNoOfTickets();
            }
        }
        return seatsToAllocate;
    }
}


