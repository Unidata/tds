/*
 * Copyright (c) 2025 University Corporation for Atmospheric Research/Unidata
 * See LICENSE for license information.
 */

package thredds.tdm;

import java.net.URI;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import ucar.httpservices.HTTPException;
import ucar.httpservices.HTTPFactory;
import ucar.httpservices.HTTPSession;

/**
 * Manage HTTP connection for a TriggerableServer
 */
public class TriggerableServer {
  private static final org.slf4j.Logger tdmLogger = org.slf4j.LoggerFactory.getLogger(Tdm.class);

  static final String REMOTE_API_PATH = "/admin/collection/";
  static final String LOCAL_API_PATH = "/local/collection/";
  static final String DEFAULT_CONTEXT = "/thredds";
  static final String PARTIAL_TRIGGER_QUERY = "trigger=never&collection=";

  private final String url;
  final String name;
  final URI uri;
  final HTTPSession session;
  final String apiPath;
  final boolean local;

  private TriggerableServer(String url, String user, String pass) throws HTTPException {
    this.url = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    URI tmpURI = URI.create(this.url);
    local = tmpURI.getHost().equals("localhost");
    String tmpName = tmpURI.getHost();
    if (tmpURI.getPort() > 0) {
      tmpName = String.format("%s:%s", tmpName, tmpURI.getPort());
    }
    name = tmpName;
    String context = tmpURI.getPath().isEmpty() ? "/thredds" : tmpURI.getPath();
    apiPath = local ? LOCAL_API_PATH : REMOTE_API_PATH;
    uri = tmpURI.resolve(context + "/" + apiPath).normalize();
    HTTPSession httpSession = HTTPFactory.newSession(url);
    if (!local) {
      if (user != null && pass != null) {
        Credentials bp = new UsernamePasswordCredentials(user, pass);
        BasicCredentialsProvider bcp = new BasicCredentialsProvider();
        bcp.setCredentials(AuthScope.ANY, bp);
        httpSession.setCredentialsProvider(bcp);
      } else {
        throw new IllegalStateException("Credentials required for sending triggers to non-local server. Exiting.");
      }
    }
    session = httpSession;

    String serverProximity = local ? "Local" : "Remote";
    System.out.printf("%s Server added %s%n", serverProximity, url);
    tdmLogger.info("{} TDS server added {}", serverProximity, url);
  }

  public String getCollectionTrigger(String collectionName) {
    String basePath = uri.getPath();
    String pathAndQuery = String.format("%s/trigger?%s%s", basePath, PARTIAL_TRIGGER_QUERY, collectionName);
    return this.uri.resolve(pathAndQuery).normalize().toString();
  }

  public static class ServerBuilder {
    // required
    String url;

    // optional
    String user, pass;

    public static ServerBuilder create(String name) {
      return new ServerBuilder(name);
    }

    private ServerBuilder(String url) {
      this.url = url;
    }

    public ServerBuilder user(String user) {
      this.user = user;
      return this;
    }

    public ServerBuilder pass(String pass) {
      this.pass = pass;
      return this;
    }

    public TriggerableServer build() throws HTTPException {
      return new TriggerableServer(url, user, pass);
    }
  }
}
