package com.streamx.blueprints.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.ByteBuffer;

/**
 * Represents data object.
 */
public class Data extends Resource {

  public static final String TYPE_PUBLISHED = "com.streamx.blueprints.data.published.v1";
  public static final String TYPE_UNPUBLISHED = "com.streamx.blueprints.data.unpublished.v1";

  @JsonCreator
  public Data(@JsonProperty("content") ByteBuffer content, @JsonProperty("type") String type) {
    super(content, type);
  }

  public Data(String content, String type) {
    super(content, type);
  }
}
