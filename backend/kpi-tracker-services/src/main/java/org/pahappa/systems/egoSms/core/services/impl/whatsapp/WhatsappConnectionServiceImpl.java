package org.pahappa.systems.egoSms.core.services.impl.whatsapp;

import com.googlecode.genericdao.search.Search;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.client.fluent.Request;
import org.pahappa.systems.egoSms.core.services.impl.GenericServiceImpl;
import org.pahappa.systems.egoSms.core.services.whatsapp.WhatsappConnectionService;
import org.pahappa.systems.egoSms.models.whatsapp.WhatsappConnection;
import org.sers.webutils.model.RecordStatus;
import org.sers.webutils.model.exception.OperationFailedException;
import org.sers.webutils.model.exception.ValidationFailedException;
import org.sers.webutils.model.security.User;
import org.sers.webutils.server.shared.SharedAppData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WhatsappConnectionServiceImpl extends GenericServiceImpl<WhatsappConnection>
    implements WhatsappConnectionService {

  private static final int MAX_IMPORTED_PAGES = 8;
  private static final int MAX_IMPORTED_CHARACTERS = 12000;
  private static final int MAX_PAGE_CHARACTERS = 300000;

  private User currentUser() {
    User user = SharedAppData.getLoggedInUser();
    if (user == null) {
      throw new IllegalStateException("A logged-in user is required");
    }
    return user;
  }

  @Override
  public WhatsappConnection saveInstance(WhatsappConnection connection)
      throws ValidationFailedException, OperationFailedException {
    if (connection == null) {
      throw new ValidationFailedException("Missing WhatsApp connection");
    }
    if (blank(connection.getWabaId())) {
      throw new ValidationFailedException("Missing WABA ID");
    }
    if (blank(connection.getPhoneNumberId())) {
      throw new ValidationFailedException("Missing phone number ID");
    }
    if (connection.getOwner() == null) {
      connection.setOwner(currentUser());
    }
    if (!connection.getOwner().getId().equals(currentUser().getId())) {
      throw new OperationFailedException("You cannot modify another user's WhatsApp connection");
    }
    connection.setUpdatedAt(new Date());
    if (connection.getRecordStatus() == null) {
      connection.setRecordStatus(RecordStatus.ACTIVE);
    }
    if (connection.getConnectionStatus() == null) {
      connection.setConnectionStatus("CONNECTED");
    }
    WhatsappConnection saved = save(connection);
    entityManager.flush();
    return saved;
  }

  @Override
  public List<WhatsappConnection> getLoggedInUserConnections() {
    List<WhatsappConnection> visible = new ArrayList<WhatsappConnection>();
    for (WhatsappConnection connection : getLoggedInUserAllConnections()) {
      if ("CONNECTED".equalsIgnoreCase(connection.getConnectionStatus())) {
        visible.add(connection);
      }
    }
    return visible;
  }

  @Override
  public List<WhatsappConnection> getLoggedInUserAllConnections() {
    List<WhatsappConnection> stored =
        search(
            new Search()
                .addFilterEqual("owner.id", currentUser().getId())
                .addSortDesc("dateChanged"));
    List<WhatsappConnection> visible = new ArrayList<WhatsappConnection>();
    for (WhatsappConnection connection : stored) {
      if (connection.getRecordStatus() == null) {
        connection.setRecordStatus(RecordStatus.ACTIVE);
      }
      if (connection.getRecordStatus() == RecordStatus.ACTIVE) {
        visible.add(connection);
      }
    }
    return visible;
  }

  @Override
  public WhatsappConnection getLoggedInUserConnection(String id) throws OperationFailedException {
    WhatsappConnection connection =
        searchUnique(
            new Search()
                .addFilterEqual("id", id)
                .addFilterEqual("owner.id", currentUser().getId()));
    if (connection != null && connection.getRecordStatus() == null) {
      connection.setRecordStatus(RecordStatus.ACTIVE);
    }
    if (connection == null
        || connection.getRecordStatus() != RecordStatus.ACTIVE
        || !"CONNECTED".equalsIgnoreCase(connection.getConnectionStatus())) {
      throw new OperationFailedException("WhatsApp connection is not connected");
    }
    return connection;
  }

  @Override
  public WhatsappConnection findByPhoneNumberId(String id) {
    WhatsappConnection connection = searchUnique(new Search().addFilterEqual("phoneNumberId", id));
    if (connection != null && connection.getRecordStatus() == null) {
      connection.setRecordStatus(RecordStatus.ACTIVE);
    }
    return connection != null
            && connection.getRecordStatus() == RecordStatus.ACTIVE
            && "CONNECTED".equalsIgnoreCase(connection.getConnectionStatus())
        ? connection
        : null;
  }

  @Override
  public WhatsappConnection connect(
      String businessId,
      String wabaId,
      String phoneNumberId,
      String token,
      String displayNumber,
      String verifiedName)
      throws ValidationFailedException, OperationFailedException {
    WhatsappConnection connection =
        searchUnique(
            new Search()
                .addFilterEqual("owner.id", currentUser().getId())
                .addFilterEqual("wabaId", wabaId));
    if (connection == null) {
      connection = new WhatsappConnection();
    }
    connection.setOwner(currentUser());
    connection.setBusinessId(businessId);
    connection.setWabaId(wabaId);
    connection.setPhoneNumberId(phoneNumberId);
    connection.setAccessTokenCiphertext(token);
    connection.setDisplayPhoneNumber(displayNumber);
    connection.setVerifiedName(verifiedName);
    connection.setConnectionStatus("CONNECTED");
    return saveInstance(connection);
  }

  @Override
  public void disconnect(String id) throws OperationFailedException {
    WhatsappConnection connection = findOwnedConnection(id);
    connection.setConnectionStatus("DISCONNECTED");
    connection.setAccessTokenCiphertext("");
    connection.setUpdatedAt(new Date());
    entityManager.flush();
  }

  @Override
  public void updateAiAutoReply(String id, boolean enabled) throws OperationFailedException {
    WhatsappConnection connection = findOwnedConnection(id);
    connection.setAiAutoReplyEnabled(enabled);
    connection.setUpdatedAt(new Date());
    entityManager.flush();
  }

  @Override
  public void updateAiBusinessContext(String id, String context, String sourceUrl)
      throws OperationFailedException {
    WhatsappConnection connection = findOwnedConnection(id);
    if (context != null && context.length() > MAX_IMPORTED_CHARACTERS) {
      throw new OperationFailedException(
          "AI business information must be 12,000 characters or fewer");
    }
    connection.setAiBusinessContext(context == null ? null : context.trim());
    connection.setAiBusinessSourceUrl(sourceUrl == null ? null : sourceUrl.trim());
    connection.setUpdatedAt(new Date());
    entityManager.flush();
  }

  @Override
  public String importAiBusinessContext(String sourceUrl) throws OperationFailedException {
    try {
      URI start = validatedPublicUri(sourceUrl);
      LinkedHashSet<String> visited = new LinkedHashSet<String>();
      List<URI> queue = new ArrayList<URI>();
      queue.add(start);
      StringBuilder combined = new StringBuilder();
      int pages = 0;

      while (!queue.isEmpty()
          && pages < MAX_IMPORTED_PAGES
          && combined.length() < MAX_IMPORTED_CHARACTERS) {
        URI page = queue.remove(0);
        if (!visited.add(page.toString())) {
          continue;
        }
        String html;
        try {
          html = downloadPage(page);
        } catch (Exception pageError) {
          if (pages == 0) {
            throw pageError;
          }
          continue;
        }
        String text = readableText(html);
        if (!text.isEmpty()) {
          if (combined.length() > 0) {
            combined.append("\n\n");
          }
          combined.append("[Source: ").append(page).append("]\n").append(text);
        }
        pages++;
        for (String link : pageLinks(page, html, start.getHost())) {
          if (!visited.contains(link) && queue.size() < 24) {
            queue.add(new URI(link));
          }
        }
      }

      String result = combined.toString().trim();
      if (result.length() > MAX_IMPORTED_CHARACTERS) {
        result = result.substring(0, MAX_IMPORTED_CHARACTERS);
      }
      if (result.length() < 20) {
        throw new OperationFailedException("The site did not contain enough readable text");
      }
      return result;
    } catch (OperationFailedException error) {
      throw error;
    } catch (Exception error) {
      throw new OperationFailedException("Could not import the site: " + error.getMessage());
    }
  }

  private WhatsappConnection findOwnedConnection(String id) throws OperationFailedException {
    WhatsappConnection connection =
        searchUnique(
            new Search()
                .addFilterEqual("id", id)
                .addFilterEqual("owner.id", currentUser().getId()));
    if (connection == null
        || (connection.getRecordStatus() != null
            && connection.getRecordStatus() != RecordStatus.ACTIVE)) {
      throw new OperationFailedException("WhatsApp connection was not found");
    }
    return connection;
  }

  private String downloadPage(URI page) throws Exception {
    String html =
        Request.Get(page.toString())
            .connectTimeout(5000)
            .socketTimeout(10000)
            .execute()
            .returnContent()
            .asString(StandardCharsets.UTF_8);
    return html.length() > MAX_PAGE_CHARACTERS ? html.substring(0, MAX_PAGE_CHARACTERS) : html;
  }

  private String readableText(String html) {
    return html.replaceAll(
            "(?is)<(script|style|noscript|svg|nav|footer|header)[^>]*>.*?</\\1>", " ")
        .replaceAll("(?is)<[^>]+>", " ")
        .replaceAll("&nbsp;", " ")
        .replaceAll("&amp;", "&")
        .replaceAll("&lt;", "<")
        .replaceAll("&gt;", ">")
        .replaceAll("&#39;", "'")
        .replaceAll("&quot;", "\"")
        .replaceAll("\\s+", " ")
        .trim();
  }

  private List<String> pageLinks(URI base, String html, String host) {
    List<String> links = new ArrayList<String>();
    Matcher matcher = Pattern.compile("(?is)\\bhref\\s*=\\s*[\\\"']([^\\\"'#]+)").matcher(html);
    while (matcher.find() && links.size() < 12) {
      try {
        URI link = base.resolve(matcher.group(1).trim());
        String path = link.getPath() == null ? "" : link.getPath().toLowerCase();
        boolean technical =
            path.equals("/feed/")
                || path.contains("/comments/")
                || path.startsWith("/wp-json")
                || path.startsWith("/wp-content/")
                || path.matches(
                    ".*\\.(css|js|xml|json|rss|atom|pdf|zip|jpg|jpeg|png|gif|mp4|mp3|docx?)$");
        if (host.equalsIgnoreCase(link.getHost()) && !technical) {
          URI safe =
              validatedPublicUri(
                  new URI(link.getScheme(), link.getAuthority(), link.getPath(), null, null)
                      .toString());
          links.add(safe.toString());
        }
      } catch (Exception ignored) {
        // Ignore malformed or inaccessible links and continue crawling.
      }
    }
    return links;
  }

  private URI validatedPublicUri(String value) throws Exception {
    if (blank(value)) {
      throw new OperationFailedException("Enter a website or FAQ URL");
    }
    URI uri = new URI(value.trim());
    String scheme = uri.getScheme();
    String host = uri.getHost();
    if (!("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) || host == null) {
      throw new OperationFailedException("Use a complete HTTP or HTTPS URL");
    }
    InetAddress address = InetAddress.getByName(host);
    if (address.isAnyLocalAddress()
        || address.isLoopbackAddress()
        || address.isLinkLocalAddress()
        || address.isSiteLocalAddress()) {
      throw new OperationFailedException("Private or local URLs cannot be imported");
    }
    return uri;
  }

  private boolean blank(String value) {
    return value == null || value.trim().isEmpty();
  }

  @Override
  public boolean isDeletable(WhatsappConnection connection) {
    return connection != null
        && connection.getOwner() != null
        && connection.getOwner().getId().equals(currentUser().getId());
  }
}
