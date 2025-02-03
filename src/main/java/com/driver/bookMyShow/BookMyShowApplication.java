package com.driver.bookMyShow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class BookMyShowApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookMyShowApplication.class, args);
		System.out.println("Hello Spring Boot.");
	}

//TODO:
//	3. Get count of unique locations of a theater
//	4. Get the list of theaters Showing a particular time.
//	6. Cancel Ticket
//	8. rate movie Flop or Hit based on collection or ticketBooked
}
