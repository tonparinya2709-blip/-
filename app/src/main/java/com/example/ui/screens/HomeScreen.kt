package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.HealthViewModel
import com.example.ui.components.BloodGlucoseChart
import com.example.ui.components.ComplianceDonutChart
import com.example.ui.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val glucoseList by viewModel.glucoseRecords.collectAsState()
    val mdList by viewModel.medicines.collectAsState()
    val logs by viewModel.medicineLogs.collectAsState()
    val appts by viewModel.appointments.collectAsState()

    val scrollState = rememberScrollState()

    // Process variables
    val latestRecord = glucoseList.sortedBy { it.timestamp }.lastOrNull()
    
    // Compute pill adherence
    val totalMedsToday = mdList.size
    val todayStr = HealthViewModel.getTodayDateStr()
    val takenLogsToday = logs.filter { it.date == todayStr }
    val completedCount = takenLogsToday.size

    // Calculate over 7 days for compliance ring
    val last7DaysLogs = logs.filter { 
        System.currentTimeMillis() - it.timestamp <= 7 * 24 * 60 * 60 * 1000L 
    }
    val onTimeCount = last7DaysLogs.count { it.status == "ตรงเวลา" }
    val total7DaysCount = last7DaysLogs.size
    val compliancePct = if (total7DaysCount > 0) (onTimeCount * 100) / total7DaysCount else 92

    // Get next appointment
    val upcomingAppt = appts.filter { 
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val apptDate = sdf.parse(it.date)
            val todayDate = sdf.parse(todayStr)
            apptDate != null && todayDate != null && (apptDate.time >= todayDate.time)
        } catch(e: Exception) {
            true
        }
    }.sortedBy { it.date + " " + it.time }.firstOrNull()

    // Computing days left or countdown text
    val daysLeftStr = if (upcomingAppt != null) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        try {
            val apptDate = sdf.parse(upcomingAppt.date)
            val todayDate = sdf.parse(todayStr)
            if (apptDate != null && todayDate != null) {
                val diffMs = apptYValue(apptDate) - apptYValue(todayDate)
                val diffDays = diffMs / (1000 * 60 * 60 * 24L)
                if (diffDays == 0L) "วันนี้" else "อีก $diffDays วัน"
            } else "อีกเร็วๆ นี้"
        } catch (e: Exception) {
            "อีกเร็วๆ นี้"
        }
    } else {
        "ไม่มีนัดหมาย"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "สวัสดี, คุณ ${profile.name.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "ติดตามระดับน้ำตาล 🩸",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                )
            }
            
            // Dynamic Initial Circle Avatar (from Geometric Balance HTML design: bg #FFDADA with text #410002)
            val firstLetter = profile.name.take(1).uppercase()
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFDADA)) // Light rose bg
                    .clickable {
                        viewModel.speak("สวัสดีค่ะ ยินดีต้อนรับสู่เดียร์แคร์ทูเดย์ ระบบตรวจวัดน้ำตาลพร้อมผู้สนับสนุนเสียงของคุณค่ะ")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstLetter,
                    style = TextStyle(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color(0xFF410002) // Dark wine-maroon text
                    )
                )
            }
        }

        // Blood Glucose Highlight Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("blood_glucose_main_card"),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timeSlotText = latestRecord?.let { "ล่าสุด (${it.timeSlot})" } ?: "ยังไม่มีบันทึก"
                    // Pill Badge
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = timeSlotText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }

                    // Designer Dual Dots (from HTML spec)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Centered Metric Value
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = latestRecord?.valMgDl?.toString() ?: "--",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Light,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-2).sp
                        ),
                        fontSize = 72.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "mg/dL",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(bottom = 14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Real-time status text matching our health status threshold colors
                val isHigh = (latestRecord?.valMgDl ?: 0) > profile.targetMax
                val isLow = (latestRecord?.valMgDl ?: 100) < profile.targetMin
                
                val (statusText, statusColor, statusIcon) = when {
                    latestRecord == null -> Triple("ยังไม่มีบันทึกข้อมูลวันนี้", MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Info)
                    isHigh -> Triple("ระดับน้ำตาลสูงกว่าเกณฑ์", Color(0xFFDE350B), Icons.Default.Warning)
                    isLow -> Triple("ระดับน้ำตาลต่ำกว่าเกณฑ์", Color(0xFFEE9A00), Icons.Default.Warning)
                    else -> Triple("ระดับน้ำตาลปกติ", Color(0xFF00875A), Icons.Default.CheckCircle)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    )
                }
            }
        }

        // Quick Grid Highlights
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Pills compliance
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.selectTab("MEDS") },
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ยาที่ต้องกินวันนี้",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "$completedCount/$totalMedsToday รายการ",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Visual progress indicator (65% fill pattern representation)
                    val complianceProgress = if (totalMedsToday > 0) completedCount.toFloat() / totalMedsToday else 0.0f
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = if (complianceProgress > 0f) complianceProgress else 0.05f)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        )
                    }
                }
            }

            // Next Appointment Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.selectTab("DOCTOR") },
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "นัดหมายถัดไป",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = Color(0xFF0052CC),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = daysLeftStr,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = upcomingAppt?.date?.let { DateUtils.toThaiShortDate(it) } ?: "ไม่พบนัดหมายใหม่",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Compliance Donut
        ComplianceDonutChart(
            percentage = compliancePct,
            onTimeCount = onTimeCount,
            totalCount = total7DaysCount
        )

        // 7-day Blood Glucose Graph
        BloodGlucoseChart(
            records = glucoseList,
            targetMin = profile.targetMin,
            targetMax = profile.targetMax
        )
    }
}

private fun apptYValue(date: Date): Long {
    val cal = Calendar.getInstance().apply { time = date }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}
