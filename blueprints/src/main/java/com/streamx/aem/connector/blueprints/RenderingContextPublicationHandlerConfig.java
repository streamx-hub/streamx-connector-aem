package com.streamx.aem.connector.blueprints;

import com.streamx.blueprints.data.RenderingContext;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Blueprints - Rendering Context Publication Handler Config")
public @interface RenderingContextPublicationHandlerConfig {

  @AttributeDefinition(
      name = "Publish Event Type",
      description = "Publish event type of data to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = RenderingContext.TYPE_PUBLISHED
  )
  String publish_event_type() default RenderingContext.TYPE_PUBLISHED;

  @AttributeDefinition(
      name = "Unpublish Event Type",
      description = "Unpublish event type of data to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = RenderingContext.TYPE_UNPUBLISHED
  )
  String unpublish_event_type() default RenderingContext.TYPE_UNPUBLISHED;

  @AttributeDefinition(
      name = "Is Enabled?",
      description = "Indicates whether the configured service is enabled "
          + "and must undertake the supposed actions or not.",
      type = AttributeType.BOOLEAN,
      defaultValue = "true"
  )
  boolean enabled() default true;
}
