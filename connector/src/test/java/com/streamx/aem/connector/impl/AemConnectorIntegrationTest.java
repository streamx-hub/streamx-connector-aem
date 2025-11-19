package com.streamx.aem.connector.impl;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.awaitility.core.ThrowingRunnable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The test enables integration testing of the AEM Connector.
 * Prerequisites:
 *  - running AEM instance
 *  - the Connector deployed on AEM (can be achieved by running "mvn clean install -PautoInstallSinglePackage" from the project's root directory)
 *  - running StreamX instance
 *  - src/test/resources/application-integration.properties file with the following settings (sample values given for demonstration)
 *     aem.author.url=http://localhost:4502
 *     aem.username=admin
 *     aem.password=admin
 *     page.parent.relative.path=/content/mysite/us/pl
 *     page.resource.type=mysite/components/page
 *     streamx.web.server.sink.url=http://localhost:8081
 */
@Disabled("To be run only manually on demand")
public class AemConnectorIntegrationTest {

  private static final Properties properties = loadProperties("src/test/resources/application-integration.properties");

  private static final String AEM_AUTHOR_URL = properties.getProperty("aem.author.url");
  private static final String AEM_USERNAME = properties.getProperty("aem.username");
  private static final String AEM_PASSWORD = properties.getProperty("aem.password");
  private static final String STREAMX_WEB_SERVER_SINK_URL = properties.getProperty("streamx.web.server.sink.url");
  private static final String PAGE_PARENT_RELATIVE_PATH = properties.getProperty("page.parent.relative.path");
  private static final String PAGE_RESOURCE_TYPE = properties.getProperty("page.resource.type");

  @Test
  public void testPageCreationAndPublish() throws Exception {
    // given
    final int pageNum = 10000 + SecureRandom.getInstanceStrong().nextInt(10000);
    final String pageRelativePath = PAGE_PARENT_RELATIVE_PATH + "/test-page" + pageNum;
    final String pageAbsoluteUrl = AEM_AUTHOR_URL + pageRelativePath;
    final String pageTitle = "Test Page " + pageNum;

    // create page
    executeRequest(pageAbsoluteUrl, Map.of(
        "jcr:primaryType", "cq:Page"
    ));

    // add page content
    executeRequest(pageAbsoluteUrl + "/jcr:content", Map.of(
        "jcr:primaryType", "cq:PageContent",
        "sling:resourceType", PAGE_RESOURCE_TYPE,
        "jcr:title", pageTitle
    ));

    // when: publish (replicate) the page
    executeRequest(AEM_AUTHOR_URL + "/bin/replicate.json", Map.of(
        "cmd", "activate",
        "path", pageRelativePath
    ));

    // then:
    String expectedStreamxPageUrl = STREAMX_WEB_SERVER_SINK_URL + pageRelativePath + ".html";
    waitUntil(() ->
        assertThat(downloadPage(expectedStreamxPageUrl))
            .contains("<title>" + pageTitle + "</title>")
    );

    // when: unpublish (deactivate) the page
    executeRequest(AEM_AUTHOR_URL + "/bin/replicate.json", Map.of(
        "cmd", "deactivate",
        "path", pageRelativePath
    ));

    // then:
    waitUntil(() ->
        assert404(expectedStreamxPageUrl)
    );
  }

  private static void executeRequest(String url, Map<String, String> params) throws IOException {
    HttpPost request = new HttpPost(url);
    List<NameValuePair> paramsList = toParamsList(params);
    request.setEntity(new UrlEncodedFormEntity(paramsList));

    try (CloseableHttpClient client = createAemHttpClient()) {
      client.execute(request, response -> {
        int status = response.getStatusLine().getStatusCode();
        String responseBody = IOUtils.toString(response.getEntity().getContent(), UTF_8);
        System.out.println("HTTP " + status + "\n" + responseBody);
        if (status / 100 != 2) {
          throw new RuntimeException("Failure");
        }
        return null;
      });
    }
  }

  private static List<NameValuePair> toParamsList(Map<String, String> params) {
    return params.entrySet().stream().map(
        entry -> new BasicNameValuePair(entry.getKey(), entry.getValue())
    ).collect(Collectors.toList());
  }

  private static CloseableHttpClient createAemHttpClient() {
    CredentialsProvider aemCredentialsProvider = new BasicCredentialsProvider();
    aemCredentialsProvider.setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(AEM_USERNAME, AEM_PASSWORD)
    );
    return HttpClients.custom()
        .setDefaultCredentialsProvider(aemCredentialsProvider)
        .build();
  }

  private static String downloadPage(String url) throws IOException {
    AtomicReference<String> resultHtml = new AtomicReference<>(null);
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      client.execute(new HttpGet(url), response -> {
        int status = response.getStatusLine().getStatusCode();
        assertThat(status).isEqualTo(200);
        String responseBody = IOUtils.toString(response.getEntity().getContent(), UTF_8);
        resultHtml.set(responseBody);
        return null;
      });
    }
    return resultHtml.get();
  }

  private static void assert404(String url) throws IOException {
    try (CloseableHttpClient client = HttpClients.createDefault()) {
      client.execute(new HttpGet(url), response -> {
        int status = response.getStatusLine().getStatusCode();
        assertThat(status).isEqualTo(404);
        return null;
      });
    }
  }

  private static void waitUntil(ThrowingRunnable assertion) {
    await().atMost(Duration.ofSeconds(3)).untilAsserted(assertion);
  }

  private static Properties loadProperties(String filePath) {
    Properties properties = new Properties();
    try (InputStream inputStream = new FileInputStream(filePath)) {
      properties.load(inputStream);
      return properties;
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
