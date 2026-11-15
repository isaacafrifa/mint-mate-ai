package iam.mintmate.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LiquibaseSchemaVerificationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void liquibaseCreatesCategoryAndTransactionTables() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            assertTrue(tableExists(metaData, "category"));
            assertTrue(tableExists(metaData, "transaction"));
        }
    }

    private boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet tables = metaData.getTables(null, "public", tableName, new String[] {"TABLE"})) {
            return tables.next();
        }
    }
}
