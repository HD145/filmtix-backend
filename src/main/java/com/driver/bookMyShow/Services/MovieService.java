package com.driver.bookMyShow.Services;

import com.driver.bookMyShow.Dtos.RequestDtos.MovieEntryDto;
import com.driver.bookMyShow.Dtos.ResponseDtos.MovieResponseDto;
import com.driver.bookMyShow.Exceptions.MovieAlreadyPresentWithSameNameAndLanguage;
import com.driver.bookMyShow.Exceptions.MovieDoesNotExists;
import com.driver.bookMyShow.Models.Movie;
import com.driver.bookMyShow.Models.Show;
import com.driver.bookMyShow.Models.Ticket;
import com.driver.bookMyShow.Repositories.MovieRepository;
import com.driver.bookMyShow.Repositories.ShowRepository;
import com.driver.bookMyShow.Transformers.MovieTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private RedisService redisService;

    public String addMovie(MovieEntryDto movieEntryDto) throws MovieAlreadyPresentWithSameNameAndLanguage {
        if(movieRepository.findByMovieName(movieEntryDto.getMovieName()) != null) {
            if(movieRepository.findByMovieName(movieEntryDto.getMovieName()).getLanguage().equals(movieEntryDto.getLanguage())){
                throw new MovieAlreadyPresentWithSameNameAndLanguage();
            }
        }
        Movie movie = MovieTransformer.movieDtoToMovie(movieEntryDto);
        movieRepository.save(movie);
        return "The movie has been added successfully";
    }

    public Long totalCollection(Integer movieId) throws MovieDoesNotExists {
        Optional<Movie> movieOpt = movieRepository.findById(movieId);
        if(movieOpt.isEmpty()) {
            throw new MovieDoesNotExists();
        }
        List<Show> showListOfMovie = showRepository.getAllShowsOfMovie(movieId);
        long ammount = 0;
        for(Show show : showListOfMovie) {
            for(Ticket ticket : show.getTicketList()) {
                ammount += (long)ticket.getTotalTicketsPrice();
            }
        }
        return ammount;
    }

    public List<MovieResponseDto> movieList() throws MovieDoesNotExists{

        List<MovieResponseDto> cachedMovies = redisService.get("MOVIE_LIST", List.class);

        if(cachedMovies != null && !cachedMovies.isEmpty()){
            return cachedMovies;
        }
        List<Movie> movies= movieRepository.findAll();
        if(movies.isEmpty()){
            throw new MovieDoesNotExists();
        }
        List<MovieResponseDto> moviesDto= new ArrayList<>();
        for(Movie m : movies){
            moviesDto.add(MovieTransformer.movieToMovieDto(m));
        }
        System.out.println("DB Called");
        redisService.set("MOVIE_LIST", moviesDto, 600L);
        return moviesDto;
    }
}
