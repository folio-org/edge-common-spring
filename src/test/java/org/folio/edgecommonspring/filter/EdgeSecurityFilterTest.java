package org.folio.edgecommonspring.filter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.folio.edgecommonspring.TestBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class EdgeSecurityFilterTest extends TestBase {

  private static final String HEALTH_CHECK_ENDPOINT = "http://localhost:%s/admin/health";
  private static final String INFO_ENDPOINT = "http://localhost:%s/admin/info";

  @Test
  void testHealthCheck() {
    var response = get(String.format(HEALTH_CHECK_ENDPOINT, edgeDematicPort), getEmptyHeaders(), JsonNode.class);
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody()
      .get("status")
      .asText(), equalTo("UP"));
  }

  @Test
  void testInfo() {
    var response = get(String.format(INFO_ENDPOINT, edgeDematicPort), getEmptyHeaders(), JsonNode.class);
    assertThat(response.getBody(), notNullValue());
    assertThat(response.getBody()
      .asText(), equalTo(StringUtils.EMPTY));
  }

}
