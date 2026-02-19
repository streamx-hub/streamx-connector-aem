package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.sling.connector.PublicationHandler;
import com.streamx.sling.connector.PublishData;
import com.streamx.sling.connector.ResourceInfo;
import com.streamx.sling.connector.UnpublishData;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class BasePublicationHandler<T> implements PublicationHandler<T> {

  private static final Logger LOG = LoggerFactory.getLogger(BasePublicationHandler.class);

  private final ResourceResolverFactory resolverFactory;
  private final AtomicReference<String> publishEventType = new AtomicReference<>();
  private final AtomicReference<String> unpublishEventType = new AtomicReference<>();
  private final AtomicBoolean enabled = new AtomicBoolean();

  BasePublicationHandler(ResourceResolverFactory resolverFactory) {
    this.resolverFactory = resolverFactory;
  }

  protected void assignConfigValues(boolean enabled, String publishEventType, String unpublishEventType) {
    this.enabled.set(enabled);
    this.publishEventType.set(publishEventType);
    this.unpublishEventType.set(unpublishEventType);
  }

  @Override
  public final String getId() {
    return this.getClass().getSimpleName();
  }

  @Override
  public final boolean canHandle(ResourceInfo resourceInfo) {
    return enabled.get() && canHandleResource(resourceInfo);
  }

  protected abstract boolean canHandleResource(ResourceInfo resourceInfo);

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
          publishEventType.get(),
          payload
      );
    }
  }

  @Override
  public final UnpublishData<T> getUnpublishData(ResourceInfo resourceInfo) {
    return new UnpublishData<>(
        getPublicationSubject(resourceInfo.getPath()),
        unpublishEventType.get(),
        getUnpublishPayload(resourceInfo)
    );
  }

  protected String getPublicationSubject(String resourcePath) {
    return resourcePath;
  }

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
