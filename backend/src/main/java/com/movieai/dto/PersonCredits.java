package com.movieai.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Combined movie and TV credits, de-duplicated per title and sorted by popularity.")
public record PersonCredits(
        List<PersonCredit> movies,
        List<PersonCredit> tv) {
}
