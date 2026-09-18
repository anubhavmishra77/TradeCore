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
internal fun Market(state: TradingState, watchOnly: Boolean, vm: TradingViewModel, open: (String) -> Unit) {
    var search by rememberSaveable { mutableStateOf("") }
    var gainers by rememberSaveable { mutableStateOf(false) }
    val filtered = state.quotes.filter { quote ->
        val asset = assets.first { it.symbol == quote.symbol }
        (!watchOnly || quote.symbol in state.account.watchlist) &&
            (asset.symbol.contains(search, true) || asset.name.contains(search, true)) && (!gainers || quote.change >= 0)
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Header(if (watchOnly) "Your watchlist" else "Markets", if (watchOnly) "Keep your next move in sight" else "Hello, ${state.account.name}. Find your next move.") }
        if (!watchOnly) item {
            val equity = state.account.cash + marketValue(state.account, state.quotes)
            CardBlock(accent = true) {
                Eyebrow("TOTAL PAPER BALANCE")
                Text(usd(equity), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                Text("${usd(equity - state.account.deposits)} all-time return", color = if (equity >= state.account.deposits) Mint else Red)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Available to trade", color = Muted); Text(usd(state.account.cash), fontWeight = FontWeight.Medium) }
            }
        }
        item { Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Eyebrow("CRYPTO / USD"); Text(state.marketStatus, color = Muted, fontSize = 11.sp) }
            TextButton(onClick = vm::refresh, enabled = !state.refreshing) { Text(if (state.refreshing) "Updating…" else "Refresh") }
        }; if (state.refreshing) LinearProgressIndicator(Modifier.fillMaxWidth(), color = Mint) }
        item { OutlinedTextField(value = search, onValueChange = { search = it }, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Search coins or symbols") }, shape = RoundedCornerShape(16.dp)) }
        item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !gainers, onClick = { gainers = false }, label = { Text("All assets") })
            FilterChip(selected = gainers, onClick = { gainers = true }, label = { Text("Gainers") })
        } }
        if (filtered.isEmpty()) item { Empty("Nothing here yet", if (search.isNotEmpty() || gainers) "Try another search or filter." else "Tap the star beside an asset in Markets to follow it.") }
        items(filtered, key = { it.symbol }) { quote -> AssetRow(quote, quote.symbol in state.account.watchlist, !state.saving, { open(quote.symbol) }, { vm.watch(quote.symbol) }) }
        item { Text("PAPER TRADING ONLY  •  No real orders are placed", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(vertical = 12.dp)) }
    }
}

@Composable
internal fun AssetRow(quote: MarketQuote, watched: Boolean, enabled: Boolean, open: () -> Unit, watch: () -> Unit) {
    Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(18.dp)).clickable(onClick = open).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Coin(quote.symbol)
        Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(quote.symbol, fontWeight = FontWeight.Bold); Text(assets.first { it.symbol == quote.symbol }.name, color = Muted, fontSize = 12.sp); Text(source(quote), color = if (quote.isSample || quote.isStale()) Muted else Mint, fontSize = 10.sp) }
        Column(horizontalAlignment = Alignment.End) { Text(price(quote.price), fontWeight = FontWeight.SemiBold); Text(pct(quote.change), color = if (quote.change >= 0) Mint else Red, fontSize = 12.sp) }
        IconButton(onClick = watch, enabled = enabled, modifier = Modifier.semantics { contentDescription = if (watched) "Remove ${quote.symbol} from watchlist" else "Add ${quote.symbol} to watchlist" }) { Text(if (watched) "★" else "☆", color = if (watched) Mint else Muted, fontSize = 24.sp) }
    }
}
internal fun source(quote: MarketQuote) = when { quote.isSample -> "Sample price"; quote.isStale() -> "Stale • refresh to trade"; else -> "Coinbase • ${date(quote.fetchedAt)}" }

