package com.streamx.blueprints.data;

import com.streamx.aem.connector.blueprints.ByteBuffers;
import java.nio.ByteBuffer;

/**
 * Represents object containing information how to render data. See {@link RenderingContext}.
 */
public class Renderer {

  public static final String TYPE_PUBLISHED = "com.streamx.blueprints.renderer.published.v1";
  public static final String TYPE_UNPUBLISHED = "com.streamx.blueprints.renderer.unpublished.v1";

  private final ByteBuffer template;

  public Renderer(ByteBuffer template) {
    this.template = template;
  }

  public Renderer(String content) {
    this(ByteBuffers.fromString(content));
  }

  public ByteBuffer getTemplate() {
    return template;
  }

}
