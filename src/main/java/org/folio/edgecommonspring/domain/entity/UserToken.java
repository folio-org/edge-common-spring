package org.folio.edgecommonspring.domain.entity;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserToken(String accessToken, Instant accessTokenExpiration) {
}
