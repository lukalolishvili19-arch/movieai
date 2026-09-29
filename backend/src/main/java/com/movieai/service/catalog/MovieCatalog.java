package com.movieai.service.catalog;

import java.util.List;

import com.movieai.dto.Genre;
import com.movieai.dto.MediaImages;
import com.movieai.dto.MovieDetails;
import com.movieai.dto.MovieSummary;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.VideoAsset;
import com.movieai.dto.WatchProviders;

/** Provider-neutral movie data source. The TMDB implementation lives in the integration layer. */
public interface MovieCatalog {

    PagedResponse<MovieSummary> trending(TimeWindow window, int page);

    PagedResponse<MovieSummary> popular(int page);

    PagedResponse<MovieSummary> nowPlaying(int page);

    PagedResponse<MovieSummary> upcoming(int page);

    PagedResponse<MovieSummary> topRated(int page);

    PagedResponse<MovieSummary> discover(DiscoverQuery query);

    PagedResponse<MovieSummary> search(String query, int page);

    MovieDetails details(long id, String region);

    MediaImages images(long id);

    List<VideoAsset> videos(long id);

    WatchProviders watchProviders(long id, String region);

    List<Genre> genres();
}
