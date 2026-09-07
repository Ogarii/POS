package com.example.pos.nav

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            bottomNavItems.forEach { screen ->
                item(
                    icon = {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = screen.icon,
                            contentDescription = screen.label
                        )
                    },
                    label = { Text(screen.label) },
                    selected = screen.route == currentRoute,
                    onClick = { currentRoute = screen.route }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
}

@Preview(showBackground = true)
@Composable
fun NavHostPreview() {
    POSTheme {
        POSApp()
    }
}