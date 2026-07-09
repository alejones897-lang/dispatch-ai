package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DispatchViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BlueSecondary
import com.example.ui.theme.SuccessGreen

@Composable
fun ReportsScreen(
    viewModel: DispatchViewModel,
    modifier: Modifier = Modifier
) {
    val loads by viewModel.loads.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val trucks by viewModel.trucks.collectAsState()

    val scrollState = rememberScrollState()

    // Analytics calculations
    val totalRevenue = loads.sumOf { it.rateConfirmationAmount }
    val deliveredRevenue = loads.filter { it.status == "Delivered" }.sumOf { it.rateConfirmationAmount }
    val activeRevenue = loads.filter { it.status == "Assigned" || it.status == "Dispatched" }.sumOf { it.rateConfirmationAmount }

    val deliveredCount = loads.count { it.status == "Delivered" }
    val activeCount = loads.count { it.status == "Assigned" || it.status == "Dispatched" }

    val activeDriversCount = drivers.count { it.status == "On Trip" }
    val availableDriversCount = drivers.count { it.status == "Available" }

    val inShopTrucksCount = trucks.count { it.maintenanceStatus == "In Shop" }
    val activeTrucksCount = trucks.count { it.maintenanceStatus == "Active" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fleet Insights & Reports",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Real-time revenue metrics, staff efficiency, and trailer usage",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = "Analytics",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Revenue Metrics Row (Cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reports_weekly_revenue_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = borderOutline()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Gross Revenue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = "$${String.format("%,.0f", totalRevenue)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Seeded + Logged", fontSize = 9.sp, color = SuccessGreen, fontWeight = FontWeight.Medium)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reports_delivered_revenue_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = borderOutline()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Delivered Revenue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = "$${String.format("%,.0f", deliveredRevenue)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$deliveredCount shipments finalized", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reports_active_revenue_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = borderOutline()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Freight", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = "$${String.format("%,.0f", activeRevenue)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$activeCount transit assignments", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Revenue Trend Chart (Custom Canvas drawing)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_revenue_chart_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = borderOutline()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Daily Revenue Stream (USD)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Last 5 days of logistics invoicing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw Bar Chart on Canvas
                    RevenueBarChart()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fleet KPI Breakdown Row (Pie/Utilizations)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Driver Utilization Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = borderOutline()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Driver Utilization", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            val ratio = if (drivers.isNotEmpty()) activeDriversCount.toFloat() / drivers.size else 0f
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(Color.Gray.copy(alpha = 0.15f), style = Stroke(width = 16f))
                                drawArc(
                                    color = BlueSecondary,
                                    startAngle = -90f,
                                    sweepAngle = ratio * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 16f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(ratio * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("In Transit", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        Text("$activeDriversCount on road | $availableDriversCount waiting", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }

                // Truck Maintenance Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = borderOutline()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Truck Status Ratio", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))

                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            val activeRatio = if (trucks.isNotEmpty()) activeTrucksCount.toFloat() / trucks.size else 0f
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(Color.Gray.copy(alpha = 0.15f), style = Stroke(width = 16f))
                                drawArc(
                                    color = SuccessGreen,
                                    startAngle = -90f,
                                    sweepAngle = activeRatio * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 16f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(activeRatio * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Active", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            }
                        }

                        Text("$activeTrucksCount active fleet | $inShopTrucksCount in shop", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Driver Performance Leaderboard
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reports_driver_leaderboard_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = borderOutline()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Driver Performance KPIs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Icon(Icons.Default.Star, contentDescription = "Star", tint = AmberAccent, modifier = Modifier.size(18.dp))
                    }
                    Text("Safety and on-time routing ratings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))

                    Spacer(modifier = Modifier.height(12.dp))

                    drivers.forEachIndexed { idx, driver ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("#${idx + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.width(24.dp))
                                Column {
                                    Text(driver.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text("CDL ID: ${driver.cdlNumber}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                            }

                            // Simulated score
                            val score = when (driver.name) {
                                "Marcus Vance" -> 98
                                "Elena Rostova" -> 95
                                "Tyrone Miller" -> 90
                                "Carlos Santana" -> 88
                                else -> 94
                            }
                            val scoreColor = if (score >= 95) SuccessGreen else AmberAccent

                            Column(horizontalAlignment = Alignment.End) {
                                Text("$score% Score", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = scoreColor)
                                Text("On-time status", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            }
                        }
                        if (idx < drivers.size - 1) {
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueBarChart() {
    val barColor = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        val width = size.width
        val height = size.height

        // Y-axis grid lines (3 grids)
        val gridY = listOf(0.1f, 0.45f, 0.8f)
        gridY.forEach { fraction ->
            drawLine(
                color = gridColor,
                start = Offset(0f, height * fraction),
                end = Offset(width, height * fraction),
                strokeWidth = 1f
            )
        }

        // Daily revenue values: Friday: $2,800, Saturday: $0, Sunday: $0, Monday: $4,200, Tuesday: $2,450
        val data = listOf(2800f, 0f, 0f, 4200f, 2450f)
        val days = listOf("Fri", "Sat", "Sun", "Mon", "Tue")
        val maxVal = 5000f

        val paddingX = width * 0.1f
        val chartWidth = width - (paddingX * 2f)
        val colWidth = chartWidth / data.size
        val barWidth = colWidth * 0.6f

        data.forEachIndexed { index, valRaw ->
            val fraction = valRaw / maxVal
            val colCenterX = paddingX + (index * colWidth) + (colWidth / 2f)
            val barHeight = height * 0.8f * fraction

            // Draw Bar
            if (valRaw > 0) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(colCenterX - (barWidth / 2f), (height * 0.8f) - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            // Draw label on ground
            // (For actual text rendering on Canvas, we usually use native canvas or drawContext. Since we are drawing simple lines/boxes, a line marker is highly compatible)
            drawLine(
                color = labelColor,
                start = Offset(colCenterX, height * 0.82f),
                end = Offset(colCenterX, height * 0.86f),
                strokeWidth = 2f
            )
        }

        // Ground Axis line
        drawLine(
            color = labelColor,
            start = Offset(0f, height * 0.8f),
            end = Offset(width, height * 0.8f),
            strokeWidth = 2f
        )
    }

    // Label descriptions below the chart
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val days = listOf("Fri ($2.8k)", "Sat ($0)", "Sun ($0)", "Mon ($4.2k)", "Tue ($2.5k)")
        days.forEach { day ->
            Text(day, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun borderOutline() = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
