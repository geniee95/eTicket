
package ticket;

import java.util.Date;

public class Cancelled extends AbstractEvent {

    private String userId;
    private String status;
    private Long reservationId;
    private Long ticketId;
    private String userGrade;
    private Date reservetime;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public Long getReserveId() {
        return reservationId;
    }

    public void setReserveId(Long reservationId) {
        this.reservationId = reservationId;
    }
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }
    public String getUserGrade() {
        return userGrade;
    }

    public void setUserGrade(String userGrade) {
        this.userGrade = userGrade;
    }
    public Date getReservetime() {
        return reservetime;
    }

    public void setReservetime(Date reservetime) {
        this.reservetime = reservetime;
    }
}

