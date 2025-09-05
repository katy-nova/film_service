package com.example.films;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.SQLException;

@SpringBootTest
class FilmorateApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(FilmorateApplicationTests.class);

	@Autowired(required = false)
	private DataSource dataSource;

	@Test
	void contextLoads() {
		logger.info("Starting contextLoads test...");
		try {
			if (dataSource != null) {
				logger.info("Data source is available: " + dataSource.getConnection().getMetaData().getURL());
			} else {
				logger.warn("Data source is not available.");
			}
		} catch (SQLException e) {
			logger.error("Failed to connect to database: " + e.getMessage(), e);
		}
		logger.info("contextLoads test finished.");
	}
}

