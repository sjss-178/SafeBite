package com.example.safe.bite.controllers;


import com.example.safe.bite.service.GraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/safebite")
@CrossOrigin(origins = "*") // Allows local React dev server (e.g., Vite on port 5173)
public class GraphController {

    private final GraphService graphService;

    public GraphController(GraphService graphService) {
        this.graphService = graphService;
    }

    /**
     * GET /api/safebite/menu
     */
    @GetMapping("/menu")
    public ResponseEntity<List<Map<String, Object>>> getMenuItems() {
        return ResponseEntity.ok(graphService.getAllMenuItems());
    }

    /**
     * GET /api/safebite/allergens
     */
    @GetMapping("/allergens")
    public ResponseEntity<List<Map<String, Object>>> getAllergens() {
        return ResponseEntity.ok(graphService.getAllAllergens());
    }

    /**
     * GET /api/safebite/menu/{itemName}/ingredients
     */
    @GetMapping("/menu/{itemName}/ingredients")
    public ResponseEntity<List<Map<String, Object>>> getDirectIngredients(@PathVariable String itemName) {
        return ResponseEntity.ok(graphService.getDirectIngredients(itemName));
    }

    /**
     * GET /api/safebite/trace?item=Signature%20SafeBite%20Burger&allergen=Peanuts
     */
    @GetMapping("/trace")
    public ResponseEntity<List<Map<String, Object>>> traceAllergen(
            @RequestParam String item,
            @RequestParam String allergen) {
        return ResponseEntity.ok(graphService.traceAllergenPath(item, allergen));
    }

    /**
     * GET /api/safebite/facility-impact?facility=Alpha%20Processing%20Plant
     */
    @GetMapping("/facility-impact")
    public ResponseEntity<List<Map<String, Object>>> getFacilityImpact(@RequestParam String facility) {
        return ResponseEntity.ok(graphService.getFacilityImpact(facility));
    }

    /**
     * GET /api/safebite/unsafe?allergens=Peanuts,Dairy
     */
    @GetMapping("/unsafe")
    public ResponseEntity<List<Map<String, Object>>> getUnsafeMenuItems(
            @RequestParam(required = false, defaultValue = "") List<String> allergens) {
        if (allergens.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(graphService.getUnsafeMenuItems(allergens));
    }
    /**
     * GET /api/safebite/safe-alternatives?category=Main%20Course&allergens=Peanuts,Dairy
     */
    @GetMapping("/safe-alternatives")
    public ResponseEntity<List<Map<String, Object>>> getSafeAlternatives(
            @RequestParam String category,
            @RequestParam(required = false, defaultValue = "") List<String> allergens) {

        // If no allergens are selected, the query will naturally return all items in the category
        return ResponseEntity.ok(graphService.getSafeAlternatives(category, allergens));
    }
}
