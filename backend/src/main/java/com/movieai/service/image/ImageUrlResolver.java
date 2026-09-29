package com.movieai.service.image;

/** Builds public image URLs from provider file paths. Only file paths are ever stored. */
public interface ImageUrlResolver {

    enum Kind { POSTER, BACKDROP, PROFILE, LOGO, STILL }

    /** Purpose-based sizes; implementations map them to the closest provider size. */
    enum Size { THUMB, CARD, DETAIL, HERO }

    /** @return the URL, or {@code null} when {@code path} is empty. */
    String url(String path, Kind kind, Size size);
}
