package com.movieai.service.catalog;

import java.util.List;

import com.movieai.dto.Genre;
import com.movieai.dto.MediaImages;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.TvDetails;
import com.movieai.dto.TvSummary;
import com.movieai.dto.VideoAsset;
import com.movieai.dto.WatchProviders;

/** Provider-neutral TV data source. */
public interface TvCatalog {

    PagedResponse<TvSummary> trending(TimeWindow window, int page);

    PagedResponse<TvSummary> popular(int page);

    PagedResponse<TvSummary> airingToday(int page);

    PagedResponse<TvSummary> onTheAir(int page);

    PagedResponse<TvSummary> topRated(int page);

    PagedResponse<TvSummary> discover(DiscoverQuery query);

    PagedResponse<TvSummary> search(String query, int page);

    TvDetails details(long id, String region);

    MediaImages images(long id);

    List<VideoAsset> videos(long id);

    WatchProviders watchProviders(long id, String region);

    List<Genre> genres();
}
