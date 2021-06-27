package ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import ticket.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler {
    @Autowired
    TicketRepository ticketRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelled_Cancel(@Payload Cancelled cancelled) {

        if (cancelled.validate()) {
            //예약을 취소한 경우, 티켓의 상태를 '예약가능'으로 변경
            Ticket ticket = ticketRepository.findByTicketId(Long.valueOf(cancelled.getTicketId()));
            ticket.setStatus("예약가능");
            ticketRepository.save(ticket);
        }

    }

}
