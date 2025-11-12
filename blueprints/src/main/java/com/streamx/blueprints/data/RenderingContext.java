package com.streamx.blueprints.data;

/**
 * Represents configuration defining what should be rendered. Relates to {@link Renderer} and
 * {@link Data}.
 */
public class RenderingContext {

  public static final String TYPE_PUBLISHED =
      "com.streamx.blueprints.rendering-context.published.v1";
  public static final String TYPE_UNPUBLISHED =
      "com.streamx.blueprints.rendering-context.unpublished.v1";

  private final String rendererKey;
  private final String dataKeyMatchPattern;
  private final String dataTypeMatchPattern;
  private final String outputKeyTemplate;
  private final String outputTypeTemplate;
  private final OutputFormat outputFormat;

  public RenderingContext(
      String rendererKey,
      String dataKeyMatchPattern,
      String dataTypeMatchPattern,
      String outputKeyTemplate,
      String outputTypeTemplate,
      OutputFormat outputFormat) {
    this.rendererKey = rendererKey;
    this.dataKeyMatchPattern = dataKeyMatchPattern;
    this.dataTypeMatchPattern = dataTypeMatchPattern;
    this.outputKeyTemplate = outputKeyTemplate;
    this.outputTypeTemplate = outputTypeTemplate;
    this.outputFormat = outputFormat;
  }

  public String getRendererKey() {
    return rendererKey;
  }

  public String getDataKeyMatchPattern() {
    return dataKeyMatchPattern;
  }

  public String getDataTypeMatchPattern() {
    return dataTypeMatchPattern;
  }

  public String getOutputKeyTemplate() {
    return outputKeyTemplate;
  }

  public String getOutputTypeTemplate() {
    return outputTypeTemplate;
  }

  public OutputFormat getOutputFormat() {
    return outputFormat;
  }

  public enum OutputFormat {
    PAGE, FRAGMENT
  }
}
