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
internal fun TradeDialog(quote: MarketQuote, side: String, state: TradingState, vm: TradingViewModel, close: () -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    var reviewed by remember { mutableStateOf<MarketQuote?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var completed by remember { mutableStateOf(false) }
    val selected = reviewed ?: quote
    val total = input.toBigDecimalOrNull()?.multiply(selected.price)?.money()
    AlertDialog(onDismissRequest = { if (!state.saving) close() }, title = { Text(if (completed) "Trade complete" else if (reviewed != null) "Review paper order" else "${if (side == "BUY") "Buy" else "Sell"} ${quote.symbol}") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (completed) { Text("Your simulated $side order is filled. Your holdings, wallet, and order history have been updated.") }
            else {
                Text("PAPER TRADE • MARKET ORDER", color = Mint, fontSize = 11.sp)
                if (reviewed == null) OutlinedTextField(value = input, onValueChange = { if (it.length <= 20 && it.all { c -> c.isDigit() || c == '.' }) input = it; error = null }, label = { Text("Quantity (${quote.symbol})") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                else LabelValue("Quantity", "$input ${quote.symbol}")
                LabelValue("Price", price(selected.price)); LabelValue("Order total", total?.let(::usd) ?: "—")
                LabelValue("Available cash", usd(state.account.cash))
                Text(source(selected), color = Muted, fontSize = 12.sp)
                Text("Virtual funds only. Fills use this reviewed price.", color = Muted, fontSize = 12.sp)
                (error ?: state.message)?.let { Text(it, color = Red) }
            }
        }
    }, confirmButton = {
        TextButton(enabled = !state.saving, onClick = {
            if (completed) close()
            else if (reviewed == null) {
                try { PaperTrading.trade(state.account, quote, side, input); reviewed = quote; error = null }
                catch (e: Exception) { error = e.message }
            } else {
                try { PaperTrading.trade(state.account, selected, side, input); vm.trade(selected, side, input) { completed = true } }
                catch (e: Exception) { error = e.message; reviewed = null }
            }
        }) { Text(if (completed) "Done" else if (state.saving) "Saving…" else if (reviewed != null) "Confirm $side" else "Review order") }
    }, dismissButton = { if (!completed) TextButton(onClick = { if (reviewed != null) reviewed = null else close() }, enabled = !state.saving) { Text(if (reviewed != null) "Edit" else "Cancel") } })
}

