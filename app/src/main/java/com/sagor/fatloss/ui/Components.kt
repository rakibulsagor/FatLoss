package com.sagor.fatloss.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@Composable
fun TopBar(title: String = "Fat Loss", badge: String? = null, onProfileClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    val photoPath = context.getSharedPreferences("fat_loss_prefs", android.content.Context.MODE_PRIVATE)
        .getString("profile_photo_path", null)
    val bitmap = remember(photoPath) {
        photoPath?.let { path ->
            runCatching { BitmapFactory.decodeFile(path) }.getOrNull()
        }
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .background(Surface2, RoundedCornerShape(50))
                .border(1.dp, Green.copy(alpha = .55f), RoundedCornerShape(50))
                .then(if (onProfileClick != null) Modifier.clickable { onProfileClick() } else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Profile",
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(50))
                )
            } else {
                Text("FL", color = Green, fontWeight = FontWeight.Black, fontSize = 11.sp)
            }
        }
        Text(
            title,
            color = if (title == "FATLOSS") Green else TextColor,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 12.dp).weight(1f)
        )
        if (badge != null) {
            Box(
                Modifier
                    .background(Green.copy(alpha = .12f), RoundedCornerShape(18.dp))
                    .border(1.dp, Green.copy(alpha = .55f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(badge, color = Green, fontWeight = FontWeight.Black)
            }
        } else {
            Icon(Icons.Default.OfflineBolt, contentDescription = "Offline", tint = TextColor)
        }
    }
}

@Composable
fun Section(title: String) {
    Text(
        title.uppercase(),
        color = TextColor,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(top = 24.dp, bottom = 10.dp)
    )
}

@Composable
fun PlanCard(title: String, body: String, accent: Color = Green, content: @Composable (() -> Unit)? = null) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, color = accent, style = MaterialTheme.typography.titleMedium)
            if (body.isNotBlank()) Text(body, color = TextColor, fontSize = 14.sp, modifier = Modifier.padding(top = 5.dp))
            content?.invoke()
        }
    }
}

@Composable
fun StatCard(label: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label.uppercase(), color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(value, color = Green, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(sub, color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
fun CheckRow(text: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onChecked)
        Text(text, color = TextColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProgressLine(label: String, value: Float, caption: String) {
    val animated by animateFloatAsState(
        targetValue = value.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 850),
        label = "progress-$label"
    )
    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = TextColor, fontWeight = FontWeight.Bold)
            Text(caption, color = Muted)
        }
        LinearProgressIndicator(
            progress = { animated },
            modifier = Modifier.fillMaxWidth().height(12.dp).padding(top = 5.dp),
            color = Green,
            trackColor = Surface3
        )
    }
}

@Composable
fun AppTextField(value: String, label: String, onChange: (String) -> Unit, number: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Decimal else KeyboardType.Text),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}

@Composable
fun TinyChart(values: List<Double>, target: Double = 65.0) {
    val chartColor = Green
    val targetColor = Amber
    Canvas(
        modifier = Modifier.fillMaxWidth().height(120.dp).background(Surface2, RoundedCornerShape(8.dp)).padding(8.dp)
    ) {
        if (values.isEmpty()) return@Canvas
        val max = (values.maxOrNull() ?: 80.0).coerceAtLeast(80.0)
        val min = target.coerceAtMost(values.minOrNull() ?: target)
        fun y(v: Double) = size.height - (((v - min) / (max - min)).toFloat() * size.height)
        val step = if (values.size <= 1) size.width else size.width / (values.size - 1)
        drawLine(targetColor, Offset(0f, y(target)), Offset(size.width, y(target)), strokeWidth = 2.dp.toPx())
        values.forEachIndexed { i, v ->
            if (i > 0) drawLine(
                chartColor,
                Offset((i - 1) * step, y(values[i - 1])),
                Offset(i * step, y(v)),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun PrimaryAction(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Green, contentColor = Bg),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(vertical = 4.dp)
    ) {
        Text(text.uppercase(), fontWeight = FontWeight.Black, fontSize = 17.sp)
    }
}

@Composable
fun MetricRing(
    label: String,
    value: String,
    target: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900),
        label = "ring-$label"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(72.dp)) {
                    drawCircle(Surface3, style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(
                        color = color,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(label.take(1).uppercase(), color = color, fontWeight = FontWeight.Black)
            }
            Text(value, color = TextColor, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text(target, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HeroBanner(title: String, subtitle: String, modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "hero-pulse")
    val scale by pulse.animateFloat(
        initialValue = .985f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "hero-scale"
    )
    val glow by pulse.animateFloat(
        initialValue = .18f,
        targetValue = .34f,
        animationSpec = infiniteRepeatable(tween(1100), RepeatMode.Reverse),
        label = "hero-glow"
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = Green),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(18.dp, RoundedCornerShape(12.dp), ambientColor = Green.copy(alpha = glow))
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("!", color = Bg.copy(alpha = .65f), fontSize = 34.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(18.dp))
            Column {
                Text(title.uppercase(), color = Bg, style = MaterialTheme.typography.headlineMedium)
                Text(subtitle.uppercase(), color = Bg.copy(alpha = .65f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AccentChip(text: String, color: Color = Green) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = .13f), RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
