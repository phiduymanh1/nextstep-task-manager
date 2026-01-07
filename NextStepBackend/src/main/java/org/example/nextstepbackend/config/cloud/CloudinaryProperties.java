package org.example.nextstepbackend.config.cloud;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "cloudinary")
@Data
@Component
public class CloudinaryProperties {
  private String cloudName;
  private String apiKey;
  private String apiSecret;
}
