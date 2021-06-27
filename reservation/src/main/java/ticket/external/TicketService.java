
package ticket.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignClient(name="ticket", url="${api.url.ticketservice}")
//@FeignClient(name="ticket", url="http://ticket:8080")
@FeignClient(name="ticket", url="http://localhost:8088")
public interface TicketService {

    @RequestMapping(method= RequestMethod.GET, path="/tickets/checkAndBook")
    public boolean checkAndBook(@RequestParam("ticketId") Long ticketId);

}

