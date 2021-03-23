package org.folio.edgecommonspring.util;

import java.util.Properties;
import org.folio.edgecommonspring.exception.AuthorizationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class PropertiesUtilTest {


  @Test
  void getProperties_shouldReturnProperties() {
    Properties properties = PropertiesUtil.getProperties("src/test/resources/ephemeral.properties");

    Assertions.assertNotNull(properties);
    Assertions.assertEquals("fs00000000,test", properties.get("tenants"));
    Assertions.assertEquals("test_admin,test", properties.get("test"));
    Assertions.assertEquals("fs00000000,{FS00000000_IU_PASSWORD}", properties.get("fs00000000"));
    Assertions.assertEquals("Ephemeral", properties.get("secureStore.type"));
  }

  @Test
  void getProperties_shouldFailToLoadProperties() {

    AuthorizationException exception = Assertions.assertThrows(AuthorizationException.class, () ->
      PropertiesUtil.getProperties("src/test/resources/test.properties"));

    Assertions.assertEquals("Failed to load secure store properties", exception.getMessage());
  }

  @Test
  void getProperties_shouldUseDefault() {

    Properties properties = PropertiesUtil.getProperties(null);

    Assertions.assertEquals(0, properties.size());
  }

}
