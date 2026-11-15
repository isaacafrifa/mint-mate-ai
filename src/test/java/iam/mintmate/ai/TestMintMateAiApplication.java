package iam.mintmate.ai;

import org.springframework.boot.SpringApplication;

public class TestMintMateAiApplication {

    public static void main(String[] args) {
        SpringApplication.from(MintMateAiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
