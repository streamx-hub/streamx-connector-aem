package com.streamx.aem.connector.blueprints.tools;

import com.drew.lang.annotations.Nullable;
import com.streamx.sling.connector.util.SimpleInternalRequest;
import java.util.Map;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.uri.SlingUri;
import org.apache.sling.engine.SlingRequestProcessor;

public final class InternalRequestForPage {

  private InternalRequestForPage() {
    // no instances
  }

  public static String getHtml(Resource pageResource, ResourceResolver resourceResolver,
      SlingRequestProcessor slingRequestProcessor, Map<String, String> additionalProperties) {
    String[] selectors = FranklinCheck.isFranklinPage(pageResource) ? new String[]{"plain"} : new String[0];
    return getContent(pageResource, selectors, "html", resourceResolver, slingRequestProcessor, additionalProperties);
  }

  public static String getContent(Resource pageResource,
      String[] selectors, @Nullable String extension, ResourceResolver resourceResolver,
      SlingRequestProcessor slingRequestProcessor, Map<String, String> additionalProperties) {
    SlingUri slingUri = SlingUriCreator.create(pageResource, selectors, extension);
    return new SimpleInternalRequest(
        slingUri,
        slingRequestProcessor,
        resourceResolver,
        additionalProperties
    ).getResponseAsString();
  }
}