@Composable
internal fun AssetDetail(quote: MarketQuote, state: TradingState, vm: TradingViewModel, back: () -> Unit) {
    var side by rememberSaveable(quote.symbol) { mutableStateOf<String?>(null) }
    var chart by remember(quote.symbol) { mutableStateOf<List<Float>>(emptyList()) }
    var chartLoading by remember(quote.symbol) { mutableStateOf(false) }
    var chartRetry by remember(quote.symbol) { mutableIntStateOf(0) }
    LaunchedEffect(quote.symbol, chartRetry) {
        chartLoading = true
        try { chart = vm.candles(quote.symbol) } catch (e: CancellationException) { throw e } catch (_: Exception) { chart = emptyList() }
        finally { chartLoading = false }
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = back) { Text("← Back") }; Text("ASSET OVERVIEW", color = Muted, fontSize = 11.sp); TextButton(onClick = { vm.watch(quote.symbol) }, enabled = !state.saving) { Text(if (quote.symbol in state.account.watchlist) "★ Saved" else "☆ Save") } } }
        item { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) { Coin(quote.symbol); Header(assets.first { it.symbol == quote.symbol }.name, "${quote.symbol} / USD") } }
        item { Text(price(quote.price), fontSize = 40.sp, fontWeight = FontWeight.Bold); Text("${pct(quote.change)}  past 24 hours", color = if (quote.change >= 0) Mint else Red); Text(source(quote), color = Muted, fontSize = 12.sp) }
        item { CardBlock {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Eyebrow("24H PRICE HISTORY"); Text("1H intervals", color = Muted, fontSize = 11.sp) }
            when {
                chartLoading -> Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Mint) }
                chart.isNotEmpty() -> { PriceChart(chart); Text("Coinbase hourly closes • latest ${chart.size} candles", color = Muted, fontSize = 11.sp) }
                else -> { Text("Price history is unavailable. You can still practice with the displayed quote.", color = Muted); TextButton(onClick = { chartRetry++ }) { Text("Retry chart") } }
            }
        } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Stat("24h high", price(quote.high), Modifier.weight(1f)); Stat("24h low", price(quote.low), Modifier.weight(1f)) } }
        item { val holding = state.account.holdings.find { it.symbol == quote.symbol }; CardBlock { Eyebrow("YOUR POSITION"); LabelValue("Holdings", "${holding?.let { qty(it.quantity) } ?: "0"} ${quote.symbol}"); LabelValue("Market value", usd((holding?.quantity ?: BigDecimal.ZERO) * quote.price)); LabelValue("Available cash", usd(state.account.cash)) } }
        item { Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { Button(onClick = { side = "BUY" }, enabled = !state.storageError, modifier = Modifier.weight(1f).height(52.dp)) { Text("Buy ${quote.symbol}") }; OutlinedButton(onClick = { side = "SELL" }, enabled = !state.storageError, modifier = Modifier.weight(1f).height(52.dp)) { Text("Sell ${quote.symbol}") } } }
        item { TextButton(onClick = vm::refresh, enabled = !state.refreshing) { Text(if (state.refreshing) "Refreshing…" else "Refresh quote") }; Text("Simulated market orders fill at the price you review. No fees or slippage are modeled. ${if (quote.isSample) "This quote is sample data." else "Prices are fetched on demand, not streamed."}", color = Muted, fontSize = 12.sp) }
    }
    side?.let { chosen -> TradeDialog(quote, chosen, state, vm) { side = null } }
}

@Composable
internal fun PriceChart(values: List<Float>) {
    Canvas(Modifier.fillMaxWidth().height(160.dp).semantics { contentDescription = "Price chart, first close ${values.first()}, latest close ${values.last()} USD" }) {
        val min = values.min(); val max = values.max(); val range = (max - min).coerceAtLeast(.00001f)
        repeat(4) { line -> val y = size.height * line / 3; drawLine(Muted.copy(alpha = .12f), Offset(0f, y), Offset(size.width, y)) }
        val path = Path()
        values.forEachIndexed { index, value -> val x = size.width * index / (values.size - 1); val y = 8f + (size.height - 16f) * (1 - (value - min) / range); if (index == 0) path.moveTo(x, y) else path.lineTo(x, y) }
        drawPath(path, Mint, style = Stroke(width = 3.dp.toPx()))
    }
}

