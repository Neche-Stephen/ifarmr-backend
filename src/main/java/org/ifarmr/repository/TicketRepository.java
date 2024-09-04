package org.ifarmr.repository;


import org.ifarmr.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByUserIdOrderByDateCreatedDesc(Long userId);

}
