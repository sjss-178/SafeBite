package com.example.safe.bite.seed;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class SeedDataRunner implements CommandLineRunner {

    private final Driver driver;

    public SeedDataRunner(Driver driver) {
        this.driver = driver;
    }

    @Override
    public void run(String... args) {
        try (Session session = driver.session()) {
            // 1. Clear existing data for a clean slate
            session.run("MATCH (n) DETACH DELETE n");

            // 2. Read the Cypher query from the resources folder
            ClassPathResource resource = new ClassPathResource("seed.cypher");
            String seedQuery = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // 3. Execute the file's contents
            session.run(seedQuery);
            System.out.println("✅ Seed data successfully loaded from seed.cypher into CognoDB.");

        } catch (Exception e) {
            System.err.println("❌ Failed to read or execute the seed file: " + e.getMessage());
        }
    }
}