Design a modern, premium movie and TV discovery web application called MovieAI.

IMPORTANT:
This is NOT an AI chatbot product. Do not include AI assistants, AI chat interfaces, or AI-generated recommendations. The name MovieAI is only the product name. The application is powered by TMDB data.

Overall Design Direction
Create a cinematic, premium streaming-platform interface inspired by the visual quality of Netflix, HBO Max, Apple TV, IMDb, and modern editorial platforms, but with a unique original visual identity.

Style:

Dark cinematic interface
Premium and sophisticated
Modern minimal UI
Large cinematic imagery
Subtle glassmorphism
Soft gradients
Rounded cards
High-quality typography
Strong visual hierarchy
Smooth spacing
Elegant micro-interactions
No excessive neon
No overly futuristic cyberpunk styling
No clutter
Use a deep black / charcoal background with subtle warm accent colors.

Typography:

Use Inter or Manrope
Large bold cinematic headings
Clean medium-weight body text
Strong contrast and excellent readability
Canvas:

Desktop: 1440 × 1024
Create responsive versions for tablet and mobile
Use Auto Layout extensively
Use reusable components and variants
Use an 8px spacing system
Global Navigation
Create a premium top navigation bar.

Left:

MovieAI logo
Minimal cinematic logo mark
Navigation:

Home
Movies
TV Shows
Actors
Discover
Right:

Search icon / search field
Watchlist icon
User avatar
Navigation should become compact and sticky while scrolling.

HOME PAGE
Design the homepage as a cinematic discovery dashboard.

1. HERO / FEATURED
Create a large full-width cinematic hero section.

Use:

Large movie backdrop
Dark gradient overlay
Movie poster
Movie title
Short description
Rating
Release year
Runtime
Genres
Primary CTA: “Watch Trailer”
Secondary CTA: “More Details”
Add to Watchlist button
The background image should extend across the entire hero section.

Use strong left-to-right visual hierarchy.

Add subtle gradient fading into the dark page background.

2. TRENDING MOVIES
Section title:

“Trending Movies”

Add:

“Today”
“This Week”
“See All”
Create a horizontal movie carousel.

Each movie card:

Poster
Title
Release year
Rating
Small genre indicator
Watchlist button on hover
Cards should have rounded corners and subtle hover animations.

3. TRENDING TV SHOWS
Section title:

“Trending TV Shows”

Create a separate horizontal carousel.

Use the same reusable MovieCard component but adapt it for TV content.

Show:

Poster
Series name
First air date
Rating
Number of seasons when available
4. TRENDING ACTORS
Section title:

“Trending Actors”

Create elegant circular or softly rounded portrait cards.

Each actor card:

Large profile image
Actor name
Known-for title
Small popularity/trending indicator
Make this section visually different from movie cards.

5. NOW PLAYING
Section title:

“Now Playing”

Display movies currently playing in theaters.

Use horizontal cards with:

Poster
Movie title
Release date
Rating
6. AIRING TODAY
Create a separate TV section:

“Airing Today”

Show TV shows airing today.

Include:

Poster
Title
Episode information
Rating
Network
7. UPCOMING MOVIES
Section title:

“Coming Soon”

Create a visually distinctive upcoming section.

Show:

Poster
Release date
Countdown-style label such as:
“Sep 18”
Title
Genre
Add a “See All” button.

8. UPCOMING TV SHOWS
Create:

“Upcoming TV Shows”

Show:

Series poster
Next episode date
Season / episode
Rating
9. TOP RATED MOVIES
Create a premium editorial-style section:

“Top Rated Movies”

Display cards with:

Poster
Large rating number
Title
Year
10. TOP RATED TV SHOWS
Create:

“Top Rated TV Shows”

Use the same visual language.

WHAT SHOULD I WATCH?
Create a dedicated interactive discovery section.

Headline:

“What Should I Watch?”

Subtitle:

“Can’t decide? Pick what you’re in the mood for.”

Create genre chips:

Action
Adventure
Animation
Comedy
Crime
Drama
Fantasy
Horror
Mystery
Romance
Science Fiction
Thriller
Allow multiple selections.

Add additional filters:

Minimum Rating:

6+
7+
8+
9+
Release Period:

Any Year
2026
2020–2026
2010–2026
Runtime:

Any
Under 90 min
90–120 min
120+ min
Language:

Any
English
Spanish
Korean
Japanese
French
Primary CTA:

“🎲 Show Me 10 Movies”

After clicking, show a beautiful results page with exactly 10 movie cards.

Add:

“Give Me 10 More”

button.

MOVIES PAGE
Create a dedicated Movies discovery page.

Header:
“Movies”

Tabs:

Trending
Popular
Top Rated
Now Playing
Upcoming
Add advanced filter sidebar:

Genre
Release Year
Rating
Vote Count
Runtime
Language
Country
Sort By
Grid layout:
6 cards per row on desktop.

