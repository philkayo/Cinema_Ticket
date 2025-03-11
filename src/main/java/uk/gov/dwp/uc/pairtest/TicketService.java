package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public interface TicketService {

    void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException;

    void validateTicketRequests(TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException;

    int calculateTotalAmount(TicketTypeRequest... ticketTypeRequests);

    int calculateSeatsToAllocate(TicketTypeRequest... ticketTypeRequests);
}

