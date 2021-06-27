package ticket;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

 @RestController
 public class TicketController {

        @Autowired
        TicketRepository ticketRepository;

@RequestMapping(value = "/tickets/checkAndBook",
        method = RequestMethod.GET,
        produces = "application/json;charset=UTF-8")
public boolean checkAndBook(HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        System.out.println("##### /ticket/checkAndBook  called #####");
        
        //서킷브레이커 시간지연
        //Thread.currentThread();
        //Thread.sleep((long) (400 + Math.random() * 220));
        
       
        boolean result = true;  
        Long ticketId = Long.valueOf(request.getParameter("ticketId"));
        System.out.println("##### ticketId  "+ ticketId);

        Ticket ticket = ticketRepository.findByTicketId(ticketId);
        Date currDate = new Date(System.currentTimeMillis());
        Date ticketEndDate = ticket.getEndtime();
        int compare = currDate.compareTo(ticketEndDate);
        System.out.println("currDate :: " + currDate);
        System.out.println("ticketEndDate :: " + ticketEndDate);
        System.out.println("compare :: " + compare);

        String status = "";
        status = ticket.getStatus();

        if (compare > 0) {
             result = false;
             System.out.println("사용 기간이 만료된 티켓을 예약할 수 없습니다.  티켓번호 : " + ticketId); 
             return result;
        } else if(!isStringEmpty(status) && status.equals("예약됨")) {
                result = false;
                System.out.println("이미 예약된 티켓은 중복 예약할 수 없습니다. 티켓번호 : " + ticketId);       
                return result;
        } 
        ticket.setStatus("예약됨");
        ticketRepository.save(ticket);
        status = ticket.getStatus();

        return result;     
        }

        private boolean isStringEmpty(String str) {
                return str == null || str.isEmpty();
        }

 }
