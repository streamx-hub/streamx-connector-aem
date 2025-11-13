package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.sling.connector.PublicationHandler;
import com.streamx.sling.connector.PublishData;
import com.streamx.sling.connector.ResourceInfo;
import com.streamx.sling.connector.UnpublishData;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BasePublicationHandler<T> implements PublicationHandler<T> {

  private static final Logger LOG = LoggerFactory.getLogger(BasePublicationHandler.class);

  private final ResourceResolverFactory resolverFactory;

  BasePublicationHandler(ResourceResolverFactory resolverFactory) {
    this.resolverFactory = resolverFactory;
  }

  @Override
  public final String getId() {
    return this.getClass().getSimpleName();
  }

  @Override
  public final PublishData<T> getPublishData(ResourceInfo resourceInfo) {
    String resourcePath = resourceInfo.getPath();
    try (ResourceResolver resourceResolver = createResourceResolver()) {
      Resource resource = resourceResolver.getResource(resourcePath);
      if (resource == null) {
        LOG.error("Resource not found when trying to publish it: {}", resourcePath);
        return null;
      }

      T payload = getPublishPayload(resourceInfo, resource, resourceResolver);
      if (payload == null) {
        return null;
      }

      return new PublishData<>(
          getPublicationSubject(resourcePath),
          getPublishEventType(),
          payload
      );
    }
  }

  @Override
  public final UnpublishData<T> getUnpublishData(ResourceInfo resourceInfo) {
    return new UnpublishData<>(
        getPublicationSubject(resourceInfo.getPath()),
        getUnpublishEventType(),
        getUnpublishPayload(resourceInfo)
    );
  }

  protected String getPublicationSubject(String resourcePath) {
    return resourcePath;
  }

  protected abstract String getPublishEventType();

  protected abstract String getUnpublishEventType();

  protected abstract T getPublishPayload(ResourceInfo resourceInfo, Resource resource, ResourceResolver resourceResolver);

  protected abstract T getUnpublishPayload(ResourceInfo resourceInfo);

  protected ResourceResolver createResourceResolver() {
    try {
      return resolverFactory.getAdministrativeResourceResolver(null);
    } catch (LoginException e) {
      throw new IllegalStateException("Cannot create ResourceResolver", e);
    }
  }

  protected static String readProperty(ResourceInfo resourceInfo, String parent, String child) {
    return readProperty(resourceInfo, parent + "/" + child);
  }

  protected static String readProperty(ResourceInfo resourceInfo, String path) {
    return resourceInfo.getProperty(path);
  }
}
