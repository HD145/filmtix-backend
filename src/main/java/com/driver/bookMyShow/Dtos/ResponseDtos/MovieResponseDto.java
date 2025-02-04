package com.driver.bookMyShow.Dtos.ResponseDtos;

import com.driver.bookMyShow.Enums.Genre;
import com.driver.bookMyShow.Enums.Language;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class MovieResponseDto {
    private String movieName;
    private Integer duration;
    private Double rating;
    private Date releaseDate;
    private Genre genre;
    private Language language;
}
