package ticket;

import java.util.Date;

public class Registered extends AbstractEvent {

    private Long ticketId;
    private String status;
    private Date starttime;
    private Date endtime;
    
    public Long getTicketId() {
        return ticketId;
    }


    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Registered(){
        super();
    }

   
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }
    public Date getEndtime() {
        return endtime;
    }

    public void setEndtime(Date endtime) {
        this.endtime = endtime;
    }
}
