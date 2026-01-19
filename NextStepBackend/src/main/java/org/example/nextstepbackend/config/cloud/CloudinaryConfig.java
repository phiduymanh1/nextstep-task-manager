package org.example.nextstepbackend.config.cloud;

import com.cloudinary.Cloudinary;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CloudinaryConfig {

  private final CloudinaryProperties props;

  /** Cloudinary bean */
  @Bean
  public Cloudinary cloudinary() {
    return new Cloudinary(
        Map.of(
            "cloud_name", props.getCloudName(),
            "api_key", props.getApiKey(),
            "api_secret", props.getApiSecret()));
  }
}
