package com.example.pos.nav

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.pos.R
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Product : Screen("product", "Orders", Icons.Default.List)
    object Cart : Screen("cart", "Analysis", Icons.Default.BarChart)
    object TakeOrder : Screen("take_order", "Take Order", Icons.Default.Add)

}

val bottomNavItems = listOf(Screen.Home, Screen.Product, Screen.Cart)