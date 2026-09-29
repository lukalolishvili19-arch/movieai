// Mirrors the backend DTOs served under /api/v1. Nullable fields are null when TMDB has no value.

export type MediaType = 'movie' | 'tv'
export type TimeWindow = 'day' | 'week'

export interface PagedResponse<T> {
  results: T[]
  page: number
  totalPages: number
  totalResults: number
}

export interface Genre {
  id: number
  name: string
}

interface MediaSummaryBase {
  id: number
  title: string
  originalTitle: string
  overview: string
  releaseDate: string | null
  year: number | null
  rating: number | null
  voteCount: number
  popularity: number
  genreIds: number[]
  genres: string[]
  posterPath: string | null
  posterUrl: string | null
  backdropPath: string | null
  backdropUrl: string | null
}

export interface MovieSummary extends MediaSummaryBase {
  mediaType: 'movie'
}

export interface TvSummary extends MediaSummaryBase {
  mediaType: 'tv'
}

export type MediaSummary = MovieSummary | TvSummary

export interface PersonSummary {
  id: number
  name: string
  knownForDepartment: string | null
  knownFor: string[]
  popularity: number
  profilePath: string | null
  profileUrl: string | null
}

export interface CastMember {
  id: number
  name: string
  character: string | null
  order: number
  profileUrl: string | null
}

export interface CrewMember {
  id: number
  name: string
  job: string
  department: string
  profileUrl: string | null
}

export interface ImageAsset {
  filePath: string
  url: string
  thumbnailUrl: string
  width: number
  height: number
  aspectRatio: number
  type: 'poster' | 'backdrop' | 'profile' | 'logo' | 'still'
}

export interface MediaImages {
  backdrops: ImageAsset[]
  posters: ImageAsset[]
  logos: ImageAsset[]
}

export interface VideoAsset {
  id: string
  key: string
  name: string
  site: string
  type: string
  official: boolean
  publishedAt: string | null
  watchUrl: string | null
  embedUrl: string | null
  thumbnailUrl: string | null
}

export interface WatchProvider {
  id: number
  name: string
  logoUrl: string | null
  displayPriority: number
}

export interface WatchProviders {
  region: string
  link: string | null
  flatrate: WatchProvider[]
  free: WatchProvider[]
  ads: WatchProvider[]
  rent: WatchProvider[]
  buy: WatchProvider[]
  attribution: string
}

export interface MovieDetails {
  id: number
  mediaType: 'movie'
  title: string
  originalTitle: string | null
  originalLanguage: string | null
  tagline: string | null
  overview: string | null
  releaseDate: string | null
  year: number | null
  runtime: number | null
  rating: number | null
  voteCount: number
  status: string | null
  genres: Genre[]
  posterUrl: string | null
  backdropUrl: string | null
  homepage: string | null
  imdbId: string | null
  directors: CrewMember[]
  crew: CrewMember[]
  cast: CastMember[]
  images: MediaImages
  videos: VideoAsset[]
  trailer: VideoAsset | null
  similar: MovieSummary[]
  watchProviders: WatchProviders | null
}

export interface TvNetwork {
  id: number
  name: string
  logoUrl: string | null
}

export interface TvSeason {
  id: number
  seasonNumber: number
  name: string
  episodeCount: number
  airDate: string | null
  overview: string | null
  posterUrl: string | null
}

export interface TvDetails {
  id: number
  mediaType: 'tv'
  title: string
  originalTitle: string | null
  originalLanguage: string | null
  tagline: string | null
  overview: string | null
  releaseDate: string | null
  lastAirDate: string | null
  year: number | null
  rating: number | null
  voteCount: number
  status: string | null
  inProduction: boolean
  numberOfSeasons: number | null
  numberOfEpisodes: number | null
  episodeRunTime: number[]
  genres: Genre[]
  networks: TvNetwork[]
  createdBy: CrewMember[]
  posterUrl: string | null
  backdropUrl: string | null
  homepage: string | null
  seasons: TvSeason[]
  cast: CastMember[]
  images: MediaImages
  videos: VideoAsset[]
  trailer: VideoAsset | null
  similar: TvSummary[]
  watchProviders: WatchProviders | null
}

export interface PersonCredit {
  id: number
  mediaType: MediaType
  title: string
  character: string | null
  job: string | null
  creditType: 'cast' | 'crew'
  releaseDate: string | null
  year: number | null
  rating: number | null
  voteCount: number
  popularity: number
  genres: string[]
  posterUrl: string | null
  episodeCount: number | null
}

export interface PersonCredits {
  movies: PersonCredit[]
  tv: PersonCredit[]
}

export interface PersonDetails {
  id: number
  name: string
  biography: string | null
  birthday: string | null
  deathday: string | null
  placeOfBirth: string | null
  knownForDepartment: string | null
  alsoKnownAs: string[]
  popularity: number
  profileUrl: string | null
  homepage: string | null
  imdbId: string | null
  credits: PersonCredits
  images: ImageAsset[]
}

export interface SearchResults {
  query: string
  movies: PagedResponse<MovieSummary>
  tv: PagedResponse<TvSummary>
  people: PagedResponse<PersonSummary>
}

export interface RecommendationResponse {
  results: MovieSummary[]
  page: number
  seed: number
  hasMore: boolean
  totalMatches: number
}

export type MovieCategory = 'trending' | 'popular' | 'top-rated' | 'now-playing' | 'upcoming'
export type TvCategory = 'trending' | 'popular' | 'top-rated' | 'airing-today' | 'on-the-air' | 'upcoming'
export type SortOption = 'popularity' | 'rating' | 'release_date' | 'title'

export interface BrowseParams<C extends string> {
  category: C
  period?: TimeWindow
  genres?: number[]
  minRating?: number
  yearFrom?: number
  yearTo?: number
  runtime?: string
  sort?: SortOption
  page?: number
}

export interface RecommendationParams {
  genres?: number[]
  minRating?: number
  releasePeriod?: string
  runtime?: string
  page?: number
  limit?: number
  seed?: number
}

export interface UserProfile {
  id: string
  email: string
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: 'Bearer'
  expiresIn: number
  user: UserProfile
}

export interface Credentials {
  email: string
  password: string
}

export interface WatchlistItem {
  id: string
  mediaType: MediaType
  tmdbId: number
  addedAt: string
  media: MediaSummary | null
}

export interface WatchlistKey {
  mediaType: MediaType
  tmdbId: number
}

export interface WatchlistStatus extends WatchlistKey {
  inWatchlist: boolean
}

export interface FieldViolation {
  field: string
  message: string
}

export interface ApiErrorBody {
  success: false
  error: {
    code: string
    message: string
    details?: FieldViolation[]
  }
}
