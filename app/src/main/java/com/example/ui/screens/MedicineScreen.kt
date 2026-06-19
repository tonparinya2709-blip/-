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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Medicine
import com.example.ui.HealthViewModel

@Composable
fun MedicineScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val medicines by viewModel.medicines.collectAsState()
    val logs by viewModel.medicineLogs.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var showAddMedDialog by remember { mutableStateOf(false) }
    var medName by remember { mutableStateOf("") }
    var medDosage by remember { mutableStateOf("") }
    var medInstruction by remember { mutableStateOf("ก่อนอาหารเช้า") }
    var medTime by remember { mutableStateOf("08:00") }

    val todayStr = HealthViewModel.getTodayDateStr()
    val todayLogs = logs.filter { it.date == todayStr }
    val todayTakenSet = todayLogs.map { it.medicineId }.toSet()

    val scrollState = rememberScrollState()

    // Calculated stats
    val onTimeCount = logs.count { it.status == "ตรงเวลา" }
    val lateCount = logs.count { it.status == "ล่าช้า" }
    val totalCount = logs.size
    val compliancePct = if (totalCount > 0) (onTimeCount * 100) / totalCount else 92

    // Instructions available
    val instructionsList = listOf(
        "ก่อนอาหารเช้า",
        "หลังอาหารเช้า",
        "ก่อนอาหารกลางวัน",
        "หลังอาหารกลางวัน",
        "ก่อนอาหารเย็น",
        "หลังอาหารเย็น",
        "ก่อนนอน"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Title Row with + Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ตารางทานยาประจำวัน",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "บันทึกและตรวจเช็คปริมาณยาสม่ำเสมอเพื่อสุขภาพที่ดี",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showAddMedDialog = true },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                    .testTag("add_medicine_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "เพิ่มยาใหม่",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Compliance mini card
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
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "สถิติทานยาช่วงนี้",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "ตรงเวลา $onTimeCount ครั้ง", style = MaterialTheme.typography.bodySmall, color = Color(0xFF00875A))
                        Text(text = "ล่าช้า $lateCount ครั้ง", style = MaterialTheme.typography.bodySmall, color = Color(0xFFDE350B))
                    }
                }
                Box(
                    modifier = Modifier
                        .background(Color(0xFF00875A).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$compliancePct% ตรงเวลา",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00875A)
                        )
                    )
                }
            }
        }

        // Active Medicines List
        if (medicines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ยังไม่มีรายการยาที่คุณต้องกินหลักในระบบ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    TextButton(onClick = { showAddMedDialog = true }) {
                        Text("กดเพื่อเพิ่มยารายการแรกของคุณ")
                    }
                }
            }
        } else {
            medicines.forEach { medicine ->
                val isTaken = todayTakenSet.contains(medicine.id)
                val logItem = todayLogs.find { it.medicineId == medicine.id }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isTaken) {
                            Color(0xFFE2F0D9).copy(alpha = 0.25f) // Warm subtle green if completed
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            if (isTaken) Color(0xFF00875A) else Color(0xFFDE350B),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = medicine.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${medicine.instruction} | เวลา ${medicine.timeStr} น. (${medicine.dosage})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (isTaken && logItem != null) {
                                Text(
                                    text = "ทานแล้วเวลา ${logItem.takenTime} น. (${logItem.status})",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF00875A),
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        // Right actions (Toggle taken, delete)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { viewModel.toggleMedicineTaken(medicine, todayStr) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTaken) Color(0xFF00875A) else MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("pills_toggle_button_${medicine.id}")
                            ) {
                                if (isTaken) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("กินแล้ว", fontSize = 12.sp)
                                } else {
                                    Text("รอกินยา", fontSize = 12.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { viewModel.deleteMedicine(medicine.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "ลบสูตรยา",
                                    tint = Color.Gray.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Voice settings info footer card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "เครื่องเตือนความจำด้วยเสียง",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    val statusText = if (profile.voiceEnabled) {
                        "เปิดใช้งานระดับเสียง ${profile.voiceVolume}% (สำเนียงผู้หญิง${profile.voiceGender})"
                    } else {
                        "ปิดระบบแจ้งเตือนเสียงชั่วคราว"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Modal adding medicine dialog
    if (showAddMedDialog) {
        AlertDialog(
            onDismissRequest = { showAddMedDialog = false },
            title = {
                Text(
                    text = "เพิ่มยารายการตรวจเช็คใหม่",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = medName,
                        onValueChange = { medName = it },
                        label = { Text("ชื่อยา") },
                        placeholder = { Text("ระบุ เช่น Metformin") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("med_name_input")
                    )

                    OutlinedTextField(
                        value = medDosage,
                        onValueChange = { medDosage = it },
                        label = { Text("ขนาดการทาน / โดส") },
                        placeholder = { Text("ระบุ เช่น 500 mg, 1 เม็ด") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("med_dosage_input")
                    )

                    // Instruction selector drop-down style logic
                    Column {
                        Text(
                            text = "ช่วงเวลากลุ่มอาหารหลัก",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            instructionsList.take(4).forEach { option ->
                                AssistChip(
                                    onClick = { medInstruction = option },
                                    label = { Text(option, fontSize = 10.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (medInstruction == option) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            instructionsList.drop(4).forEach { option ->
                                AssistChip(
                                    onClick = { medInstruction = option },
                                    label = { Text(option, fontSize = 10.sp) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (medInstruction == option) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = medTime,
                        onValueChange = { medTime = it },
                        label = { Text("เวลาที่ต้องทานยา (รูปแบบ HH:MM)") },
                        placeholder = { Text("เช่น 08:30, 18:00") },
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth().testTag("med_time_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (medName.isNotBlank() && medTime.isNotBlank()) {
                            viewModel.addMedicine(medName, medDosage, medInstruction, medTime)
                            showAddMedDialog = false
                            // Reset state
                            medName = ""
                            medDosage = ""
                        }
                    },
                    modifier = Modifier.testTag("med_save_button")
                ) {
                    Text("บันทึก")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMedDialog = false }) {
                    Text("ยกเลิก")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
