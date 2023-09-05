package org.folio.edgecommonspring.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.folio.spring.model.UserToken;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class ConnectionSystemParameters {

  private String username;

  private String password;

  @JsonIgnore
  private UserToken okapiToken;

  @JsonIgnore
  private String tenantId;
}
