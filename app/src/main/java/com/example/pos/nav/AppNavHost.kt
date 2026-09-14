package com.example.pos.nav

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pos.nav.ui.theme.POSTheme
import com.example.pos.core.models.Order
import com.example.pos.core.money.Money
import com.example.pos.orders.OrderStore
import com.example.pos.orders.TakeOrderScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppNavHost : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            POSTheme {
                POSApp()
            }
        }
    }
}
@Composable
fun POSApp() {
    var currentRoute by rememberSaveable { mutableStateOf(Screen.Home.route) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute != Screen.TakeOrder.route) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        tonalElevation = 8.dp,
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            bottomNavItems.forEach { screen ->
                                val selected = screen.route == currentRoute
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentRoute = screen.route },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(screen.label) },
                                    alwaysShowLabel = false
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = if (currentRoute == Screen.TakeOrder.route) 0.dp else innerPadding.calculateBottomPadding())
        ) {
            when (currentRoute) {
                Screen.Home.route -> HomeScreenContent(
                    onCreateOrderClick = { currentRoute = Screen.TakeOrder.route }
                )
                Screen.Product.route -> ProductScreenContent()
                Screen.Cart.route -> CartScreenContent()
                Screen.TakeOrder.route -> TakeOrderScreen(
                    categories = MenuCatalog.categories,
                    menuItems = MenuCatalog.menuItems,
                    onBack = { currentRoute = Screen.Home.route },
                    onSubmitOrder = { cartLines ->
                        val order = Order(
                            id = "ORD-${(1000..9999).random()}",
                            lines = cartLines,
                            total = Order.calculateTotal(cartLines),
                            timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(
                                Date()
                            )
                        )
                        OrderStore.addOrder(order)
                        currentRoute = Screen.Product.route
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NavHostPreview() {
    POSTheme {
        POSApp()
    }
}