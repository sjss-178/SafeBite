// ==========================================
// 1. CREATE ALLERGENS
// ==========================================
CREATE (a_peanut:Allergen {name: 'Peanuts', severity: 'High'})
CREATE (a_dairy:Allergen {name: 'Dairy', severity: 'Medium'})
CREATE (a_gluten:Allergen {name: 'Gluten', severity: 'Medium'})
CREATE (a_shellfish:Allergen {name: 'Shellfish', severity: 'High'})
CREATE (a_soy:Allergen {name: 'Soy', severity: 'Low'})

// ==========================================
// 2. CREATE FACILITIES & CROSS-CONTAMINATION
// ==========================================
CREATE (fac_alpha:Facility {name: 'Alpha Processing Plant', location: 'Chicago'})
CREATE (fac_beta:Facility {name: 'Beta Mills', location: 'Kansas'})
CREATE (fac_gamma:Facility {name: 'Gamma Seafood Hub', location: 'Seattle'})

// Map cross-contamination risks at the facility level
CREATE (fac_alpha)-[:PROCESSES_ALLERGEN]->(a_peanut)
CREATE (fac_alpha)-[:PROCESSES_ALLERGEN]->(a_dairy)
CREATE (fac_beta)-[:PROCESSES_ALLERGEN]->(a_gluten)
CREATE (fac_gamma)-[:PROCESSES_ALLERGEN]->(a_shellfish)

// ==========================================
// 3. CREATE SUPPLIERS
// ==========================================
CREATE (sup_global:Supplier {name: 'Global Spices & Oils'})
CREATE (sup_harvest:Supplier {name: 'Golden Harvest Grains'})
CREATE (sup_oceanic:Supplier {name: 'Oceanic Catch Inc.'})

// Link Suppliers to Facilities
CREATE (sup_global)-[:OPERATES_IN]->(fac_alpha)
CREATE (sup_harvest)-[:OPERATES_IN]->(fac_beta)
CREATE (sup_oceanic)-[:OPERATES_IN]->(fac_gamma)

// ==========================================
// 4. CREATE BASE INGREDIENTS (Level 3/4)
// ==========================================
CREATE (i_peanut_oil:Ingredient {name: 'Peanut Oil', type: 'Base'})
CREATE (i_wheat_flour:Ingredient {name: 'Wheat Flour', type: 'Base'})
CREATE (i_milk_powder:Ingredient {name: 'Milk Powder', type: 'Base'})
CREATE (i_shrimp_extract:Ingredient {name: 'Shrimp Extract', type: 'Base'})
CREATE (i_water:Ingredient {name: 'Purified Water', type: 'Base'})
CREATE (i_salt:Ingredient {name: 'Sea Salt', type: 'Base'})
CREATE (i_soy_lecithin:Ingredient {name: 'Soy Lecithin', type: 'Base'})

// Link Base Ingredients to Suppliers
CREATE (i_peanut_oil)-[:SOURCED_FROM]->(sup_global)
CREATE (i_milk_powder)-[:SOURCED_FROM]->(sup_global)
CREATE (i_wheat_flour)-[:SOURCED_FROM]->(sup_harvest)
CREATE (i_shrimp_extract)-[:SOURCED_FROM]->(sup_oceanic)
CREATE (i_soy_lecithin)-[:SOURCED_FROM]->(sup_harvest)

// Direct Allergen Links
CREATE (i_peanut_oil)-[:HAS_ALLERGEN]->(a_peanut)
CREATE (i_wheat_flour)-[:HAS_ALLERGEN]->(a_gluten)
CREATE (i_milk_powder)-[:HAS_ALLERGEN]->(a_dairy)
CREATE (i_shrimp_extract)-[:HAS_ALLERGEN]->(a_shellfish)
CREATE (i_soy_lecithin)-[:HAS_ALLERGEN]->(a_soy)

