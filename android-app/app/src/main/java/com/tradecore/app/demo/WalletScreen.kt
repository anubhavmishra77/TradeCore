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
internal fun Wallet(state: TradingState, vm: TradingViewModel) {
    var add by rememberSaveable { mutableStateOf(false) }
    var reset by rememberSaveable { mutableStateOf(false) }
    var amount by rememberSaveable { mutableStateOf("1000") }
    var error by remember { mutableStateOf<String?>(null) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        item { Header("Your wallet", "Virtual funds. Real practice.") }
        if (state.storageError) item { Text("Your saved account could not be loaded. Reset below to create a new practice account.", color = Red) }
        item { CardBlock(accent = true) { Eyebrow("AVAILABLE VIRTUAL CASH"); Text(usd(state.account.cash), fontSize = 36.sp, fontWeight = FontWeight.Bold); Text("USD • Paper account", color = Mint); Button(onClick = { add = true; error = null }, enabled = !state.saving && !state.storageError, modifier = Modifier.fillMaxWidth()) { Text("+ Add demo funds") } } }
        item { CardBlock { LabelValue("Trader", state.account.name.ifEmpty { "Demo trader" }); LabelValue("Total demo funds", usd(state.account.deposits)); Text("This account lives on this device. Clearing app data removes it. No real deposits, withdrawals, or exchange account are connected.", color = Muted, fontSize = 12.sp) } }
        item { Eyebrow("RECENT ACTIVITY") }
        items(state.account.ledger.take(30), key = { it.id }) { entry -> Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(entry.label, fontWeight = FontWeight.Medium); Text(date(entry.timestamp), color = Muted, fontSize = 11.sp) }; Text((if (entry.amount.signum() > 0) "+" else "") + usd(entry.amount), color = if (entry.amount.signum() > 0) Mint else MaterialTheme.colorScheme.onSurface) } }
        item { HorizontalDivider(color = Muted.copy(alpha = .2f)); TextButton(onClick = { reset = true }, enabled = !state.saving) { Text("Reset practice account", color = Red) }; Text("TradeCore demo v0.1 • Market data: Coinbase Exchange\nMarket orders only • No fees or slippage", color = Muted, fontSize = 11.sp) }
    }
    if (add) AlertDialog(onDismissRequest = { if (!state.saving) add = false }, title = { Text("Add demo funds") }, text = { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { Text("Add virtual USD to your practice wallet."); OutlinedTextField(value = amount, onValueChange = { if (it.length <= 12 && it.all { c -> c.isDigit() || c == '.' }) amount = it; error = null }, label = { Text("Amount in USD") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)); (error ?: state.message)?.let { Text(it, color = Red) } } }, confirmButton = { TextButton(enabled = !state.saving, onClick = { try { PaperTrading.deposit(state.account, amount); vm.deposit(amount) { add = false } } catch (e: Exception) { error = e.message } }) { Text(if (state.saving) "Saving…" else "Add funds") } }, dismissButton = { TextButton(onClick = { add = false }, enabled = !state.saving) { Text("Cancel") } })
    if (reset) AlertDialog(onDismissRequest = { if (!state.saving) reset = false }, title = { Text("Start fresh?") }, text = { Text("This removes your holdings, orders, and activity and restores $10,000 in virtual cash. It cannot be undone.") }, confirmButton = { TextButton(onClick = { vm.reset { reset = false } }, enabled = !state.saving) { Text("Reset account", color = Red) } }, dismissButton = { TextButton(onClick = { reset = false }, enabled = !state.saving) { Text("Keep account") } })
}

