package ticket;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name = "Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long reservationId;
    private String userId;
    private Long ticketId;
    private String status;
    private String userGrade;
    private Date updatedDate;

    @PrePersist
    public void onPrePersist() throws Exception {
        // 예약을 저장하기 전에 티켓 서비를 호출하여, 티켓의 상태를 확인한다.
        boolean result = ReservationApplication.applicationContext.getBean(ticket.external.TicketService.class)
                .checkAndBook(this.getTicketId());

        if (result) {
            // 티켓이 예약 가능 상태이면, 예약에 저장할 값을 set 한다.
            this.status = "RESERVED";
            this.updatedDate = new Date(System.currentTimeMillis());
        } else {
            // 티켓이 예약 불가한 상태이면, 예약을 저장하지 않고 끝낸다.
            throw new Exception(" 티켓을 예약할 수 없습니다.  티켓ID : " + this.getTicketId());
        }
    }

    @PostPersist
    public void onPostPersist() {
        // 예약이 저장된 후, Reserved 이벤트를 Pub 한다.
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.publishAfterCommit();
    }

    @PreUpdate
    public void onPreUpdate() {
        // 예약이 수정 저장되기 전에 updatedDate 값을 현재 시간으로 설정한다.
        this.updatedDate = new Date(System.currentTimeMillis());
    }

    @PostUpdate
    public void onPostUpdate() {
        // 예약이 Cancelled로 수정된 다음, Cancelled 이벤트를 Pub 한다.
        Cancelled cancelled = new Cancelled();
        BeanUtils.copyProperties(this, cancelled);
        cancelled.publishAfterCommit();
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserGrade() {
        return userGrade;
    }

    public void setUserGrade(String userGrade) {
        this.userGrade = userGrade;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

}
