package de.cinetastisch.backend.service;

import de.cinetastisch.backend.dto.ReservationRequestDto;
import de.cinetastisch.backend.dto.ReservationResponseDto;
import de.cinetastisch.backend.enumeration.OrderStatus;
import de.cinetastisch.backend.exception.ResourceAlreadyOccupiedException;
import de.cinetastisch.backend.exception.ResourceNotFoundException;
import de.cinetastisch.backend.mapper.ReservationMapper;
import de.cinetastisch.backend.model.*;
import de.cinetastisch.backend.repository.OrderRepository;
import de.cinetastisch.backend.repository.ReservationRepository;
import de.cinetastisch.backend.repository.TicketRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final SeatService seatService;
    private final ScreeningService screeningService;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final ReservationMapper mapper;

    @Scheduled(fixedRate = 10000)
    @Transactional
    public void deleteExpiredReservations(){
        if(reservationRepository.existsByExpiresAtIsLessThanEqual(LocalDateTime.now())){
//            List<Reservation> reservationsToDelete = reservationRepository.findAllByExpireAtLessThanEqual(LocalDateTime.now());

//            List<Reservation> uniqueReservationsToDelete = reservationsToDelete.stream()
//                                                                               .collect(
//                                                                                       collectingAndThen(
//                                                                                               toCollection( () -> new TreeSet<>(comparingLong(Order::getId)) ),
//                                                                                               ArrayList::new
//                                                                                       )
//                                                                               );


            List<Order> ordersOfReservations = reservationRepository.getAllDistinctOrdersOfReservationsToDelete(LocalDateTime.now());

            for (Order o : ordersOfReservations){
                if(o.getOrderStatus() != OrderStatus.PAID){
                    System.out.println(o);
                    o.setOrderStatus(OrderStatus.CANCELLED);
                    orderRepository.save(o);
                }
                reservationRepository.deleteAllByOrder(o);
            }
//            reservationRepository.deleteByExpireAtIsLessThanEqual(LocalDateTime.now());

//            orderService.cancelOrder();
        }
    }

    public void deleteReservation(Long id){
        reservationRepository.delete(getReservation(id));
    }

    public List<ReservationResponseDto> getAllReservations(Long userId, Long screeningId){
        if (userId != null && screeningId != null){
            return mapper.entityToDto(reservationRepository.findAllByUserAndScreening(userService.getUser(userId), screeningService.getScreening(screeningId)));
        } else if (userId != null){
            return mapper.entityToDto(reservationRepository.findAllByUser(userService.getUser(userId)));
        } else if(screeningId != null){
            return mapper.entityToDto(reservationRepository.findAllByScreening(screeningService.getScreening(screeningId)));
        }
        return mapper.entityToDto(reservationRepository.findAll());
    }

    public Reservation getReservation(Long id){
        return reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    public Reservation getReservation(User user, Screening screening, Seat seat){
        return reservationRepository.findByUserAndScreeningAndSeat(user, screening, seat).orElseThrow(() -> new ResourceNotFoundException("Reservation not found"));
    }

    public Reservation addReservation(ReservationRequestDto request){
        Reservation reservation = Reservation.builder()
                                             .user(userService.getUser(request.userId()))
                                             .screening(screeningService.getScreening(request.screeningId()))
                                             .seat(seatService.getSeat(request.seatId()))
                                             .expiresAt(LocalDateTime.now().plusSeconds(20))
                                             .build();
        deleteExpiredReservations();

        // Check for reservations
        if((reservationRepository.existsByScreeningAndSeatAndExpiresAtIsGreaterThanEqual(reservation.getScreening(), reservation.getSeat(), LocalDateTime.now()))
                || ticketRepository.existsByScreeningAndSeat(reservation.getScreening(), reservation.getSeat())){
            throw new ResourceAlreadyOccupiedException("Seat is currently reserved");
        }

        if(!reservationRepository.existsByUserAndScreening(reservation.getUser(), reservation.getScreening())){
            Order order = orderRepository.save(new Order(reservation.getUser()));
            reservation.setOrder(order);
        } else {
            Reservation firstReservation = reservationRepository.findAllByUserAndScreening(reservation.getUser(), reservation.getScreening()).get(0);
            reservation.setOrder(firstReservation.getOrder());
            reservation.setExpiresAt(firstReservation.getExpiresAt());
        }
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAllReservations(Order order) {
        return reservationRepository.findAllByOrder(order);
    }
}
