package com.movieai.dto;

import java.util.List;

public record MediaImages(
        List<ImageAsset> backdrops,
        List<ImageAsset> posters,
        List<ImageAsset> logos) {

    public static MediaImages empty() {
        return new MediaImages(List.of(), List.of(), List.of());
    }
}
