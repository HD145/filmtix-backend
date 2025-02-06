package com.driver.bookMyShow.Services;

import com.driver.bookMyShow.Config.JwtConfig;
import com.driver.bookMyShow.Config.UserDetailsImpl;
import com.driver.bookMyShow.Dtos.RequestDtos.UserEntryDto;
import com.driver.bookMyShow.Dtos.RequestDtos.UserLoginDto;
import com.driver.bookMyShow.Dtos.ResponseDtos.TicketResponseDto;
import com.driver.bookMyShow.Exceptions.UserAlreadyExistsWithEmail;
import com.driver.bookMyShow.Exceptions.UserDoesNotExists;
import com.driver.bookMyShow.Models.Ticket;
import com.driver.bookMyShow.Models.UserEntity;
import com.driver.bookMyShow.Repositories.UserRepository;
import com.driver.bookMyShow.Transformers.TicketTransformer;
import com.driver.bookMyShow.Transformers.UserTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserDetailsImpl userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String signupUser(UserEntryDto userEntryDto) throws UserAlreadyExistsWithEmail{
        if(userRepository.findByEmailId(userEntryDto.getEmailId()) != null) {
            throw new UserAlreadyExistsWithEmail();
        }
        UserEntity user = UserTransformer.userDtoToUser(userEntryDto);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        userRepository.save(user);
        return "User Saved Successfully";
    }

    public String loginUser(UserLoginDto userLoginDto){
        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userLoginDto.getEmailId(), userLoginDto.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(userLoginDto.getEmailId());
            String jwt = jwtConfig.generateToken(userDetails.getUsername());
            return jwt;
        }catch (Exception e){
            return "Incorrect username or password";
        }
    }

    public List<TicketResponseDto> allTickets() throws UserDoesNotExists{
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserEntity user = userRepository.findByEmailId(username);
        if(user == null) {
            throw new UserDoesNotExists();
        }
        List<Ticket> ticketList = user.getTicketList();
        List<TicketResponseDto> ticketResponseDtos = new ArrayList<>();
        for(Ticket ticket : ticketList) {
            TicketResponseDto ticketResponseDto = TicketTransformer.returnTicket(ticket.getShow(), ticket);
            ticketResponseDtos.add(ticketResponseDto);
        }
        return ticketResponseDtos;
    }
}
