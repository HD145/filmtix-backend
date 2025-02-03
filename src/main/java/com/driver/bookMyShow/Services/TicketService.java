package com.driver.bookMyShow.Services;

import com.driver.bookMyShow.Dtos.RequestDtos.TicketEntryDto;
import com.driver.bookMyShow.Dtos.ResponseDtos.TicketResponseDto;
import com.driver.bookMyShow.Exceptions.RequestedSeatAreNotAvailable;
import com.driver.bookMyShow.Exceptions.ShowDoesNotExists;
import com.driver.bookMyShow.Exceptions.UserDoesNotExists;
import com.driver.bookMyShow.Models.Show;
import com.driver.bookMyShow.Models.ShowSeat;
import com.driver.bookMyShow.Models.Ticket;
import com.driver.bookMyShow.Models.UserEntity;
import com.driver.bookMyShow.Repositories.*;
import com.driver.bookMyShow.Transformers.TicketTransformer;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TheaterRepository theaterRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    @Transactional
    public TicketResponseDto ticketBooking(TicketEntryDto ticketEntryDto) throws RequestedSeatAreNotAvailable, UserDoesNotExists, ShowDoesNotExists{

        Optional<Show> showOpt = showRepository.findById(ticketEntryDto.getShowId());
        if(showOpt.isEmpty()) {
            throw new ShowDoesNotExists();
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserEntity user = userRepository.findByEmailId(username);
        if(user == null) {
            throw new UserDoesNotExists();
        }
        Show show = showOpt.get();

        Boolean isSeatAvailable = isSeatAvailable(show.getShowSeatList(), ticketEntryDto.getRequestSeats());
        if(!isSeatAvailable) {
            throw new RequestedSeatAreNotAvailable();
        }

        Integer getPriceAndAssignSeats = getPriceAndAssignSeats(show.getShowSeatList(),ticketEntryDto.getRequestSeats());

        String seats = listToString(ticketEntryDto.getRequestSeats());

        Ticket ticket = new Ticket();
        ticket.setTotalTicketsPrice(getPriceAndAssignSeats);
        ticket.setBookedSeats(seats);

        ticket.setUser(user);
        ticket.setShow(show);

        ticket = ticketRepository.save(ticket);

        user.getTicketList().add(ticket);
        show.getTicketList().add(ticket);
        userRepository.save(user);
        showRepository.save(show);

//        sendMailToUser(user, show,seats);

        String mailBody = generateEmailBody(user, show, seats);
        emailService.sendMailToUser(user.getEmailId(), "Ticket Successfully Booked!", mailBody);

        return TicketTransformer.returnTicket(show, ticket);
    }

    @Transactional
    public TicketResponseDto cancelTicket(Integer ticketId){

        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);

        if(ticketOpt.isEmpty()){
            throw new Error("Ticket does not exist");
        }

        Ticket ticket = ticketOpt.get();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        UserEntity user = userRepository.findByEmailId(username);
        boolean ticketExistFlag = false;

        for(Ticket t : user.getTicketList()){
            if(t.getTicketId().equals(ticketId)){
                ticketExistFlag = true;
                break;
            }
        }

        if(!ticketExistFlag){
            throw new Error("Ticket does not exist");
        }

        user.getTicketList().remove(ticket);
        userRepository.save(user);

        String bookedSeats = ticket.getBookedSeats().trim();
        if (bookedSeats.endsWith(",")) {
            bookedSeats = bookedSeats.substring(0, bookedSeats.length() - 1);
        }
        List<String> seats = Arrays.asList(bookedSeats.split(",\\s*"));

        Show show = ticket.getShow();

        for(ShowSeat seat:show.getShowSeatList()){
            String seatNo = seat.getSeatNo();
            if(seats.contains(seatNo)){
                seat.setIsAvailable(Boolean.TRUE);
            }
        }

        showRepository.save(show);
        ticketRepository.deleteById(ticketId);

        return TicketTransformer.returnTicket(show, ticket);
    }

    private String generateEmailBody(UserEntity user, Show show, String seats) {
        return "Dear " + user.getName() + ",\n\n" +
                "Your ticket has been successfully booked!\n\n" +
                "Ticket Details:\n" +
                "Seats: " + seats + "\n" +
                "Movie: " + show.getMovie().getMovieName() + "\n" +
                "Date: " + show.getDate() + "\n" +
                "Time: " + show.getTime() + "\n" +
                "Location: " + show.getTheater().getAddress() + "\n\n" +
                "Enjoy the show!";
    }

    private Boolean isSeatAvailable(List<ShowSeat> showSeatList, List<String> requestSeats) {
        for(ShowSeat showSeat : showSeatList) {
            String seatNo = showSeat.getSeatNo();
            if(requestSeats.contains(seatNo)) {
                if(!showSeat.getIsAvailable()) {
                    return false;
                }
            }
        }
        return true;
    }

    private Integer getPriceAndAssignSeats(List<ShowSeat> showSeatList, List<String> requestSeats) {
        Integer totalAmount = 0;
        for(ShowSeat showSeat : showSeatList) {
            if(requestSeats.contains(showSeat.getSeatNo())) {
                totalAmount += showSeat.getPrice();
                showSeat.setIsAvailable(Boolean.FALSE);
            }
        }
        return totalAmount;
    }

    private String listToString(List<String> requestSeats) {
        StringBuilder sb = new StringBuilder();
        for(String s : requestSeats) {
            sb.append(s).append(",");
        }
        return sb.toString();
    }

    @Scheduled(cron = "0 0 10 * * *")
    public void sendReminderMail(){
        List<Ticket> ticket = ticketRepository.findAll();

        for(Ticket t : ticket){
            Show s = t.getShow();

            Date sqlDate = new Date(System.currentTimeMillis());
            Timestamp sqlTimestamp = new Timestamp(sqlDate.getTime());

            LocalDateTime givenDateTime = sqlTimestamp.toLocalDateTime();

            LocalTime givenTime = givenDateTime.toLocalTime();

            LocalTime currentTime = LocalTime.now();

            if (currentTime.isBefore(givenTime)) {
                UserEntity user = t.getUser();
                String mailBody = generateEmailBody(user, s, t.getBookedSeats());
                emailService.sendMailToUser(user.getEmailId(), "Ticket Successfully Booked!", mailBody);
            }
        }
    }
}