// ==========================================
// 5. CREATE INTERMEDIATE INGREDIENTS (Level 2)
// ==========================================
CREATE (i_brioche_bun:Ingredient {name: 'Brioche Bun', type: 'Intermediate'})
CREATE (i_secret_sauce:Ingredient {name: 'House Secret Sauce', type: 'Intermediate'})
CREATE (i_seafood_broth:Ingredient {name: 'Spicy Seafood Broth', type: 'Intermediate'})
CREATE (i_chocolate_glaze:Ingredient {name: 'Chocolate Glaze', type: 'Intermediate'})
CREATE (i_beef_patty:Ingredient {name: '100% Beef Patty', type: 'Intermediate'})
CREATE (i_ramen_noodles:Ingredient {name: 'Ramen Noodles', type: 'Intermediate'})

// Build the Deep Nesting (Intermediate -> Base)
CREATE (i_brioche_bun)-[:CONTAINS]->(i_wheat_flour)
CREATE (i_brioche_bun)-[:CONTAINS]->(i_milk_powder)
CREATE (i_brioche_bun)-[:CONTAINS]->(i_water)

CREATE (i_secret_sauce)-[:CONTAINS]->(i_peanut_oil)
CREATE (i_secret_sauce)-[:CONTAINS]->(i_salt)
CREATE (i_secret_sauce)-[:CONTAINS]->(i_water)

CREATE (i_seafood_broth)-[:CONTAINS]->(i_shrimp_extract)
CREATE (i_seafood_broth)-[:CONTAINS]->(i_salt)
CREATE (i_seafood_broth)-[:CONTAINS]->(i_water)

CREATE (i_chocolate_glaze)-[:CONTAINS]->(i_milk_powder)
CREATE (i_chocolate_glaze)-[:CONTAINS]->(i_soy_lecithin)

CREATE (i_ramen_noodles)-[:CONTAINS]->(i_wheat_flour)
CREATE (i_ramen_noodles)-[:CONTAINS]->(i_water)

// ==========================================
// 6. CREATE MENU ITEMS (Level 1)
// ==========================================
CREATE (m_burger:MenuItem {name: 'Signature SafeBite Burger', category: 'Main Course', price: 14.99})
CREATE (m_ramen:MenuItem {name: 'Oceanic Spicy Ramen', category: 'Main Course', price: 16.50})
CREATE (m_dessert:MenuItem {name: 'Lava Chocolate Cake', category: 'Dessert', price: 8.99})
CREATE (m_vegan_salad:MenuItem {name: 'Fresh Garden Salad', category: 'Appetizer', price: 9.00})

// Link Menu Items to Intermediate Ingredients
CREATE (m_burger)-[:CONTAINS]->(i_brioche_bun)
CREATE (m_burger)-[:CONTAINS]->(i_beef_patty)
CREATE (m_burger)-[:CONTAINS]->(i_secret_sauce) // Triggers Peanut allergy 3 hops down

CREATE (m_ramen)-[:CONTAINS]->(i_ramen_noodles)
CREATE (m_ramen)-[:CONTAINS]->(i_seafood_broth) // Triggers Shellfish allergy

CREATE (m_dessert)-[:CONTAINS]->(i_chocolate_glaze)
CREATE (m_dessert)-[:CONTAINS]->(i_wheat_flour) // Direct base ingredient link

// New Menu Items
CREATE (m_smoothie:MenuItem {name: 'Peanut Butter Power Smoothie', category: 'Beverage', price: 6.99})
CREATE (m_vegan_wrap:MenuItem {name: 'Spicy Tofu Wrap', category: 'Main Course', price: 11.99})
CREATE (m_steak:MenuItem {name: 'Classic Ribeye', category: 'Main Course', price: 28.99})

// Map New Menu Items to their deeply nested ingredients
CREATE (m_smoothie)-[:CONTAINS]->(i_peanut_oil)
CREATE (m_smoothie)-[:CONTAINS]->(i_milk_powder)
CREATE (m_smoothie)-[:CONTAINS]->(i_chocolate_glaze)

CREATE (m_vegan_wrap)-[:CONTAINS]->(i_tortilla)
CREATE (m_vegan_wrap)-[:CONTAINS]->(i_tofu)
CREATE (m_vegan_wrap)-[:CONTAINS]->(i_spinach)
CREATE (m_vegan_wrap)-[:CONTAINS]->(i_secret_sauce)

CREATE (m_steak)-[:CONTAINS]->(i_beef_patty)
CREATE (m_steak)-[:CONTAINS]->(i_salt)