package br.com.cursohub.learning.config;

import br.com.cursohub.learning.application.StudentLearningService;
import br.com.cursohub.learning.application.port.in.StudentLearningUseCase;
import br.com.cursohub.learning.application.port.out.LearningContentReaderPort;
import br.com.cursohub.learning.application.port.out.LessonProgressWriterPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StudentLearningConfiguration {
    @Bean
    StudentLearningUseCase studentLearningUseCase(LearningContentReaderPort reader, LessonProgressWriterPort writer) {
        return new StudentLearningService(reader, writer);
    }
}
