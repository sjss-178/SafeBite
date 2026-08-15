package com.example.safe.bite.service;


import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GraphService {

    private final Driver driver;

    public GraphService(Driver driver) {
        this.driver = driver;
    }

    // -------------------------------------------------------------------------
    // 1. Single-Hop Query: Fetch All Base Data
    // -------------------------------------------------------------------------

    /**
     * Retrieve all unique menu items for populating UI catalog
     */
    public List<Map<String, Object>> getAllMenuItems() {
        String cypher = """
            MATCH (m:MenuItem)
            RETURN m.name AS name, m.category AS category, m.price AS price
            ORDER BY m.name ASC
            """;

        try (Session session = driver.session()) {
            return session.run(cypher)
                    .list(record -> Map.of(
                            "name", record.get("name").asString(),
                            "category", record.get("category").asString("Uncategorized"),
                            "price", record.get("price").asDouble(0.0)
                    ));
        }
    }

    /**
     * Retrieve all known allergens for the filter checklist
     */
    public List<Map<String, Object>> getAllAllergens() {
        String cypher = """
            MATCH (a:Allergen)
            RETURN a.name AS name, a.severity AS severity
            ORDER BY a.name ASC
            """;

        try (Session session = driver.session()) {
            return session.run(cypher)
                    .list(record -> Map.of(
                            "name", record.get("name").asString(),
                            "severity", record.get("severity").asString("Unknown")
                    ));
        }
    }

    /**
     * Single-hop query: Direct ingredients of a specific dish
     */
    public List<Map<String, Object>> getDirectIngredients(String menuItemName) {
        String cypher = """
            MATCH (m:MenuItem {name: $menuItemName})-[:CONTAINS]->(i:Ingredient)
            RETURN i.name AS ingredientName, i.type AS ingredientType
            ORDER BY i.name ASC
            """;

        try (Session session = driver.session()) {
            return session.run(cypher, Values.parameters("menuItemName", menuItemName))
                    .list(record -> Map.of(
                            "ingredientName", record.get("ingredientName").asString(),
                            "ingredientType", record.get("ingredientType").asString("Base")
                    ));
        }
    }

    // -------------------------------------------------------------------------
    // 2. Multi-Hop Queries (> 2 Hops)
    // -------------------------------------------------------------------------

    /**
     * 3-Hop Traversal: MenuItem -> Intermediate Ingredient -> Base Ingredient -> Allergen
     */
    public List<Map<String, Object>> traceAllergenPath(String menuItemName, String allergenName) {
        String cypher = """
            MATCH (m:MenuItem {name: $menuItemName})-[:CONTAINS]->(inter:Ingredient)-[:CONTAINS]->(base:Ingredient)-[:HAS_ALLERGEN]->(a:Allergen {name: $allergenName})
            RETURN inter.name AS intermediateIngredient,
                   base.name AS baseIngredient,
                   a.name AS allergenName,
                   a.severity AS severity
            """;

        try (Session session = driver.session()) {
            return session.run(cypher, Values.parameters(
                    "menuItemName", menuItemName,
                    "allergenName", allergenName
            )).list(record -> Map.of(
                    "intermediateIngredient", record.get("intermediateIngredient").asString(),
                    "baseIngredient", record.get("baseIngredient").asString(),
                    "allergenName", record.get("allergenName").asString(),
                    "severity", record.get("severity").asString()
            ));
        }
    }

    /**
     * 4-Hop Traversal: MenuItem -> Intermediate -> Base -> Supplier -> Facility
     * Audits menu items impacted by facility contamination
     */
    public List<Map<String, Object>> getFacilityImpact(String facilityName) {
        String cypher = """
            MATCH (m:MenuItem)-[:CONTAINS]->(inter:Ingredient)-[:CONTAINS]->(base:Ingredient)-[:SOURCED_FROM]->(s:Supplier)-[:OPERATES_IN]->(f:Facility {name: $facilityName})
            RETURN DISTINCT m.name AS affectedMenuItem,
                            inter.name AS intermediateIngredient,
                            base.name AS baseIngredient,
                            s.name AS supplierName
            """;

        try (Session session = driver.session()) {
            return session.run(cypher, Values.parameters("facilityName", facilityName))
                    .list(record -> Map.of(
                            "affectedMenuItem", record.get("affectedMenuItem").asString(),
                            "intermediateIngredient", record.get("intermediateIngredient").asString(),
                            "baseIngredient", record.get("baseIngredient").asString(),
                            "supplierName", record.get("supplierName").asString()
                    ));
        }
    }

    // -------------------------------------------------------------------------
    // 3. Awkward SQL Query: Variable-Length Path Traversal (1..4 Hops)
    // -------------------------------------------------------------------------

    /**
     * Variable-depth search: Scans 1 to 4 hops deep to find all unsafe menu items
     * matching any selected allergen.
     */
    public List<Map<String, Object>> getUnsafeMenuItems(List<String> allergenNames) {
        String cypher = """
            MATCH path = (m:MenuItem)-[:CONTAINS*1..4]->(i:Ingredient)-[:HAS_ALLERGEN]->(a:Allergen)
            WHERE a.name IN $allergens
            RETURN DISTINCT m.name AS unsafeMenuItem,
                            a.name AS triggeredAllergen,
                            length(path) AS depthHops
            ORDER BY depthHops ASC, unsafeMenuItem ASC
            """;

        try (Session session = driver.session()) {
            return session.run(cypher, Values.parameters("allergens", allergenNames))
                    .list(record -> Map.of(
                            "unsafeMenuItem", record.get("unsafeMenuItem").asString(),
                            "triggeredAllergen", record.get("triggeredAllergen").asString(),
                            "depthHops", record.get("depthHops").asInt()
                    ));
        }
    }
    /**
     * Anti-Pattern Matching: Recommends safe alternatives in a specific category
     * by explicitly excluding any path that leads to a selected allergen.
     */
    public List<Map<String, Object>> getSafeAlternatives(String category, List<String> allergens) {
        String cypher = """
            MATCH (m:MenuItem {category: $categoryName})
            WHERE NOT EXISTS {
                MATCH (m)-[:CONTAINS*1..4]->(:Ingredient)-[:HAS_ALLERGEN]->(a:Allergen)
                WHERE a.name IN $allergens
            }
            RETURN m.name AS safeAlternative, m.price AS price
            ORDER BY m.price ASC
            """;

        try (Session session = driver.session()) {
            return session.run(cypher, Values.parameters(
                    "categoryName", category,
                    "allergens", allergens
            )).list(record -> Map.of(
                    "safeAlternative", record.get("safeAlternative").asString(),
                    "price", record.get("price").asDouble()
            ));
        }
    }
}