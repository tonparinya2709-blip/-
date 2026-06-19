package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GlucoseRecord
import com.example.ui.HealthViewModel
import com.example.ui.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlucoseScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val records by viewModel.glucoseRecords.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSlotForAdd by remember { mutableStateOf("ก่อนอาหารเช้า") }
    var glucoseInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // 7 standard meal/bed slots
    val timeSlots = listOf(
        "ก่อนอาหารเช้า" to "07:00",
        "หลังอาหารเช้า" to "09:00",
        "ก่อนอาหารกลางวัน" to "12:00",
        "หลังอาหารกลางวัน" to "14:00",
        "ก่อนอาหารเย็น" to "17:00",
        "หลังอาหารเย็น" to "19:00",
        "ก่อนนอน" to "21:30"
    )

    // Filter records for the active selected day
    val dayRecords = records.filter { it.date == selectedDate }
    val recordMap = dayRecords.associateBy { it.timeSlot }

    // Date navigation math helper
    val changeDate: (Int) -> Unit = { daysOffset ->
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val current = sdf.parse(selectedDate)
            if (current != null) {
                val cal = Calendar.getInstance().apply { time = current }
                cal.add(Calendar.DAY_OF_YEAR, daysOffset)
                viewModel.selectDate(sdf.format(cal.time))
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Selector Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { changeDate(-1) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "วันก่อนหน้า",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp).padding(end = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = DateUtils.toThaiFullDate(selectedDate),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                IconButton(onClick = { changeDate(1) }) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "วันถัดไป",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Checklist of clinical slots
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "รายการตรวจบันทึกประจำวัน",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                timeSlots.forEach { (slot, schedTime) ->
                    val slotRecord = recordMap[slot]
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = if (slotRecord != null) 0.35f else 0.12f
                                ),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                selectedSlotForAdd = slot
                                glucoseInput = slotRecord?.valMgDl?.toString() ?: ""
                                noteInput = slotRecord?.note ?: ""
                                showAddDialog = true
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Circular Meal Icon Indicator
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        color = if (slotRecord != null) Color(0xFF00875A).copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.2f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        slot.contains("เช้า") -> Icons.Default.WbSunny
                                        slot.contains("กลางวัน") -> Icons.Default.LightMode
                                        slot.contains("เย็น") -> Icons.Default.WbTwilight
                                        else -> Icons.Default.Bedtime
                                    },
                                    contentDescription = null,
                                    tint = if (slotRecord != null) Color(0xFF00875A) else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = slot,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "เวลาประมาณ $schedTime น.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Right Side Glucose Display & Emoji Indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (slotRecord != null) {
                                val isHigh = slotRecord.valMgDl > profile.targetMax
                                val isLow = slotRecord.valMgDl < profile.targetMin
                                
                                val statusColor = when {
                                    isHigh -> Color(0xFFDE350B)
                                    isLow -> Color(0xFFEE9A00)
                                    else -> Color(0xFF00875A)
                                }
                                
                                val emoji = when {
                                    isHigh -> "☹️" // Sad/angry face
                                    isLow -> "⚠️"  // Caution warning
                                    else -> "😊"   // Green smiling face
                                }

                                Text(
                                    text = "${slotRecord.valMgDl} mg/dL",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = statusColor
                                    ),
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = emoji,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.deleteGlucoseRecord(slotRecord.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "ลบข้อมูล",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "-- mg/dL",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "กดเพื่อบันทึก",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Central quick advice card based on target levels
        val dayAvg = if (dayRecords.isNotEmpty()) dayRecords.map { it.valMgDl }.average().toInt() else 0
        if (dayAvg > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (dayAvg > profile.targetMax) Color(0xFFDE350B).copy(alpha = 0.08f) else Color(0xFF00875A).copy(alpha = 0.08f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (dayAvg > profile.targetMax) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (dayAvg > profile.targetMax) Color(0xFFDE350B) else Color(0xFF00875A),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "ค่าเฉลี่ยระดับน้ำตาลวันนี้: $dayAvg mg/dL",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val advice = if (dayAvg > profile.targetMax) {
                            "ค่อนข้างสูงกว่าค่าเป้าหมาย ควรควบคุมอาหารและแจ้งให้แพทย์หรือคนใกล้ตัวทราบ"
                        } else {
                            "อยู่ในเกณฑ์เป้าหมายดีเยี่ยม (${profile.targetMin} - ${profile.targetMax} mg/dL) รักษาระเบียบแบบนี้ต่อไปนะคะ"
                        }
                        Text(
                            text = advice,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Modal Add/Edit record dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "บันทึกระดับน้ำตาล - $selectedSlotForAdd",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = glucoseInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) glucoseInput = input
                        },
                        label = { Text("ระดับน้ำตาล (mg/dL)") },
                        placeholder = { Text("ระบุตัวเลข เช่น 120") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("glucose_input_field"),
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.Bloodtype, contentDescription = null, tint = Color(0xFFDE350B))
                        }
                    )

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("บันทึกเพิ่มเติม (หมายเหตุ)") },
                        placeholder = { Text("ตรวจหลังอาหารทันที ทานแป้งมื้อหนัก ฯลฯ") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = Color.Gray)
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val valueNum = glucoseInput.toIntOrNull()
                        if (valueNum != null) {
                            viewModel.addGlucoseRecord(valueNum, selectedSlotForAdd, selectedDate, noteInput)
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("glucose_save_button")
                ) {
                    Text("บันทึกข้อมูล")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("ยกเลิก")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
