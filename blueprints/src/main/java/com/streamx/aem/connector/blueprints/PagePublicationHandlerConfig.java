package com.streamx.aem.connector.blueprints;

import com.day.cq.wcm.api.NameConstants;
import com.streamx.blueprints.data.Page;
import org.apache.jackrabbit.JcrConstants;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Blueprints - Page Publication Handler Config")
public @interface PagePublicationHandlerConfig {

  @AttributeDefinition(
      name = "Publish Event Type",
      description = "Publish event type of Pages to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Page.TYPE_PUBLISHED
  )
  String publish_event_type() default Page.TYPE_PUBLISHED;

  @AttributeDefinition(
      name = "Unpublish Event Type",
      description = "Unpublish event type of Pages to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Page.TYPE_UNPUBLISHED
  )
  String unpublish_event_type() default Page.TYPE_UNPUBLISHED;

  @AttributeDefinition(
      name = "Is Enabled?",
      description = "Indicates whether the configured service is enabled "
          + "and must undertake the supposed actions or not.",
      type = AttributeType.BOOLEAN,
      defaultValue = "true"
  )
  boolean enabled() default true;

  @AttributeDefinition(
      name = "Relative Path to Node with JCR Property for StreamX resource type",
      description = "Relative path to the child node of the resource that is being published, that "
          + "is expected to have a JCR property for StreamX resource type.",
      type = AttributeType.STRING,
      defaultValue = JcrConstants.JCR_PRIMARYTYPE
  )
  String rel_path_to_node_with_jcr_prop_for_streamx_resource_type() default JcrConstants.JCR_CONTENT;

  @AttributeDefinition(
      name = "Name of JCR property for StreamX resource type",
      description = "If the resource that is being published has a respective child with a JCR "
          + "property with the specified name, the value of that property will be set as a "
          + "value of the StreamX resource type property of the message ingested into StreamX.",
      type = AttributeType.STRING,
      defaultValue = NameConstants.PN_TEMPLATE
  )
  String jcr_prop_name_for_streamx_resource_type() default NameConstants.PN_TEMPLATE;
}
