package ticket;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="View_table")
public class View {

        @Id
        private Long ticketId;
        private String status;
        private Date starttime;
        private Date endtime;
        private Integer price;
        private Long reservationId;
        private String userId;
        private String userGrade;
        private Integer discountedPrice;
        private String reservationStatus;


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
        public String getUserGrade() {
            return userGrade;
        }

        public void setUserGrade(String userGrade) {
            this.userGrade = userGrade;
        }
        public Integer getDiscountedPrice() {
            return discountedPrice;
        }

        public void setDiscountedPrice(Integer discountedPrice) {
            this.discountedPrice = discountedPrice;
        }
        public String getReservationStatus() {
            return reservationStatus;
        }

        public void setReservationStatus(String reservationStatus) {
            this.reservationStatus = reservationStatus;
        }

        public Integer getPrice() {
            return price;
        }

        public void setPrice(Integer price) {
            this.price = price;
        }

}
