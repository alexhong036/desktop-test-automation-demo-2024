package de.arvato.aep.automation.template.impl.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties
@ConstructorBinding
@ConfigurationProperties(prefix = "template")
public class TemplateProperties {

  private final String configValue;

  /**
   * @param configValue This will be set by spring
   */
  public TemplateProperties(String configValue) {
    this.configValue = configValue;
  }

  public String getConfigValue() {
    return configValue;
  }
}
