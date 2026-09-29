package com.movieai.service.catalog;

import java.util.List;

import com.movieai.dto.ImageAsset;
import com.movieai.dto.PagedResponse;
import com.movieai.dto.PersonCredits;
import com.movieai.dto.PersonDetails;
import com.movieai.dto.PersonSummary;

/** Provider-neutral people data source. */
public interface PersonCatalog {

    PagedResponse<PersonSummary> trending(TimeWindow window, int page);

    PagedResponse<PersonSummary> popular(int page);

    PagedResponse<PersonSummary> search(String query, int page);

    PersonDetails details(long id);

    PersonCredits credits(long id);

    List<ImageAsset> images(long id);
}
