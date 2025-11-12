package com.streamx.blueprints.data;

import com.streamx.aem.connector.blueprints.ByteBuffers;
import java.nio.ByteBuffer;

/**
 * Represents object containing content.
 */
abstract class Resource {

  private final ByteBuffer content;
  private final String type;

  public Resource(ByteBuffer content, String type) {
    this.content = content;
    this.type = type;
  }

  public Resource(String content, String type) {
    this(ByteBuffers.fromString(content), type);
  }

  public ByteBuffer getContent() {
    return content;
  }

  public String getType() {
    return type;
  }
}
