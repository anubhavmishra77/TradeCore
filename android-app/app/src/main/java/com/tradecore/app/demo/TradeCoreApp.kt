package com.tradecore.app.demo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CancellationException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TradeCoreApp(vm: TradingViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }
    var tab by rememberSaveable { mutableStateOf("Market") }
    var selected by rememberSaveable { mutableStateOf<String?>(null) }
    val colors = darkColorScheme(primary = Mint, onPrimary = Ink, background = Ink, surface = Panel,
        onSurface = Color(0xFFEAF0F8), onBackground = Color(0xFFEAF0F8), secondary = Blue, error = Red)
    LaunchedEffect(state.message) { state.message?.let { snack.showSnackbar(it); vm.dismissMessage() } }
    MaterialTheme(colorScheme = colors) {
        Scaffold(containerColor = Ink, snackbarHost = { SnackbarHost(snack) }, bottomBar = {
            if (state.account.name.isNotEmpty() || state.storageError) {
                NavigationBar(containerColor = Ink, tonalElevation = 0.dp) {
                    listOf("Market" to "◈", "Watchlist" to "☆", "Portfolio" to "◫", "Orders" to "≡", "Wallet" to "$ ").forEach { (name, glyph) ->
                        NavigationBarItem(selected = tab == name, onClick = { tab = name; selected = null },
                            icon = { Text(glyph, fontSize = 22.sp) }, label = { Text(name, fontSize = 10.sp, maxLines = 1) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = Panel, selectedIconColor = Mint, selectedTextColor = Mint))
                    }
                }
            }
        }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                if (state.account.name.isEmpty() && !state.storageError) Welcome(state.saving, vm::start)
                else {
                    BackHandler(selected != null || tab != "Market") { if (selected != null) selected = null else tab = "Market" }
                    val quote = state.quotes.find { it.symbol == selected }
                    if (quote != null) AssetDetail(quote, state, vm, { selected = null })
                    else when (tab) {
                        "Market", "Watchlist" -> Market(state, tab == "Watchlist", vm, { selected = it })
                        "Portfolio" -> Portfolio(state, { selected = it }, { tab = "Market" })
                        "Orders" -> Orders(state.account.orders, { tab = "Market" })
                        "Wallet" -> Wallet(state, vm)
                    }
                }
            }
        }
    }
}

