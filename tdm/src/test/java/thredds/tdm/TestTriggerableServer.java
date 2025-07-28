/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.tdm;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.net.URI;
import org.junit.Test;
import thredds.tdm.TriggerableServer.ServerBuilder;
import ucar.httpservices.HTTPException;

public class TestTriggerableServer {

  @Test
  public void testBasicServerLocalhost() throws HTTPException {
    final String[] urls = new String[] {"http://localhost", "http://localhost/", "http://localhost:8080",
        "http://localhost:8080/", "http://localhost:8080/context", "http://localhost:8080/context/yep"};

    for (String url : urls) {
      assertThat(ServerBuilder.create(url).build()).isNotNull();
      assertThat(ServerBuilder.create(url).user(null).build()).isNotNull();
      assertThat(ServerBuilder.create(url).pass(null).build()).isNotNull();
      assertThat(ServerBuilder.create(url).user(null).pass(null).build()).isNotNull();
    }
  }

  @Test
  public void testBasicServerNonlocalhost() throws HTTPException {
    final String[] urls = new String[] {"http://my.tds.org", "http://my.tds.org/", "http://my.tds.org:8080",
        "http://my.tds.org:8080/", "http://my.tds.org:8080/context", "http://my.tds.org:8080/context/yep"};

    for (String url : urls) {
      assertThrows(IllegalStateException.class, () -> ServerBuilder.create(url).build());
      assertThrows(IllegalStateException.class, () -> ServerBuilder.create(url).user(null).build());
      assertThrows(IllegalStateException.class, () -> ServerBuilder.create(url).pass(null).build());
      assertThrows(IllegalStateException.class, () -> ServerBuilder.create(url).user(null).pass(null).build());
      assertThrows(IllegalStateException.class, () -> ServerBuilder.create(url).user("user").build());
      assertThrows(IllegalStateException.class, () -> ServerBuilder.create(url).pass("pass").build());

      final TriggerableServer server = ServerBuilder.create(url).user("user").pass("pass").build();
      assertThat(server).isNotNull();
    }
  }

  @Test
  public void testCustomContextNonLocal() throws HTTPException {
    final String context = "/krazy";
    final String url = "https://my.tds.org" + context;

    final TriggerableServer server = ServerBuilder.create(url).user("user").pass("pass").build();
    assertThat(server).isNotNull();
    assertThat(server.uri.getPath()).isEqualTo(context + TriggerableServer.REMOTE_API_PATH);
  }

  @Test
  public void testCustomContextLocal() throws HTTPException {
    final String context = "/krazy";
    final String url = "https://localhost:8443" + context;

    final TriggerableServer server = ServerBuilder.create(url).user("user").pass("pass").build();
    assertThat(server).isNotNull();
    assertThat(server.uri.getPath()).isEqualTo(context + TriggerableServer.LOCAL_API_PATH);
  }

  @Test
  public void testDefaultContextNonLocal() throws HTTPException {
    final String url = "https://my.tds.org:8443";

    final TriggerableServer server = ServerBuilder.create(url).user("user").pass("pass").build();
    assertThat(server).isNotNull();
    assertThat(server.uri.getPath()).isEqualTo(TriggerableServer.DEFAULT_CONTEXT + TriggerableServer.REMOTE_API_PATH);
  }

  @Test
  public void testDefaultContextLocal() throws HTTPException {
    final String url = "http://localhost:8443";

    final TriggerableServer server = ServerBuilder.create(url).user("user").pass("pass").build();
    assertThat(server).isNotNull();
    assertThat(server.uri.getPath()).isEqualTo(TriggerableServer.DEFAULT_CONTEXT + TriggerableServer.LOCAL_API_PATH);
  }

  @Test
  public void testTriggerUrl() throws HTTPException {
    final String url = "http://localhost:8443";
    final String collectionName = "collection-name";
    final TriggerableServer server = ServerBuilder.create(url).user("user").pass("pass").build();

    assertThat(server).isNotNull();
    final String triggerUrl = server.getCollectionTrigger(collectionName);
    assertThat(triggerUrl).isNotNull();

    final URI triggerUri = URI.create(triggerUrl);
    assertThat(triggerUri.getPath())
        .isEqualTo(TriggerableServer.DEFAULT_CONTEXT + TriggerableServer.LOCAL_API_PATH + "trigger");
    assertThat(triggerUri.getQuery()).isEqualTo(TriggerableServer.PARTIAL_TRIGGER_QUERY + collectionName);
  }
}
