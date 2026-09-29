package com.movieai.support;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** TMDB-shaped JSON payloads (snake_case, as the real API returns them). */
public final class TmdbFixtures {

    private TmdbFixtures() {
    }

    public static final String MOVIE_GENRES = """
            {"genres":[{"id":28,"name":"Action"},{"id":12,"name":"Adventure"},{"id":18,"name":"Drama"},
            {"id":878,"name":"Science Fiction"},{"id":27,"name":"Horror"}]}
            """;

    public static final String TV_GENRES = """
            {"genres":[{"id":18,"name":"Drama"},{"id":10765,"name":"Sci-Fi & Fantasy"},{"id":10767,"name":"Talk"}]}
            """;

    public static final String TRENDING_MOVIES = """
            {"page":1,"total_pages":3,"total_results":57,"results":[
              {"id":693134,"title":"Dune: Part Two","original_title":"Dune: Part Two","overview":"Paul Atreides unites with Chani.",
               "release_date":"2024-02-27","vote_average":8.16,"vote_count":6200,"popularity":180.5,"genre_ids":[878,12],
               "poster_path":"/dune2.jpg","backdrop_path":"/dune2-bg.jpg","adult":false},
              {"id":1001,"title":"Unrated Indie","original_title":"Unrated Indie","overview":"",
               "release_date":"","vote_average":0.0,"vote_count":0,"popularity":1.2,"genre_ids":[18],
               "poster_path":null,"backdrop_path":null,"adult":false},
              {"id":1002,"title":"Hidden","release_date":"2024-01-01","vote_average":6.0,"vote_count":10,"adult":true}
            ]}
            """;

    public static final String MOVIE_DETAILS = """
            {"id":693134,"title":"Dune: Part Two","original_title":"Dune: Part Two","original_language":"en",
             "tagline":"Long live the fighters.","overview":"Paul Atreides unites with Chani and the Fremen.",
             "release_date":"2024-02-27","runtime":167,"vote_average":8.2,"vote_count":6200,"status":"Released",
             "genres":[{"id":878,"name":"Science Fiction"},{"id":12,"name":"Adventure"}],
             "poster_path":"/dune2.jpg","backdrop_path":"/dune2-bg.jpg","homepage":"https://www.dunemovie.com",
             "imdb_id":"tt15239678",
             "credits":{"cast":[
                {"id":1190668,"name":"Timothée Chalamet","character":"Paul Atreides","order":0,"profile_path":"/tc.jpg"},
                {"id":505710,"name":"Zendaya","character":"Chani","order":1,"profile_path":null}],
              "crew":[{"id":137427,"name":"Denis Villeneuve","job":"Director","department":"Directing","profile_path":"/dv.jpg"},
                {"id":5,"name":"Jon Spaihts","job":"Screenplay","department":"Writing","profile_path":null},
                {"id":6,"name":"Some Grip","job":"Grip","department":"Crew","profile_path":null}]},
             "videos":{"results":[
                {"id":"v1","key":"teaser1","name":"Teaser","site":"YouTube","type":"Teaser","official":true,"published_at":"2023-05-03T15:00:00.000Z"},
                {"id":"v2","key":"Way9Dexny3w","name":"Official Trailer 3","site":"YouTube","type":"Trailer","official":true,"published_at":"2023-12-18T15:00:00.000Z"},
                {"id":"v3","key":"bts1","name":"Behind the Scenes","site":"YouTube","type":"Behind the Scenes","official":true,"published_at":"2024-01-10T15:00:00.000Z"}]},
             "images":{"backdrops":[{"file_path":"/bd1.jpg","width":3840,"height":2160,"aspect_ratio":1.778}],
                       "posters":[{"file_path":"/p1.jpg","width":2000,"height":3000,"aspect_ratio":0.667}],
                       "logos":[]},
             "recommendations":{"page":1,"total_pages":1,"total_results":1,"results":[
                {"id":438631,"title":"Dune","release_date":"2021-09-15","vote_average":7.8,"vote_count":12000,"genre_ids":[878],"poster_path":"/dune1.jpg"}]},
             "similar":{"page":1,"total_pages":0,"total_results":0,"results":[]},
             "watch/providers":{"results":{
                "US":{"link":"https://www.themoviedb.org/movie/693134/watch?locale=US",
                      "flatrate":[{"provider_id":1899,"provider_name":"Max","logo_path":"/max.jpg","display_priority":2}],
                      "rent":[{"provider_id":2,"provider_name":"Apple TV","logo_path":"/apple.jpg","display_priority":4}]},
                "GB":{"link":"https://www.themoviedb.org/movie/693134/watch?locale=GB",
                      "buy":[{"provider_id":10,"provider_name":"Amazon Video","logo_path":"/amz.jpg","display_priority":7}]}}}}
            """;

