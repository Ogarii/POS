package com.example.pos.nav

import com.example.pos.core.money.Money
import com.example.pos.core.models.MenuItem

object MenuCatalog {
    val categories = listOf(
        Category("burgers", "Burgers"),
        Category("sides", "Sides"),
        Category("drinks", "Drinks")
    )


    val menuItems = listOf(
        MenuItem("brg-chicken", "Chicken Burger", "Classic chicken burger", "burgers", Money.parseDecimalString("450.00")),
        MenuItem("brg-beef", "Double Beef Burger", "Two juicy beef patties", "burgers", Money.parseDecimalString("620.00")),
        MenuItem("sid-fries", "Fries", "Crispy golden fries", "sides", Money.parseDecimalString("200.00")),
        MenuItem("sid-onion-rings", "Onion Rings", "Deep fried onion rings", "sides", Money.parseDecimalString("280.00")),
        MenuItem("drk-mango", "Fresh Mango Juice", "Large (No Ice)", "drinks", Money.parseDecimalString("180.00")),
        MenuItem("drk-soda", "Soda 300ml", "Cold soda", "drinks", Money.parseDecimalString("80.00"))
    )
}
