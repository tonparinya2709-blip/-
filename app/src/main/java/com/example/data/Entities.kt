package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_records")
data class GlucoseRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD
    val timeSlot: String, // e.g., "ก่อนอาหารเช้า", "หลังอาหารเช้า", "ก่อนอาหารกลางวัน", "หลังอาหารกลางวัน", "ก่อนอาหารเย็น", "หลังอาหารเย็น", "ก่อนนอน"
    val valMgDl: Int, // e.g., 141
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medicines")
data class Medicine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g., "Glipizide 5 mg", "Metformin 500 mg"
    val dosage: String = "",
    val instruction: String, // e.g., "ก่อนอาหารเช้า", "หลังอาหารเช้า"
    val timeStr: String, // e.g., "07:30", "08:00"
    val frequency: String = "ทุกวัน",
    val isActive: Boolean = true
)

@Entity(tableName = "medicine_logs")
data class MedicineLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val medicineId: Int,
    val medicineName: String,
    val date: String, // YYYY-MM-DD
    val takenTime: String, // e.g., "07:35"
    val status: String, // "ตรงเวลา", "ล่าช้า", "ข้ามยา"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hospital: String, // "โรงพยาบาลแพทย์รังสิต"
    val department: String, // "แผนกเบาหวาน ต่อมไร้ท่อ"
    val date: String, // YYYY-MM-DD
    val time: String, // "09:00"
    val reminderEnabled: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "พี่ต้น",
    val age: Int = 58,
    val diabetesType: String = "เบาหวานชนิดที่ 2",
    val targetMin: Int = 80,
    val targetMax: Int = 130,
    val voiceEnabled: Boolean = true,
    val voiceVolume: Int = 80,
    val voiceGender: String = "หญิง", // "หญิง" or "ชาย"
    val unit: String = "mg/dL",
    val theme: String = "มืด" // "มืด" or "สว่าง"
)
