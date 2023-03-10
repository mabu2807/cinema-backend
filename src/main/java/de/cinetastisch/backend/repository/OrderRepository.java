package de.cinetastisch.backend.repository;

import de.cinetastisch.backend.enumeration.OrderStatus;
import de.cinetastisch.backend.model.Order;
import de.cinetastisch.backend.model.Screening;
import de.cinetastisch.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    List<Order> findAllByStatus(OrderStatus orderStatus);
    List<Order> findAllBySession(String session);

    Order findByUserAndStatusAndTicketsScreening(User user, OrderStatus status, Screening screening);
    Order findBySessionAndStatusAndTicketsScreening(String session, OrderStatus status, Screening screening);

    boolean existsByUserAndStatusAndTicketsScreening(User user, OrderStatus status, Screening screening);
    boolean existsBySessionAndStatusAndTicketsScreening(String session, OrderStatus status, Screening screening);

}
