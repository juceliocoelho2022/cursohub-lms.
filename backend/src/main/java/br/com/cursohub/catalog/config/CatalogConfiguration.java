package br.com.cursohub.catalog.config;

import br.com.cursohub.catalog.application.CatalogService;
import br.com.cursohub.catalog.application.port.in.CatalogQueryUseCase;
import br.com.cursohub.catalog.application.port.out.CourseReaderPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Composition root: Spring injeta o adaptador na porta de saída. */
@Configuration
public class CatalogConfiguration {
    @Bean
    CatalogQueryUseCase catalogQueryUseCase(CourseReaderPort courseReader) {
        return new CatalogService(courseReader);
    }
}
