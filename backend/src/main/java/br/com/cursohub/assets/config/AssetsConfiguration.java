package br.com.cursohub.assets.config;
import br.com.cursohub.assets.application.CourseAssetService;
import br.com.cursohub.assets.application.port.in.CourseAssetUseCase;
import br.com.cursohub.assets.application.port.out.CourseAssetStorePort;
import org.springframework.context.annotation.*;
@Configuration
public class AssetsConfiguration {
    @Bean CourseAssetUseCase courseAssetUseCase(CourseAssetStorePort store) {
        return new CourseAssetService(store);
    }
}
