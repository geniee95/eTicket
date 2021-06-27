package ticket;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import ticket.config.kafka.KafkaProcessor;

@Service
public class PolicyHandler {
    @Autowired
    PriceRepository priceRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverRegistered_ApplyPolicy(@Payload Registered registered) {
        // 티켓이 최초 등록 되었을 때 요일별 가격 할인율 적용
        if (registered.isMe()) {

            // Sample Logic //
            System.out.println("\n\n##### listener ApplyPolicy : Registered  " + registered.toJson() + "\n\n");

            Price price = new Price();
            price.setTicketId(registered.getTicketId());
            price.setPrice(new Integer(10000));
            price.setDiscountedPrice(new Integer(10000));

            // starttime이 수/목인 경우는 20% 할인을 적용한다.
            Date startTime = (Date) registered.getStarttime();
            SimpleDateFormat dateFormat = new SimpleDateFormat("E"); // 시간을 가지고 온다.
            String weekday = dateFormat.format(startTime);

            System.out.println("weekday:::: " + weekday);

            if (weekday.equals("수") || weekday.equals("목")) {
                Integer originPrice = price.getPrice();
                price.setDiscountedPrice(originPrice - (originPrice * 20 / 100));
                System.out.println("discounted ........." + price.getDiscountedPrice());
            }
            System.out.println("priceRepository before   save ........... " + price.getDiscountedPrice());
            priceRepository.save(price);
        }
    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverReserved_ApplyPolicy(@Payload Reserved reserved) {
        // 티켓을 사용자가 예약 했을 때, VIP이면 1000원 추가 할인
        if (reserved.isMe()) {
            // Get Methods
            // 예약을 하고, VIP 일때만 업데이트 됨

            // Sample Logic //
            System.out.println("\n\n##### listener ApplyPolicy : Reserved  " + reserved.toJson() + "\n\n");

            String grade = reserved.getUserGrade();
            if (!isStringEmpty(grade) && grade.equals("VIP")) {
                // VIP는 1000원 추가 할인함
                Price price = priceRepository.findByTicketId(reserved.getTicketId());
                Integer discountedPrice = price.getDiscountedPrice() - new Integer(1000);
                price.setDiscountedPrice(discountedPrice);
                priceRepository.save(price);
            }
        }

    }

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverCancelled_ApplyPolicy(@Payload Cancelled cancelled) {
        // 티켓을 사용자가 취소 했을 때, VIP이면 1000원 추가 할인했던 것을 원복한다. 
        if (cancelled.isMe()) {
            // Get Methods
            // 예약을 하고, VIP 일때만 업데이트 됨

            // Sample Logic //
            System.out.println("\n\n##### listener ApplyPolicy : Cancelled  " + cancelled.toJson() + "\n\n");

            String grade = cancelled.getUserGrade();
            if (!isStringEmpty(grade) && grade.equals("VIP")) {
                // VIP는 1000원 추가 할인함
                Price price = priceRepository.findByTicketId(cancelled.getTicketId());
                Integer discountedPrice = price.getDiscountedPrice() + new Integer(1000);
                price.setDiscountedPrice(discountedPrice);
                priceRepository.save(price);
            }
        }

    }

    private boolean isStringEmpty(String str) {
        return str == null || str.isEmpty();
    }

}