Use infinite scrolling or pagination.

TV SHOWS PAGE
Create:

“TV Shows”

Tabs:

Trending
Popular
Top Rated
Airing Today
On The Air
Upcoming
Use the same filter system adapted for TV.

ACTORS PAGE
Create:

“Trending Actors”

Include:

Today / This Week toggle
Grid of actor profiles
Profile photo
Name
Known For
Popularity
Make the page feel editorial and premium.

ACTOR DETAILS PAGE
Create a cinematic actor profile page.

Top section:

Large profile photo
Actor name
Biography
Birthday
Birthplace
Popularity
Add:

“Known For”

and:

“Filmography”

Tabs:

Movies
TV Shows
All Credits
Movie cards should show:

Poster
Title
Character
Year
Rating
Also create:
“Photos”

section.

MOVIE DETAILS PAGE
Create a highly cinematic movie details page.

Hero:

Full-screen backdrop
Dark gradient
Poster
Title
Original title
Rating
Release date
Runtime
Genres
Overview
Buttons:

Watch Trailer
Add to Watchlist
Favorite
Sections:

Cast
Crew
Director
Writers
Videos
Photos
Reviews
Keywords
Where to Watch
Similar Movies
Recommendations

Create a “Where to Watch” component with provider logos.

TV DETAILS PAGE
Create the equivalent cinematic TV details page.

Include:

Backdrop
Poster
Title
Rating
First air date
Status
Seasons
Episodes
Genres
Overview
Sections:

Cast
Crew
Seasons
Episodes
Photos
Videos
Reviews
Similar Shows
Recommendations
Where to Watch
SEARCH EXPERIENCE
Create a modern global search interface.

Search placeholder:

“Search movies, TV shows, actors…”

When typing, show a dropdown with three categories:

MOVIES
TV SHOWS
ACTORS

Each result:

Small poster/profile image
Name
Year
Media type
Create a full search results page with tabs:

All
Movies
TV Shows
Actors

WATCHLIST
Create:

“My Watchlist”

Tabs:

All
Movies
TV Shows
Allow:

Remove
Mark as watched
Open details
USER PROFILE
Create a minimal profile dashboard.

Show:

Avatar
Username
Favorite genres
Watched count
Watchlist count
Favorite movies
Sections:

Favorites
Watchlist
Watched
Custom Lists
DESIGN SYSTEM
Create a complete reusable design system.

Components:

Navigation
Buttons
Movie Card
TV Card
Actor Card
Genre Chip
Rating Badge
Watchlist Button
Favorite Button
Filter Dropdown
Search Bar
Tabs
Carousel
Modal
Trailer Modal
Pagination
Skeleton Loader
Toast Notification
Empty State
Error State
Create component variants:

Default
Hover
Active
Selected
Disabled
Loading
COLORS
Primary background:
Deep black / charcoal

Secondary surfaces:
Dark gray

Accent:
Warm cinematic orange / amber

Supporting accent:
Muted red

Text:
White / soft white / muted gray

Avoid excessive gradients.

Use gradients mainly for:

Hero overlays
Image readability
Subtle card effects
VISUAL DETAILS
Use:

16–24px card radius
Soft shadows
Thin subtle borders
Cinematic image overlays
Smooth hover transitions
Poster image zoom on hover
Glass effects only where appropriate
Large whitespace between sections
Consistent 8px spacing system
Do NOT:

Make the UI look like a generic dashboard
Use excessive glassmorphism
Use excessive neon colors
Use overly complicated animations
Make cards too small
Overload the homepage
RESPONSIVE DESIGN
Create three responsive layouts:

Desktop:
1440px

Tablet:
768–1024px

Mobile:
390px

Mobile navigation:

Bottom navigation
Home
Discover
Search
Watchlist
Profile
Mobile movie cards:
2-column grid where appropriate.

Horizontal carousels should support touch scrolling.

PROTOTYPE INTERACTIONS
Create clickable prototype interactions:

Home → Movie Details

Home → TV Details

Home → Actor Details

Search → Search Results

Search Result → Details

Actor → Filmography

Movie → Cast Actor

Movie → Trailer Modal

Add to Watchlist → Button state changes

Genre selection → Selected state

“What Should I Watch?” → Random 10 Results

“Give Me 10 More” → New result state

Navigation → Corresponding pages

FINAL DESIGN GOAL
The final product should feel like a real premium entertainment platform, not a student project or generic API demo.

Prioritize:

Cinematic visual hierarchy
High-quality movie artwork
Fast discovery
Clean navigation
Excellent search experience
Strong movie and actor detail pages
Beautiful responsive design
Reusable component system
Consistent spacing and typography
Premium modern streaming-platform aesthetic
Create all major screens and reusable components in one organized Figma project with clear sections and Auto Layout.