package org.example.nextstepbackend.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

@Configuration
public class MessageConfig {

  /** Message source bean for internationalization */
  @Bean
  public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();

    ms.setBasename("classpath:messages");
    ms.setDefaultEncoding("UTF-8");
    ms.setFallbackToSystemLocale(true);

    return ms;
  }
}
