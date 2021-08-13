package com.abanobnageh.recipeapp.data.models.domain

enum class FoodCategory(val foodCategory: String) {
    CHICKEN("Chicken"),
    BEEF("Beef"),
    SOUP("Soup"),
    DESSERT("Dessert"),
    VEGETARIAN("Vegetarian"),
    MILK("Milk"),
    VEGAN("Vegan"),
    PIZZA("Pizza"),
    DONUT("Donut");

    companion object {
        fun getAllFoodCategories(): List<FoodCategory> {
            return values().toList()
        }

        fun isFoodCategory(foodCategory: String): Boolean {
            return values().associateBy(FoodCategory::foodCategory).contains(foodCategory)
        }

        fun getFoodCategory(foodCategory: String): FoodCategory? {
            if (isFoodCategory(foodCategory)) {
                return values().associateBy(FoodCategory::foodCategory)[foodCategory]
            }

            return null
        }
    }
}