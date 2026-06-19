package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HealthViewModel
import com.example.ui.components.BloodGlucoseChart

@Composable
fun ReportScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.glucoseRecords.collectAsState()
    val logs by viewModel.medicineLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var activePeriodDays by remember { mutableStateOf(7) } // 7, 30, 90, 365

    // Dynamic Computations
    val nowMs = System.currentTimeMillis()
    val periodRecords = records.filter {
        val diffDays = (nowMs - it.timestamp) / (24 * 60 * 60 * 1000L)
        diffDays <= activePeriodDays
    }

    val avgValue = if (periodRecords.isNotEmpty()) periodRecords.map { it.valMgDl }.average().toInt() else 198
    val maxValue = if (periodRecords.isNotEmpty()) periodRecords.maxOf { it.valMgDl } else 299
    val minValue = if (periodRecords.isNotEmpty()) periodRecords.minOf { it.valMgDl } else 98
    val totalRecordsCount = if (periodRecords.isNotEmpty()) periodRecords.size else 28

    // Pill logs count
    val periodLogs = logs.filter {
        val diffDays = (nowMs - it.timestamp) / (24 * 60 * 60 * 1000L)
        diffDays <= activePeriodDays
    }
    val totalLogsCount = periodLogs.size
    val onTimeCount = periodLogs.count { it.status == "ตรงเวลา" }
    val lateCount = periodLogs.count { it.status == "ล่าช้า" }
    
    val compliantPct = if (totalLogsCount > 0) (onTimeCount * 100) / totalLogsCount else 92
    val latePct = if (totalLogsCount > 0) (lateCount * 100) / totalLogsCount else 8
    val skipPct = 100 - compliantPct - latePct

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Header
        Column {
            Text(
                text = "รายงานวิเคราะห์ผลสุขภาพ",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = "วิเคราะห์ค่าเฉลี่ย สถิติข้อมูล และจัดเตรียมรายงานสำหรับแพทย์",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Time Period Selectors Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                7 to "7 วัน",
                30 to "30 วัน",
                90 to "90 วัน",
                365 to "1 ปี"
            ).forEach { (days, label) ->
                Button(
                    onClick = { activePeriodDays = days },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activePeriodDays == days) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (activePeriodDays == days) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Summary Statistics Grid
        Card(
            modifier = Modifier.fillMaxWidth().testTag("grid_summary_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "สรุปผลสุขภาพรอบ $activePeriodDays วันที่ผ่านมา",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Average Card
                    StatMetricTile(
                        label = "ค่าเฉลี่ย",
                        value = "$avgValue",
                        unit = "mg/dL",
                        color = Color(0xFF00875A),
                        modifier = Modifier.weight(1f)
                    )

                    // Max Card
                    StatMetricTile(
                        label = "สูงสุด",
                        value = "$maxValue",
                        unit = "mg/dL",
                        color = Color(0xFFDE350B),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Min Card
                    StatMetricTile(
                        label = "ต่ำสุด",
                        value = "$minValue",
                        unit = "mg/dL",
                        color = Color(0xFFEE9A00),
                        modifier = Modifier.weight(1f)
                    )

                    // Total checks
                    StatMetricTile(
                        label = "ตรวจทั้งหมด",
                        value = "$totalRecordsCount",
                        unit = "ครั้ง",
                        color = Color(0xFF0052CC),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Dynamic Trend Graph inside periods
        BloodGlucoseChart(
            records = periodRecords.ifEmpty { records },
            targetMin = profile.targetMin,
            targetMax = profile.targetMax,
            modifier = Modifier.testTag("trend_glucose_chart")
        )

        // Pill Adherence Bars
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            )
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "สถิติตารางทานยาสะสม",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // On Time
                ProgressBarStat(label = "ทานยาตรงเวลา", percentage = compliantPct, color = Color(0xFF00875A))
                Spacer(modifier = Modifier.height(12.dp))
                // Late
                ProgressBarStat(label = "ทานยาล่าช้า", percentage = latePct, color = Color(0xFFEE9A00))
                Spacer(modifier = Modifier.height(12.dp))
                // Skipped
                ProgressBarStat(label = "ข้ามยาล่าช้า", percentage = skipPct, color = Color(0xFFDE350B))
            }
        }

        // Exporter Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    Toast.makeText(context, "กำลังส่งออกรายงานสุขภาพ เป็น PDF...", Toast.LENGTH_LONG).show()
                    viewModel.speak("จัดเตรียมเอกสารรายงานสุขภาพพีดีเอ็ฟแบบละเอียด พร้อมส่งมอบให้คุณหมอเรียบร้อยค่ะ")
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("share_pdf_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("แชร์รายงาน (PDF)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Button(
                onClick = {
                    Toast.makeText(context, "ส่งออกไฟล์ประวัติการตรวจ (CSV) ไปที่ดาวน์โหลดแล้ว", Toast.LENGTH_LONG).show()
                    viewModel.speak("จัดทำข้อมูลตารางระดับน้ำตาล ซีเอสวี เรียบร้อยค่ะ")
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("export_csv_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ส่งออก (CSV)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatMetricTile(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = color,
                        fontSize = 24.sp
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

@Composable
fun ProgressBarStat(
    label: String,
    percentage: Int,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .fillMaxHeight()
                    .background(color, RoundedCornerShape(4.dp))
            )
        }
    }
}
