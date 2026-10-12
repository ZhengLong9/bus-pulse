package sg.buspulse.api;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("local")
class DatabaseWiringTests {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    @Test
    void connectsToPostgresAndValidatesMigrationHistory() {
        assertThat(jdbcTemplate.queryForObject("SELECT version()", String.class))
                .startsWith("PostgreSQL");
        flyway.validate();
        assertThat(flyway.info().pending()).isEmpty();
    }
}
