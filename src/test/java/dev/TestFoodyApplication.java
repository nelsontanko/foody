package dev;

import org.springframework.boot.SpringApplication;

/**
* @author Nelson Tanko
*/
public class TestFoodyApplication {
    public static void main(String[] args) {
        SpringApplication.from(FoodyApplication::main)
                .with(TestContainersConfiguration.class).run(args);
    }
}
