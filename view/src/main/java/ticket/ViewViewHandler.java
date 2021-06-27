package ticket;

import ticket.config.kafka.KafkaProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ViewViewHandler {


    @Autowired
    private ViewRepository viewRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void whenRegistered_then_CREATE_1 (@Payload Registered registered) {
        try {

            if (!registered.validate()) return;

            // view 객체 생성
            View view = new View();
            // view 객체에 이벤트의 Value 를 set 함
           view.setTicketId(registered.getTicketId());
           view.setStatus(registered.getStatus());
           view.setStarttime(registered.getStarttime());
           view.setEndtime(registered.getEndtime());
            // view 레파지 토리에 save
            viewRepository.save(view);

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenCalculated_then_UPDATE_1(@Payload Calculated calculated) {
        try {
            if (!calculated.validate()) return;
                // view 객체 조회
            Optional<View> viewOptional = viewRepository.findById(calculated.getTicketId());

            if( viewOptional.isPresent()) {
                 View view = viewOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 view.setPrice(calculated.getPrice());
                 view.setDiscountedPrice(calculated.getDiscountedPrice());
                // view 레파지 토리에 save
                 viewRepository.save(view);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @StreamListener(KafkaProcessor.INPUT)
    public void whenReserved_then_UPDATE_2(@Payload Reserved reserved) {
        try {
            if (!reserved.validate()) return;
                // view 객체 조회
            Optional<View> viewOptional = viewRepository.findById(reserved.getTicketId());

            if( viewOptional.isPresent()) {
                 View view = viewOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 view.setUserId(reserved.getUserId());
                 view.setUserGrade(reserved.getUserGrade());
                 view.setReservationId(reserved.getReservationId());
                 view.setReservationStatus(reserved.getStatus());
                // view 레파지 토리에 save
                 viewRepository.save(view);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void whenStatusUpdated_then_UPDATE_3(@Payload StatusUpdated statusUpdated) {
        try {
            if (!statusUpdated.validate()) return;
                // view 객체 조회
            Optional<View> viewOptional = viewRepository.findById(statusUpdated.getTicketId());

            if( viewOptional.isPresent()) {
                 View view = viewOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 view.setStatus(statusUpdated.getStatus());
                // view 레파지 토리에 save
                 viewRepository.save(view);
                }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @StreamListener(KafkaProcessor.INPUT)
    public void whenCancelled_then_UPDATE_4(@Payload Cancelled cancelled) {
        try {
            if (!cancelled.validate()) return;
                // view 객체 조회
            Optional<View> viewOptional = viewRepository.findById(cancelled.getTicketId());

            if( viewOptional.isPresent()) {
                 View view = viewOptional.get();
            // view 객체에 이벤트의 eventDirectValue 를 set 함
                 view.setReservationStatus(cancelled.getStatus());
                // view 레파지 토리에 save
                 viewRepository.save(view);
                }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

}

