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
internal fun Portfolio(state: TradingState, open: (String) -> Unit, explore: () -> Unit) {
    val account = state.account
    val value = marketValue(account, state.quotes)
    val cost = account.holdings.fold(BigDecimal.ZERO) { sum, h -> sum + h.cost }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Header("Your portfolio", "Small moves. A bigger picture.") }
        item { CardBlock(accent = true) { Eyebrow("NET ACCOUNT VALUE"); Text(usd(value + account.cash), fontSize = 36.sp, fontWeight = FontWeight.Bold); Text("${usd(value + account.cash - account.deposits)} total profit / loss", color = if (value + account.cash >= account.deposits) Mint else Red); Text("Includes ${usd(account.cash)} virtual cash", color = Muted) } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Stat("Unrealized P&L", usd(value - cost), Modifier.weight(1f)); Stat("Realized P&L", usd(account.realized), Modifier.weight(1f)) } }
        item { Eyebrow("YOUR HOLDINGS (${account.holdings.size})"); Text("Valued using the displayed sample or fetched prices.", fontSize = 11.sp, color = Muted) }
        if (account.holdings.isEmpty()) item { Empty("A fresh start", "Your first practice trade will appear here."); Button(onClick = explore, modifier = Modifier.fillMaxWidth()) { Text("Explore markets") } }
        items(account.holdings, key = { it.symbol }) { holding ->
            val quote = state.quotes.first { it.symbol == holding.symbol }
            val current = holding.quantity * quote.price
            CardBlock(modifier = Modifier.clickable { open(holding.symbol) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Coin(holding.symbol); Column(Modifier.weight(1f)) { Text(holding.symbol, fontWeight = FontWeight.Bold); Text("${qty(holding.quantity)} coins", color = Muted, fontSize = 12.sp) }; Text(usd(current), fontWeight = FontWeight.Bold) }
                LabelValue("Cost basis", usd(holding.cost)); LabelValue("Unrealized P&L", usd(current - holding.cost), if (current >= holding.cost) Mint else Red)
                Text(source(quote), color = Muted, fontSize = 11.sp)
            }
        }
    }
}

