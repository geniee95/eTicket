package ticket;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ViewRepository extends CrudRepository<View, Long> {

    List<View> findByTicketId(Long ticketId);

}