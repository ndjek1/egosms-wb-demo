package org.pahappa.systems.egoSms.core.services.impl.whatsapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URLEncoder;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetaGraphClient {

  private final ObjectMapper mapper = new ObjectMapper();

  @Autowired private MetaConfiguration config;

  public JsonNode get(String path, String token) {
    try {
      Request request = Request.Get(url(path));
      if (token != null) {
        request.addHeader("Authorization", "Bearer " + token);
      }
      return response(request);
    } catch (Exception error) {
      throw failure(error);
    }
  }

  public JsonNode post(String path, String token, Object body) {
    try {
      return response(
          Request.Post(url(path))
              .addHeader("Authorization", "Bearer " + token)
              .bodyString(mapper.writeValueAsString(body), ContentType.APPLICATION_JSON));
    } catch (Exception error) {
      throw failure(error);
    }
  }

  public JsonNode postEmpty(String path, String token) {
    try {
      return response(Request.Post(url(path)).addHeader("Authorization", "Bearer " + token));
    } catch (Exception error) {
      throw failure(error);
    }
  }

  public JsonNode upload(String path, String token, String contentType, byte[] content) {
    try {
      return response(
          Request.Post(url(path))
              .addHeader("Authorization", "OAuth " + token)
              .addHeader("file_offset", "0")
              .bodyByteArray(content, ContentType.create(contentType)));
    } catch (Exception error) {
      throw failure(error);
    }
  }

  public JsonNode uploadMedia(
      String path, String token, String fileName, String contentType, byte[] content) {
    CloseableHttpClient client = null;
    CloseableHttpResponse response = null;
    try {
      client = HttpClients.createDefault();
      HttpPost post = new HttpPost(url(path));
      post.addHeader("Authorization", "Bearer " + token);
      post.setEntity(
          MultipartEntityBuilder.create()
              .addTextBody("messaging_product", "whatsapp", ContentType.TEXT_PLAIN)
              .addTextBody("type", contentType, ContentType.TEXT_PLAIN)
              .addBinaryBody("file", content, ContentType.create(contentType), fileName)
              .build());
      response = client.execute(post);
      return response(response);
    } catch (Exception error) {
      throw failure(error);
    } finally {
      closeQuietly(response);
      closeQuietly(client);
    }
  }

  public JsonNode exchangeCode(String code) {
    try {
      String path =
          "/oauth/access_token?client_id="
              + encode(config.appId())
              + "&client_secret="
              + encode(config.appSecret())
              + "&code="
              + encode(code);
      return get(path, null);
    } catch (Exception error) {
      throw failure(error);
    }
  }

  private JsonNode response(Request request) throws Exception {
    HttpResponse response = request.execute().returnResponse();
    return response(response);
  }

  private JsonNode response(HttpResponse response) throws Exception {
    String body = EntityUtils.toString(response.getEntity());
    if (response.getStatusLine().getStatusCode() >= 300) {
      throw new IllegalStateException("Meta API rejected the request: " + body);
    }
    return mapper.readTree(body);
  }

  private String url(String path) {
    return config.graphBase() + "/" + config.version() + path;
  }

  private String encode(String value) throws Exception {
    return URLEncoder.encode(value, "UTF-8");
  }

  private RuntimeException failure(Exception error) {
    if (error instanceof RuntimeException) {
      return (RuntimeException) error;
    }
    return new IllegalStateException("Meta API request failed", error);
  }

  private void closeQuietly(CloseableHttpResponse response) {
    if (response == null) {
      return;
    }
    try {
      response.close();
    } catch (Exception ignored) {
      // The original request result is more important than cleanup failure.
    }
  }

  private void closeQuietly(CloseableHttpClient client) {
    if (client == null) {
      return;
    }
    try {
      client.close();
    } catch (Exception ignored) {
      // The original request result is more important than cleanup failure.
    }
  }
}