    public static final String TV_PAGE = """
            {"page":1,"total_pages":1,"total_results":1,"results":[
              {"id":1399,"name":"Game of Thrones","original_name":"Game of Thrones","overview":"Seven noble families.",
               "first_air_date":"2011-04-17","vote_average":8.46,"vote_count":24000,"popularity":300.1,
               "genre_ids":[18,10765],"poster_path":"/got.jpg","backdrop_path":"/got-bg.jpg"}]}
            """;

    public static final String TV_DETAILS = """
            {"id":1399,"name":"Game of Thrones","original_name":"Game of Thrones","original_language":"en",
             "overview":"Seven noble families fight for control.","first_air_date":"2011-04-17","last_air_date":"2019-05-19",
             "vote_average":8.46,"vote_count":24000,"status":"Ended","in_production":false,
             "number_of_seasons":8,"number_of_episodes":73,"episode_run_time":[60],
             "genres":[{"id":18,"name":"Drama"}],"networks":[{"id":49,"name":"HBO","logo_path":"/hbo.png"}],
             "created_by":[{"id":9813,"name":"David Benioff","profile_path":null}],
             "poster_path":"/got.jpg","backdrop_path":"/got-bg.jpg",
             "seasons":[{"id":3627,"season_number":0,"name":"Specials","episode_count":14,"air_date":"2010-12-05","poster_path":null},
                        {"id":3624,"season_number":1,"name":"Season 1","episode_count":10,"air_date":"2011-04-17","poster_path":"/s1.jpg"}],
             "credits":{"cast":[{"id":22970,"name":"Peter Dinklage","character":"Tyrion Lannister","order":0,"profile_path":"/pd.jpg"}],"crew":[]},
             "videos":{"results":[]},
             "images":{"backdrops":[],"posters":[],"logos":[]},
             "recommendations":{"page":1,"total_pages":0,"total_results":0,"results":[]},
             "similar":{"page":1,"total_pages":0,"total_results":0,"results":[]},
             "watch/providers":{"results":{}}}
            """;

    public static final String PEOPLE_PAGE = """
            {"page":1,"total_pages":1,"total_results":1,"results":[
              {"id":1190668,"name":"Timothée Chalamet","known_for_department":"Acting","popularity":95.2,
               "profile_path":"/tc.jpg","known_for":[{"id":693134,"media_type":"movie","title":"Dune: Part Two"},
               {"id":1399,"media_type":"tv","name":"Homeland"}]}]}
            """;

    public static final String PERSON_DETAILS = """
            {"id":1190668,"name":"Timothée Chalamet","biography":"","birthday":null,"deathday":null,
             "place_of_birth":null,"known_for_department":"Acting","also_known_as":[],"popularity":95.2,
             "profile_path":"/tc.jpg","homepage":null,"imdb_id":"nm3154303",
             "combined_credits":{
               "cast":[
                 {"id":693134,"media_type":"movie","title":"Dune: Part Two","character":"Paul Atreides","release_date":"2024-02-27","vote_average":8.2,"vote_count":6200,"popularity":180.5,"genre_ids":[878],"poster_path":"/dune2.jpg"},
                 {"id":438631,"media_type":"movie","title":"Dune","character":"Paul Atreides","release_date":"2021-09-15","vote_average":7.8,"vote_count":12000,"popularity":90.0,"genre_ids":[878],"poster_path":"/dune1.jpg"},
                 {"id":1407,"media_type":"tv","name":"Homeland","character":"Finn Walden","first_air_date":"2011-10-02","vote_average":7.7,"vote_count":3000,"popularity":50.0,"genre_ids":[18],"episode_count":8,"poster_path":"/homeland.jpg"},
                 {"id":59941,"media_type":"tv","name":"The Tonight Show","character":"Self","first_air_date":"2014-02-17","vote_average":6.0,"vote_count":300,"popularity":80.0,"genre_ids":[10767],"episode_count":3}],
               "crew":[{"id":693134,"media_type":"movie","title":"Dune: Part Two","job":"Executive Producer","release_date":"2024-02-27","popularity":180.5,"genre_ids":[878]}]},
             "images":{"profiles":[{"file_path":"/tc.jpg","width":1000,"height":1500,"aspect_ratio":0.667},
                                   {"file_path":"/tc2.jpg","width":800,"height":1200,"aspect_ratio":0.667}]}}
            """;

    /** A discover page of {@code count} movies with ids {@code page*100 + i}. */
    public static String discoverPage(int page, int count, int totalPages, int totalResults) {
        String results = IntStream.range(0, count)
                .mapToObj(i -> {
                    int id = page * 100 + i;
                    return """
                            {"id":%d,"title":"Movie %d","release_date":"2020-01-01","vote_average":7.5,"vote_count":500,
                             "popularity":%d,"genre_ids":[28],"poster_path":"/m%d.jpg"}""".formatted(id, id, 1000 - id, id);
                })
                .collect(Collectors.joining(","));
        return """
                {"page":%d,"total_pages":%d,"total_results":%d,"results":[%s]}""".formatted(page, totalPages, totalResults, results);
    }
}
