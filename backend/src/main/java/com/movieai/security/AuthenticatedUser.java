package com.movieai.security;

import java.util.UUID;

/** Principal placed in the security context for requests with a valid access token. */
public record AuthenticatedUser(UUID id, String email) {
}
