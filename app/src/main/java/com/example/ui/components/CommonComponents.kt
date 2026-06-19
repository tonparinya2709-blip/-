package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlucoseRecord
import com.example.ui.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun BloodGlucoseChart(
    records: List<GlucoseRecord>,
    modifier: Modifier = Modifier,
    targetMin: Int = 80,
    targetMax: Int = 130
) {
    val textMeasurer = rememberTextMeasurer()
    
    // Sort and limit to latest 7 records (or fill with dummy points if less than 2, but we have database seed which has exactly 7!)
    val sortedRecords = records.sortedBy { it.timestamp }.takeLast(7)
    
    val containerBg = Color(0xFF201A19) // High-contrast geometric base warm black
    val titleColor = Color.White
    val accentColor = Color(0xFFD0BCFF) // Lavendar/Violet accent from design html
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerBg
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Text(
                text = "กราฟระดับน้ำตาลย้อนหลัง (mg/dL)",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = titleColor
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (sortedRecords.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "กรุณากรอกข้อมูลน้ำตาลเพื่อแสดงกราฟวิเคราะห์แนวโน้ม",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            } else {
                val minVal = (sortedRecords.minOf { it.valMgDl } - 20).coerceAtLeast(30)
                val maxVal = (sortedRecords.maxOf { it.valMgDl } + 30).coerceAtMost(400)
                val valueRange = (maxVal - minVal).toFloat()

                androidx.compose.foundation.Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    
                    val paddingLeft = 50f
                    val paddingRight = 50f
                    val paddingTop = 40f
                    val paddingBottom = 40f
                    
                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    // Draw Target Range Ribbon (Lavender copy)
                    val targetYMax = paddingTop + chartHeight * (1f - (targetMax - minVal) / valueRange)
                    val targetYMin = paddingTop + chartHeight * (1f - (targetMin - minVal) / valueRange)
                    
                    drawRect(
                        color = accentColor.copy(alpha = 0.12f), // Soft neon purple ribbon
                        topLeft = Offset(paddingLeft, targetYMax),
                        size = androidx.compose.ui.geometry.Size(chartWidth, targetYMin - targetYMax)
                    )

                    // Draw Threshold Lines
                    drawLine(
                        color = accentColor.copy(alpha = 0.3f),
                        start = Offset(paddingLeft, targetYMin),
                        end = Offset(width - paddingRight, targetYMin),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.3f),
                        start = Offset(paddingLeft, targetYMax),
                        end = Offset(width - paddingRight, targetYMax),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw subtle dark gridlines
                    val gridSteps = 3
                    for (i in 0..gridSteps) {
                        val gridY = paddingTop + (chartHeight / gridSteps) * i
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(paddingLeft, gridY),
                            end = Offset(width - paddingRight, gridY),
                            strokeWidth = 1f
                        )
                    }

                    // Calculate point locations
                    val pointCount = sortedRecords.size
                    val points = sortedRecords.mapIndexed { i, record ->
                        val x = paddingLeft + (chartWidth / (pointCount - 1)) * i
                        val y = paddingTop + chartHeight * (1f - (record.valMgDl - minVal) / valueRange)
                        Offset(x, y)
                    }

                    // Draw Gradient path under line
                    val gradPath = Path().apply {
                        moveTo(points.first().x, height - paddingBottom)
                        for (p in points) {
                            lineTo(p.x, p.y)
                        }
                        lineTo(points.last().x, height - paddingBottom)
                        close()
                    }
                    drawPath(
                        path = gradPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                accentColor.copy(alpha = 0.3f),
                                accentColor.copy(alpha = 0.005f)
                            ),
                            startY = paddingTop,
                            endY = height - paddingBottom
                        )
                    )

                    // Draw curve line
                    val strokePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 1 until points.size) {
                            lineTo(points[i].x, points[i].y)
                        }
                    }
                    drawPath(
                        path = strokePath,
                        color = accentColor,
                        style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw Circles and labels
                    for (i in points.indices) {
                        val record = sortedRecords[i]
                        val pt = points[i]
                        
                        // Draw white outer shadow, then primary inner dot
                        drawCircle(color = Color.White, radius = 9f, center = pt)
                        drawCircle(color = accentColor, radius = 6f, center = pt)

                        // Draw value label above point
                        val textValue = record.valMgDl.toString()
                        val textLayout = textMeasurer.measure(
                            text = textValue,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(pt.x - textLayout.size.width / 2, pt.y - 45f)
                        )

                        // Draw X-axis date label below
                        val rawDate = record.date
                        val dayNum = rawDate.split("-").lastOrNull() ?: ""
                        
                        val axisLayout = textMeasurer.measure(
                            text = dayNum,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        )
                        drawText(
                            textLayoutResult = axisLayout,
                            topLeft = Offset(pt.x - axisLayout.size.width / 2, height - paddingBottom + 10f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ComplianceDonutChart(
    percentage: Int,
    onTimeCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "สถิติรับประทานยาตรงเวลา",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                )
                Text(
                    text = "อ้างอิงจากการสแกนทานยาล่าสุด",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ตรงเวลา: $onTimeCount ครั้ง",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(MaterialTheme.colorScheme.error, RoundedCornerShape(5.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ข้ามยาล่าช้า: ${totalCount - onTimeCount} ครั้ง",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 12.dp.toPx()
                    // Draw grey background ring
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        style = Stroke(width = strokeWidth)
                    )
                    // Draw progress arc
                    val sweepAngle = (percentage / 100f) * 360f
                    drawArc(
                        color = Color(0xFF6750A4), // Primary Purple Theme Accent
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$percentage%",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6750A4)
                        )
                    )
                    Text(
                        text = "เสร็จสิ้น",
                        style = TextStyle(
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
        }
    }
}
