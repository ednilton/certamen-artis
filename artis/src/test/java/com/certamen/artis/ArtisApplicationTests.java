package com.certamen.artis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * Garante que o contexto completo sobe: Flyway aplica as migrations e o
 * Hibernate valida as entidades contra o schema real (ddl-auto: validate).
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ArtisApplicationTests {

    @Test
    void contextLoads() {
    }
}
