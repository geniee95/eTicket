package ticket;

import javax.persistence.*;
import org.springframework.beans.BeanUtils;
import java.util.Date;

@Entity
@Table(name="Reservation_table")
public class Reservation {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long reservationId;
    private String userId;
    private Long ticketId;
    private String status;
    private String userGrade;
    private Date updatedDate;

    @PrePersist
    public void onPrePersist() throws Exception {

        System.out.println("######################  Reservation 1. PrePsersist");
        System.out.println("######################" + this.getTicketId());
        // 예약 전에 티켓의 상태를 확인하고, 
        boolean result = ReservationApplication.applicationContext.getBean(ticket.external.TicketService.class)
        .checkAndBook(this.getTicketId());
        System.out.println("ticket.checkAndBook --------  " + result);
        if (result) {
            //ticket이 예약 가능 상태이므로, reservation에 저장할 값을 set 한다.  
            this.status = "RESERVED";
            this.updatedDate = new Date(System.currentTimeMillis());
            System.out.println("onPrePersist .... ");
        } else {
            throw new Exception(" 티켓을 예약할 수 없습니다.  티켓ID : " + this.getTicketId());
        }
        
    }

    @PostPersist
    public void onPostPersist(){
        // 예약이 가능하므로, Reservation 저장 후 Reserved Pub
        System.out.println("######################  Reservation 2. PostPersist");
        Reserved reserved = new Reserved();
        BeanUtils.copyProperties(this, reserved);
        reserved.publishAfterCommit();
        System.out.println("######################  Reservation 2-1. reserved  Pub");

    }

    @PreUpdate
    public void onPreUpdate() {
        System.out.println("######################  Reservation 4. onPostUpdate");
        //날짜를 업데이트 한다. 
        this.updatedDate = new Date(System.currentTimeMillis());
    }

    @PostUpdate
    public void onPostUpdate() {
        System.out.println("######################  Reservation 4. onPostUpdate");
        //Cancelled 이벤트를 pub 한다. 
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
