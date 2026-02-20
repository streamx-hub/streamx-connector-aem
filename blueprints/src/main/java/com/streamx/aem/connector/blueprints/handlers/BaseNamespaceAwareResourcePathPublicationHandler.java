package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.aem.connector.blueprints.tools.NamespacePrefixAdder;
import com.streamx.sling.connector.PublicationData;
import com.streamx.sling.connector.PublishData;
import com.streamx.sling.connector.ResourceInfo;
import com.streamx.sling.connector.StreamxPublicationException;
import com.streamx.sling.connector.UnpublishData;
import com.streamx.sling.connector.handlers.resourcepath.ResourcePathPublicationHandler;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.engine.SlingRequestProcessor;

abstract class BaseNamespaceAwareResourcePathPublicationHandler<T> extends
    ResourcePathPublicationHandler<T>{

  private final AtomicBoolean shouldAddNamespacePrefix = new AtomicBoolean();
  private final AtomicReference<String> namespacePrefix = new AtomicReference<>();

  protected BaseNamespaceAwareResourcePathPublicationHandler(
      ResourceResolverFactory resourceResolverFactory,
      SlingRequestProcessor slingRequestProcessor) {
    super(resourceResolverFactory, slingRequestProcessor);
  }

  protected void assignConfigValues(boolean shouldAddNamespacePrefix, String namespacePrefix) {
    this.shouldAddNamespacePrefix.set(shouldAddNamespacePrefix);
    this.namespacePrefix.set(namespacePrefix);
  }

  @Override
  public final PublishData<T> getPublishData(ResourceInfo resourceInfo)
      throws StreamxPublicationException {
    PublishData<T> publishData = super.getPublishData(resourceInfo);
    return new PublishData<>(
        getIngestionSubject(publishData),
        publishData.getEventType(),
        publishData.getData(),
        publishData.getExtensions()
    );
  }

  @Override
  public final UnpublishData<T> getUnpublishData(ResourceInfo resourceInfo) {
    UnpublishData<T> unpublishData = super.getUnpublishData(resourceInfo);
    return new UnpublishData<>(
        getIngestionSubject(unpublishData),
        unpublishData.getEventType(),
        unpublishData.getData(),
        unpublishData.getExtensions()
    );
  }

  private String getIngestionSubject(PublicationData<T> publicationData) {
    String subject = publicationData.getSubject();
    if (shouldAddNamespacePrefix.get()) {
      return NamespacePrefixAdder.add(subject, namespacePrefix.get());
    }
    return subject;
  }
}
