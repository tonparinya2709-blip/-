package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Appointment
import com.example.ui.HealthViewModel
import com.example.ui.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DoctorScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val appointments by viewModel.appointments.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var hospitalInput by remember { mutableStateOf("โรงพยาบาลแพทย์รังสิต") }
    var deptInput by remember { mutableStateOf("แผนกเบาหวาน ต่อมไร้ท่อ") }
    var dateInput by remember { mutableStateOf("") }
    var timeInput by remember { mutableStateOf("09:00") }

    val todayStr = HealthViewModel.getTodayDateStr()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Separate future vs old appointments
    val sortedAppts = appointments.sortedBy { it.date + " " + it.time }
    val (futureAppts, pastAppts) = sortedAppts.partition {
        try {
            val dAppt = sdf.parse(it.date)
            val dToday = sdf.parse(todayStr)
            dAppt != null && dToday != null && dAppt.time >= dToday.time
        } catch(e: Exception) {
            true
        }
    }

    var activeSubTab by remember { mutableStateOf("UPCOMING") } // "UPCOMING" or "HISTORY"
    val scrollState = rememberScrollState()

    // Primary Next Appointment
    val primaryAppt = futureAppts.firstOrNull()

    // Countdown calculation
    val computeDaysLeft: (Appointment) -> String = { appt ->
        try {
            val dAppt = sdf.parse(appt.date)
            val dToday = sdf.parse(todayStr)
            if (dAppt != null && dToday != null) {
                val diff = dAppt.time - dToday.time
                val days = diff / (24 * 60 * 60 * 1000L)
                if (days == 0L) "วันนี้" else "อีก $days วัน"
            } else "เร็วๆ นี้"
        } catch(e: Exception) {
            "เร็วๆ นี้"
        }
    }

    if (dateInput.isBlank()) {
        // Initialize to standard 3 days from now
        try {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 3)
            dateInput = sdf.format(cal.time)
        } catch(e: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "นัดหมายแพทย์",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "ติดตามนัดเพื่อรับการตรวจและรับคำแนะนำจากอาจารย์แพทย์",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    .testTag("add_appointment_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "เพิ่มนัดหมายใหม่",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Segmented tab pill controller
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
            Button(
                onClick = { activeSubTab = "UPCOMING" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "UPCOMING") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeSubTab == "UPCOMING") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("นัดหมายที่จะถึง (${futureAppts.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            
            Button(
                onClick = { activeSubTab = "HISTORY" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubTab == "HISTORY") MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (activeSubTab == "HISTORY") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                Text("ประวัติการรักษา (${pastAppts.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        if (activeSubTab == "UPCOMING") {
            // First Spotlight Card if available
            if (primaryAppt != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "นัดหมายเร่งด่วน",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }

                            // Dynamic days left countdown display
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDE350B), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = computeDaysLeft(primaryAppt),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Hospital Name display
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalHospital,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = primaryAppt.hospital,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = primaryAppt.department,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Calendar detail line
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${DateUtils.toThaiShortDate(primaryAppt.date)} เวลา ${primaryAppt.time} น.",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.speak("คุณมีนัดนัดตรวจที่ ${primaryAppt.hospital} ${primaryAppt.department} วันที่ ${DateUtils.toThaiShortDate(primaryAppt.date)} นะคะ")
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color.White, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "เสียงเตือนสำหรับนัดหมาย",
                                    tint = Color(0xFF00875A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "รายการนัดหมายอื่นๆ",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                // List other future appointments
                futureAppts.drop(1).forEach { appt ->
                    ApptListItem(appt = appt, countdown = computeDaysLeft(appt), onDelete = { viewModel.deleteAppointment(appt.id) })
                }

                if (futureAppts.size <= 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ไม่มีนัดตรวจสำรองระยะยาวอื่นๆ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "ยังไม่มีกำหนดนัดหมายแพทย์ที่จะถึงค่ะ", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Button(onClick = { showAddDialog = true }, modifier = Modifier.padding(top = 10.dp)) {
                            Text("เพิ่มนัดหมายใหม่")
                        }
                    }
                }
            }
        } else {
            // Old historical appointments list
            if (pastAppts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "ยังไม่มีบันทึกข้อมูลการรักษาย้อนหลังในอุปกรณ์", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            } else {
                pastAppts.forEach { appt ->
                    ApptListItem(appt = appt, countdown = "เสร็จสิ้น", onDelete = { viewModel.deleteAppointment(appt.id) })
                }
            }
        }
    }

    // Modal adding form Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(
                    text = "เพิ่มรายการบันทึกนัดหมายแพทย์",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = hospitalInput,
                        onValueChange = { hospitalInput = it },
                        label = { Text("ชื่อสถานพยาบาล/โรงพยาบาล") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("appt_hospital_input")
                    )

                    OutlinedTextField(
                        value = deptInput,
                        onValueChange = { deptInput = it },
                        label = { Text("แผนกที่นัดตรวจ") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("appt_dept_input")
                    )

                    OutlinedTextField(
                        value = dateInput,
                        onValueChange = { dateInput = it },
                        label = { Text("วันที่นัดตรวจ (YYYY-MM-DD)") },
                        placeholder = { Text("เช่น 2026-06-23") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("appt_date_input")
                    )

                    OutlinedTextField(
                        value = timeInput,
                        onValueChange = { timeInput = it },
                        label = { Text("เวลา (เช่น 09:00)") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("appt_time_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hospitalInput.isNotBlank() && dateInput.isNotBlank() && timeInput.isNotBlank()) {
                            viewModel.addAppointment(hospitalInput, deptInput, dateInput, timeInput)
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("appt_save_button")
                ) {
                    Text("บันทึกนัด")
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

@Composable
private fun ApptListItem(
    appt: Appointment,
    countdown: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appt.hospital,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${appt.department} | ${DateUtils.toThaiShortDate(appt.date)} เวลา ${appt.time} น.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(
                            if (countdown == "เสร็จสิ้น") Color.Gray.copy(alpha = 0.15f) else Color(0xFF0052CC).copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = countdown,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (countdown == "เสร็จสิ้น") Color.Gray else Color(0xFF0052CC)
                        )
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "ลบนัดหมาย",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
