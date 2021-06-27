package ticket;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="prices", path="prices")
public interface PriceRepository extends PagingAndSortingRepository<Price, Long>{
    
    Price findByTicketId(Long TicketId);

}
