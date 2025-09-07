package org.example.graduationproject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private StringToMaGiamGiaConverter stringToMaGiamGiaConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(stringToMaGiamGiaConverter);
    }
}
