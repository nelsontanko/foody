package dev.core.config;

import dev.FoodyWebIntegrationTest;
import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

import java.util.Comparator;

public class SpringBootTestClassOrderer implements ClassOrderer {

    private static int getOrder(ClassDescriptor classDescriptor) {
        if (classDescriptor.findAnnotation(FoodyWebIntegrationTest.class).isPresent()) {
            return 2;
        }
        return 1;
    }

    @Override
    public void orderClasses(ClassOrdererContext context) {
        context.getClassDescriptors().sort(Comparator.comparingInt(SpringBootTestClassOrderer::getOrder));
    }
}
