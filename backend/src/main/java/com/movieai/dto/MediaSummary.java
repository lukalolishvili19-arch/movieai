package com.movieai.dto;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/** Common card-level shape shared by movies and TV shows. */
@Schema(oneOf = {MovieSummary.class, TvSummary.class}, discriminatorProperty = "mediaType")
public sealed interface MediaSummary permits MovieSummary, TvSummary {

    long id();

    MediaType mediaType();

    String title();

    LocalDate releaseDate();

    Integer year();

    Double rating();

    int voteCount();

    List<String> genres();

    String posterUrl();
}
