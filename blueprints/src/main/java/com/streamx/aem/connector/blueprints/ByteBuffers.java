package com.streamx.aem.connector.blueprints;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;

public final class ByteBuffers {

  private ByteBuffers() {
    // no instances
  }

  public static ByteBuffer fromString(String content) {
    if (content == null) {
      return null;
    }
    return ByteBuffer.wrap(content.getBytes(UTF_8));
  }

}
