package com.example.ui

import android.app.Application
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HealthViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = HealthRepository(db)

    // Text To Speech
    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    // App Preferences / Navigation
    private val _selectedTab = MutableStateFlow("HOME") // "HOME", "GLUCOSE", "MEDS", "DOCTOR", "REPORT", "SETTINGS"
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    // Active Selected Date in Blood Glucose Logs
    private val _selectedDate = MutableStateFlow(getTodayDateStr())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Flows from Repository
    val glucoseRecords: StateFlow<List<GlucoseRecord>> = repository.allGlucoseRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicines: StateFlow<List<Medicine>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val medicineLogs: StateFlow<List<MedicineLog>> = repository.allMedicineLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfile())

    init {
        // Initialize standard Text To Speech
        try {
            tts = TextToSpeech(application, this)
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Failed to init TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("th", "TH"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Try fallback Locale("th")
                tts?.setLanguage(Locale("th"))
            }
            isTtsInitialized = true
        }
    }

    fun speak(text: String) {
        val profile = userProfile.value
        if (!profile.voiceEnabled) return

        if (isTtsInitialized && tts != null) {
            viewModelScope.launch(Dispatchers.Main) {
                // Adjust volume via TTS parameters if supported, set speech rate:
                val rate = when (profile.voiceGender) {
                    "ชาย" -> 0.85f
                    else -> 1.0f
                }
                tts?.setSpeechRate(rate)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "DiaCareTTS")
            }
        }
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
    }

    fun selectDate(dateStr: String) {
        _selectedDate.value = dateStr
    }

    // Core Glucose Operations
    fun addGlucoseRecord(valMgDl: Int, timeSlot: String, dateStr: String, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val record = GlucoseRecord(
                date = dateStr,
                timeSlot = timeSlot,
                valMgDl = valMgDl,
                note = note,
                timestamp = System.currentTimeMillis()
            )
            repository.insertGlucose(record)
            
            // Voice feedback for glucose status
            val profile = userProfile.value
            val status = when {
                valMgDl > profile.targetMax -> "ระดับน้ำตาลสูงกว่าเกณฑ์ค่ะ มีค่า $valMgDl มิลลิกรัมต่อเดซิลิตร กรุณาควบคุมอาหารแป้งและน้ำตาล หรือดื่มน้ำสะอาดเพิ่มนะคะ"
                valMgDl < profile.targetMin -> "ระดับน้ำตาลต่ำกว่าเกณฑ์ค่ะ มีค่า $valMgDl มิลลิกรัมต่อเดซิลิตร แนะนำให้รับประทานน้ำหวานหรืออาหารเพื่อพยุงอาการทันทีนะคะ"
                else -> "ระดับน้ำตาลอยู่ในเกณฑ์ปกติค่ะ มีค่า $valMgDl มิลลิกรัมต่อเดซิลิตร รักษาสุขภาพดีเยี่ยมแบบนี้ต่อไปนะคะ"
            }
            speak(status)
        }
    }

    fun deleteGlucoseRecord(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGlucoseById(id)
        }
    }

    // Core Medicine & Log Operations
    fun addMedicine(name: String, dosage: String, instruction: String, timeStr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val medicine = Medicine(
                name = name,
                dosage = dosage,
                instruction = instruction,
                timeStr = timeStr,
                frequency = "ทุกวัน"
            )
            repository.insertMedicine(medicine)
            speak("เพิ่มบันทึกยารายการใหม่เรียบร้อยแล้วค่ะ")
        }
    }

    fun deleteMedicine(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMedicineById(id)
        }
    }

    fun toggleMedicineTaken(medicine: Medicine, dateStr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingLog = repository.getLogForMedicineOnDate(medicine.id, dateStr)
            if (existingLog != null) {
                // Undo logging
                repository.deleteMedicineLogById(existingLog.id)
                speak("ยกเลิกการบันทึกทานยาเรียบร้อยค่ะ")
            } else {
                // Log medication as taken
                // Compute status based on schedule:
                val takenTime = getCurrentTimeStr()
                // Simple on-time heuristic: if taken within ±1.5 hours of schedule, otherwise late:
                val status = calculateAdherenceStatus(medicine.timeStr, takenTime)
                
                val log = MedicineLog(
                    medicineId = medicine.id,
                    medicineName = medicine.name,
                    date = dateStr,
                    takenTime = takenTime,
                    status = status,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertMedicineLog(log)
                speak("บันทึกทานยา ${medicine.name} เรียนร้อยแล้วค่ะ สถานะ: $status")
            }
        }
    }

    private fun calculateAdherenceStatus(scheduledTime: String, actualTime: String): String {
        try {
            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            val schedDate = sdf.parse(scheduledTime)
            val actDate = sdf.parse(actualTime)
            if (schedDate != null && actDate != null) {
                val diffMins = Math.abs(actDate.time - schedDate.time) / (60 * 1000)
                return if (diffMins <= 90) "ตรงเวลา" else "ล่าช้า"
            }
        } catch (e: Exception) {
            // Fallback
        }
        return "ตรงเวลา"
    }

    // Core Appointment Operations
    fun addAppointment(hospital: String, department: String, dateStr: String, timeStr: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val appointment = Appointment(
                hospital = hospital,
                department = department,
                date = dateStr,
                time = timeStr,
                reminderEnabled = true
            )
            repository.insertAppointment(appointment)
            speak("บันทึกการนัดหมายแพทย์เรียบร้อยแล้วค่ะ")
        }
    }

    fun deleteAppointment(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAppointmentById(id)
        }
    }

    // Core Profile & Settings Operations
    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProfile(profile)
            speak("บันทึกการตั้งค่าระบบเรียบร้อยแล้วค่ะ")
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Error shutting down TextToSpeech", e)
        }
    }

    companion object {
        fun getTodayDateStr(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(Date())
        }

        fun getCurrentTimeStr(): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.US)
            return sdf.format(Date())
        }
    }
}
