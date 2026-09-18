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
internal fun Orders(orders: List<PaperOrder>, explore: () -> Unit) {
    var filter by rememberSaveable { mutableStateOf("ALL") }
    val filtered = orders.filter { filter == "ALL" || it.side == filter }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Header("Order history", "Every move, all in one place.") }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("ALL", "BUY", "SELL").forEach { value -> FilterChip(selected = filter == value, onClick = { filter = value }, label = { Text(value.lowercase().replaceFirstChar { it.uppercase() }) }) } } }
        if (filtered.isEmpty()) item { Empty("No orders yet", "Filled paper orders will appear here."); TextButton(onClick = explore) { Text("Find your first trade →") } }
        items(filtered, key = { it.id }) { order -> CardBlock {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("${order.side} ${order.symbol}", color = if (order.side == "BUY") Mint else Blue, fontWeight = FontWeight.Bold); Text("FILLED", color = Mint, fontSize = 11.sp) }
            LabelValue("Quantity", qty(order.quantity)); LabelValue("Fill price", price(order.price)); LabelValue("Total", usd(order.total))
            Text("${date(order.timestamp)} • ${if (order.sample) "Sample price" else "Coinbase quote"}", color = Muted, fontSize = 11.sp)
            Text("Paper order · ${order.id.take(8)}", color = Muted, fontSize = 10.sp)
        } }
    }
}

