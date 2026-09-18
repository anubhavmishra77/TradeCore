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
internal fun Welcome(saving: Boolean, start: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        item { Spacer(Modifier.height(32.dp)); Text("◈  TRADECORE", color = Mint, fontWeight = FontWeight.Bold, letterSpacing = 3.sp) }
        item { Text("Your next move.\nStarts here.", fontSize = 42.sp, lineHeight = 49.sp, fontWeight = FontWeight.Bold) }
        item { Text("Build your confidence in the market with a portfolio that's yours to explore.", color = Muted, fontSize = 17.sp, lineHeight = 25.sp) }
        item { CardBlock { Eyebrow("YOUR PRACTICE ACCOUNT"); Text("$10,000.00", fontSize = 34.sp, fontWeight = FontWeight.Bold); Text("Virtual USD • No deposits or real trades", color = Mint); HorizontalDivider(color = Muted.copy(alpha = .2f)); Text("Explore crypto prices, make practice trades, and follow your performance. Everything is saved on this device.", color = Muted) } }
        item { OutlinedTextField(value = name, onValueChange = { name = it.take(30) }, label = { Text("What should we call you?") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        item { Button(onClick = { start(name) }, enabled = name.trim().length >= 2 && !saving, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(if (saving) "Creating your account…" else "Start paper trading  →", fontWeight = FontWeight.Bold) } }
        item { Text("Demo account • No password needed\nPrices from Coinbase when available; labeled sample prices work offline. This is a simulator, not a brokerage account.", color = Muted, fontSize = 12.sp, lineHeight = 18.sp) }
    }
}

