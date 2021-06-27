package ticket;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Price_table")
public class Price {

    @Id
    private Long ticketId;
    private Integer price;
    private Integer discountedPrice;

    @PostPersist
    public void onPostPersist(){
        System.out.println("Price PostPersist .......... ");

        Calculated calculated = new Calculated();
        BeanUtils.copyProperties(this, calculated);
        calculated.publishAfterCommit();

    }

    @PostUpdate
    public void onPostUpdate(){
        System.out.println("Price PostUpdate .......... ");

        Calculated calculated = new Calculated();
        BeanUtils.copyProperties(this, calculated);
        calculated.publishAfterCommit();

    }



    public Integer getDiscountedPrice() {
        return discountedPrice;
    }

    public void setDiscountedPrice(Integer discountedPrice) {
        this.discountedPrice = discountedPrice;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }
    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

}
