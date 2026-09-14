package com.example.pos.nav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pos.nav.ui.theme.ui.theme.POSTheme

class CartScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            POSTheme {
                CartScreenContent()
            }
        }
    }
}

@Composable
fun CartScreenContent(modifier: Modifier = Modifier) {
    Text(
        text = "Cart Screen",
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    POSTheme {
        CartScreenContent()
    }
}
