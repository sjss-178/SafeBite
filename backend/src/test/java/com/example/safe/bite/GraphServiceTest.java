package com.example.safe.bite;

import com.example.safe.bite.service.GraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @Mock
    private Driver driver;

    @Mock
    private Session session;

    @Mock
    private Result result;

    @InjectMocks
    private GraphService graphService;

    @Captor
    private ArgumentCaptor<String> cypherCaptor;

    @BeforeEach
    void setUp() {
        // Whenever the service calls driver.session(), return our mocked session
        lenient().when(driver.session()).thenReturn(session);
    }

    @Test
    void shouldFetchAllMenuItems() {
        // Arrange
        // We mock result.list() directly to bypass the internal Record mapping logic for cleaner unit tests
        List<Map<String, Object>> mockResponse = List.of(Map.of("name", "Burger"));
        when(session.run(anyString())).thenReturn(result);
        when(result.list(any())).thenReturn(Collections.singletonList(mockResponse));

        // Act
        List<Map<String, Object>> menuItems = graphService.getAllMenuItems();

        // Assert
        assertEquals(1, menuItems.size());

        // Verify that the correct Cypher query was passed to the session
        verify(session).run(cypherCaptor.capture());
        String executedQuery = cypherCaptor.getValue();
        assert(executedQuery.contains("MATCH (m:MenuItem)"));
        assert(executedQuery.contains("RETURN m.name AS name"));
    }

    @Test
    void shouldFetchUnsafeMenuItemsWithParameterizedQuery() {
        // Arrange
        List<String> selectedAllergens = List.of("Dairy", "Peanuts");

        // Ensure this is a List containing a Map, NOT a List containing a List!
        List<Map<String, Object>> mockResponse = List.of(
                Map.of("unsafeMenuItem", "Ice Cream", "depthHops", 1)
        );

        // Mock the session.run() method
        when(session.run(anyString(), any(Value.class))).thenReturn(result);

        // THE FIX: Explicitly cast the any() matcher to Function.class
        // This stops Mockito from misinterpreting the generic return types
        when(result.list(any(java.util.function.Function.class))).thenReturn(mockResponse);

        // Act
        List<Map<String, Object>> unsafeItems = graphService.getUnsafeMenuItems(selectedAllergens);

        // Assert
        assertEquals(1, unsafeItems.size());

        // This will now safely cast to a Map and retrieve the value!
        assertEquals("Ice Cream", unsafeItems.get(0).get("unsafeMenuItem"));

        // Verify the Cypher query contains the crucial multi-hop syntax
        verify(session).run(cypherCaptor.capture(), any(Value.class));
        String executedQuery = cypherCaptor.getValue();
        assert(executedQuery.contains("MATCH path = (m:MenuItem)-[:CONTAINS*1..4]->"));
        assert(executedQuery.contains("WHERE a.name IN $allergens"));
    }
}