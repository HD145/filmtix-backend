package com.driver.bookMyShow.Transformers;

import com.driver.bookMyShow.Dtos.RequestDtos.MovieEntryDto;
import com.driver.bookMyShow.Dtos.ResponseDtos.MovieResponseDto;
import com.driver.bookMyShow.Models.Movie;

public class MovieTransformer {

    public static Movie movieDtoToMovie(MovieEntryDto movieEntryDto) {
        Movie movie = Movie.builder()
                .movieName(movieEntryDto.getMovieName())
                .duration(movieEntryDto.getDuration())
                .genre(movieEntryDto.getGenre())
                .language(movieEntryDto.getLanguage())
                .releaseDate(movieEntryDto.getReleaseDate())
                .rating(movieEntryDto.getRating())
                .build();

        return movie;
    }

    public static MovieResponseDto movieToMovieDto(Movie movie) {
        MovieResponseDto movieResponseDto = MovieResponseDto.builder()
                .movieName(movie.getMovieName())
                .duration(movie.getDuration())
                .genre(movie.getGenre())
                .language(movie.getLanguage())
                .releaseDate(movie.getReleaseDate())
                .rating(movie.getRating())
                .build();

        return movieResponseDto;
    }
}
