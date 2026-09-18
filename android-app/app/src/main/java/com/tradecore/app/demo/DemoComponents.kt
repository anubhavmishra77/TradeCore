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

internal val Ink = Color(0xFF080F1C)
internal val Panel = Color(0xFF131F30)
internal val Mint = Color(0xFF80E5C3)
internal val Muted = Color(0xFF9AAABD)
internal val Red = Color(0xFFFF8C9B)
internal val Blue = Color(0xFF8AB6FF)
internal fun usd(value: BigDecimal): String = NumberFormat.getCurrencyInstance(Locale.US).format(value)
internal fun qty(value: BigDecimal): String = value.stripTrailingZeros().toPlainString()
internal fun price(value: BigDecimal): String = if (value < BigDecimal.ONE) "$${value.setScale(4, RoundingMode.HALF_UP)}" else usd(value)
internal fun pct(value: Double) = String.format(Locale.US, "%+.2f%%", value)
internal fun date(value: Long) = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(value))
internal fun marketValue(account: PaperAccount, quotes: List<MarketQuote>) = account.holdings.fold(BigDecimal.ZERO) { sum, holding -> sum + holding.quantity * quotes.first { it.symbol == holding.symbol }.price }

@Composable internal fun Header(title: String, subtitle: String) { Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text(subtitle, fontSize = 13.sp, color = Muted) } }
@Composable internal fun Eyebrow(text: String) { Text(text, color = Muted, fontSize = 10.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold) }
@Composable internal fun CardBlock(modifier: Modifier = Modifier, accent: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().background(if (accent) Brush.linearGradient(listOf(Color(0xFF163C3C), Panel)) else Brush.linearGradient(listOf(Panel, Panel)), RoundedCornerShape(22.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
}
@Composable internal fun Coin(symbol: String) { Box(Modifier.size(44.dp).background(when (symbol) { "BTC" -> Color(0xFF4A3620); "ETH" -> Color(0xFF303656); else -> Color(0xFF203F43) }, CircleShape), contentAlignment = Alignment.Center) { Text(symbol.take(1), color = when (symbol) { "BTC" -> Color(0xFFFFBC67); "ETH" -> Blue; else -> Mint }, fontSize = 22.sp, fontWeight = FontWeight.Bold) } }
@Composable internal fun LabelValue(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Muted, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text(value, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.End) } }
@Composable internal fun Stat(label: String, value: String, modifier: Modifier) { Column(modifier.background(Panel, RoundedCornerShape(18.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(label, color = Muted, fontSize = 12.sp); Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp) } }
@Composable internal fun Empty(title: String, text: String) { CardBlock { Text("◈", color = Mint, fontSize = 32.sp); Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold); Text(text, color = Muted) } }
