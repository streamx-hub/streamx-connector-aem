package com.streamx.aem.connector.blueprints;

import com.day.cq.wcm.api.NameConstants;
import com.streamx.blueprints.data.Data;
import org.apache.jackrabbit.JcrConstants;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Blueprints - Page Model Publication Handler Config")
public @interface PageModelPublicationHandlerConfig {

  @AttributeDefinition(
      name = "Publish Event Type",
      description = "Publish event type of data to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Data.TYPE_PUBLISHED
  )
  String publish_event_type() default Data.TYPE_PUBLISHED;

  @AttributeDefinition(
      name = "Unpublish Event Type",
      description = "Unpublish event type of data to be ingested by StreamX.",
      type = AttributeType.STRING,
      defaultValue = Data.TYPE_UNPUBLISHED
  )
  String unpublish_event_type() default Data.TYPE_UNPUBLISHED;

  @AttributeDefinition(
      name = "Is Enabled?",
      description = "Indicates whether the configured service is enabled "
          + "and must undertake the supposed actions or not.",
      type = AttributeType.BOOLEAN,
      defaultValue = "false"
  )
  boolean enabled() default false;

  @AttributeDefinition(
      name = "Page Resource Path Regex",
      description = "A regular expression used to match the resource path of pages that should be published or unpublished by this service.",
      type = AttributeType.STRING
  )
  String page_resource_path_regex();

  @AttributeDefinition(
      name = "Selectors to append to page resource path when retrieving page model",
      description = "One or more selector to append to page resource path when retrieving page model.",
      type = AttributeType.STRING,
      defaultValue = "model"
  )
  String[] selectors_to_append() default {"model"};

  @AttributeDefinition(
      name = "Extension to append to page resource path when retrieving page model",
      description = "Extension to append to page resource path when retrieving page model.",
      type = AttributeType.STRING,
      defaultValue = "json"
  )
  String extension_to_append() default "json";

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
          + "property with the specified name, the value of that property will be set as "
          + "the type of the Data resource ingested into StreamX.",
      type = AttributeType.STRING,
      defaultValue = NameConstants.PN_TEMPLATE
  )
  String jcr_prop_name_for_streamx_resource_type() default NameConstants.PN_TEMPLATE;
}
