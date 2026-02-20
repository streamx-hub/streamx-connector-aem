package com.streamx.aem.connector.blueprints.handlers;

import com.streamx.blueprints.data.Renderer;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Blueprints - Renderer Publication Handler Config")
public @interface RendererPublicationHandlerConfig {

  @AttributeDefinition(
      name = "Publish Event Type",
      description = "Publish event type of Renderers to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Renderer.TYPE_PUBLISHED
  )
  String publish_event_type() default Renderer.TYPE_PUBLISHED;

  @AttributeDefinition(
      name = "Unpublish Event Type",
      description = "Unpublish event type of Renderers to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Renderer.TYPE_UNPUBLISHED
  )
  String unpublish_event_type() default Renderer.TYPE_UNPUBLISHED;

  @AttributeDefinition(name = "Should add namespace prefix")
  boolean should_add_namespace_prefix() default false;

  @AttributeDefinition(name = "Namespace prefix")
  String namespace_prefix() default "";

  @AttributeDefinition(
      name = "Is Enabled?",
      description = "Indicates whether the configured service is enabled "
          + "and must undertake the supposed actions or not.",
      type = AttributeType.BOOLEAN,
      defaultValue = "true"
  )
  boolean enabled() default true;
}
