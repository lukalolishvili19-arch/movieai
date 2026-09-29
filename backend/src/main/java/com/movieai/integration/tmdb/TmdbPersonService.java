package com.movieai.integration.tmdb;

import java.util.List;
import java.util.Map;

import com.movieai.config.CacheNames;
import com.movieai.dto.ImageAsset;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.PersonCredits;
import com.movieai.dto.PersonDetails;
import com.movieai.dto.PersonSummary;
import com.movieai.exception.ErrorCode;
import com.movieai.exception.ResourceNotFoundException;
import com.movieai.integration.tmdb.model.TmdbModels;
import com.movieai.mapper.TmdbDtoMapper;
import com.movieai.service.cache.ResilientCache;
import com.movieai.service.catalog.PersonCatalog;
import com.movieai.service.catalog.TimeWindow;
import com.movieai.service.image.ImageUrlResolver.Kind;
import com.movieai.service.image.ImageUrlResolver.Size;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

@Service
public class TmdbPersonService implements PersonCatalog {

    private static final ParameterizedTypeReference<TmdbModels.Page<TmdbModels.Person>> PERSON_PAGE =
            new ParameterizedTypeReference<>() {
            };

    private final TmdbClient client;
    private final ResilientCache cache;
    private final TmdbDtoMapper mapper;
    private final TmdbGenreService genres;

    public TmdbPersonService(TmdbClient client, ResilientCache cache, TmdbDtoMapper mapper, TmdbGenreService genres) {
        this.client = client;
        this.cache = cache;
        this.mapper = mapper;
        this.genres = genres;
    }

    @Override
    public PagedResponse<PersonSummary> trending(TimeWindow window, int page) {
        String path = "/trending/person/" + window.value();
        return cache.get(CacheNames.TRENDING, path + ":" + page,
                () -> mapper.personPage(client.get(path, Map.of("page", page), PERSON_PAGE)));
    }

    @Override
    public PagedResponse<PersonSummary> popular(int page) {
        return cache.get(CacheNames.LISTS, "/person/popular:" + page,
                () -> mapper.personPage(client.get("/person/popular", Map.of("page", page), PERSON_PAGE)));
    }

    @Override
    public PagedResponse<PersonSummary> search(String query, int page) {
        String normalized = query.trim();
        return cache.get(CacheNames.SEARCH, "person:" + normalized.toLowerCase() + ":" + page,
                () -> mapper.personPage(client.get("/search/person",
                        Map.of("query", normalized, "page", page, "include_adult", "false"), PERSON_PAGE)));
    }

    @Override
    public PersonDetails details(long id) {
        return cache.get(CacheNames.DETAILS, "person:" + id, () -> {
            TmdbModels.PersonDetails raw;
            try {
                raw = client.get("/person/" + id, Map.of("append_to_response", "combined_credits,images"),
                        TmdbModels.PersonDetails.class);
            } catch (TmdbNotFoundException e) {
                throw new ResourceNotFoundException(ErrorCode.PERSON_NOT_FOUND);
            }
            return new PersonDetails(
                    raw.id(), raw.name(), TmdbDtoMapper.blankToNull(raw.biography()),
                    TmdbDtoMapper.parseDate(raw.birthday()), TmdbDtoMapper.parseDate(raw.deathday()),
                    TmdbDtoMapper.blankToNull(raw.placeOfBirth()), TmdbDtoMapper.blankToNull(raw.knownForDepartment()),
                    raw.alsoKnownAs() == null ? List.of() : raw.alsoKnownAs(),
                    raw.popularity() == null ? 0 : raw.popularity(),
                    mapper.imageUrl(raw.profilePath(), Kind.PROFILE, Size.DETAIL),
                    TmdbDtoMapper.blankToNull(raw.homepage()), TmdbDtoMapper.blankToNull(raw.imdbId()),
                    mapper.personCredits(raw.combinedCredits(), genres.movieGenreNames(), genres.tvGenreNames()),
                    mapper.profileImages(raw.images()));
        });
    }

    @Override
    public PersonCredits credits(long id) {
        return details(id).credits();
    }

    @Override
    public List<ImageAsset> images(long id) {
        return details(id).images();
    }
}
