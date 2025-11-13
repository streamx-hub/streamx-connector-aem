package com.streamx.aem.connector.blueprints.tools;

import com.drew.lang.annotations.Nullable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.uri.SlingUri;
import org.apache.sling.api.uri.SlingUriBuilder;

public final class SlingUriCreator {

  private SlingUriCreator() {
    // no instances
  }

  public static SlingUri create(Resource resource, String[] selectors, @Nullable String extension) {
    return SlingUriBuilder.createFrom(resource)
        .setSelectors(selectors)
        .setExtension(extension)
        .build();
  }

  public static SlingUri create(String resourcePath, String[] selectors, @Nullable String extension) {
    return SlingUriBuilder.parse(resourcePath, null)
        .setSelectors(selectors)
        .setExtension(extension)
        .build();
  }
}
