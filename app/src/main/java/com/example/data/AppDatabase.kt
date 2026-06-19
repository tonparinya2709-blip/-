package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GlucoseRecord::class,
        Medicine::class,
        MedicineLog::class,
        Appointment::class,
        UserProfile::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun glucoseDao(): GlucoseDao
    abstract fun medicineDao(): MedicineDao
    abstract fun medicineLogDao(): MedicineLogDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diacare_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Launch in Coroutine Scope setup to avoid blocking
                        scope.launch(Dispatchers.IO) {
                            val dbInstance = getDatabase(context, scope)
                            
                            // Insert default user profile
                            dbInstance.userProfileDao().insertProfile(UserProfile())
                            
                            // Insert default medicines from the mockup screenshot:
                            dbInstance.medicineDao().insertMedicine(
                                Medicine(name = "Glipizide 5 mg", instruction = "ก่อนอาหารเชา", timeStr = "07:30", frequency = "ทุกวัน")
                            )
                            dbInstance.medicineDao().insertMedicine(
                                Medicine(name = "Metformin 500 mg", instruction = "หลังอาหารเช้า", timeStr = "08:00", frequency = "ทุกวัน")
                            )
                            dbInstance.medicineDao().insertMedicine(
                                Medicine(name = "Metformin 500 mg", instruction = "หลังอาหารเย็น", timeStr = "18:00", frequency = "ทุกวัน")
                            )
                            
                            // Generate timestamps for latest 7 days.
                            // 14 พ.ค.: 150, 15 พ.ค.: 180, 16 พ.ค.: 210, 17 พ.ค.: 241, 18 พ.ค.: 190, 19 พ.ค.: 165, 20 พ.ค.: 155
                            val dayMs = 24 * 60 * 60 * 1000L
                            val now = System.currentTimeMillis()
                            val values = listOf(150, 180, 210, 241, 190, 165, 155)
                            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            
                            for (i in 0 until 7) {
                                val logDate = sdf.format(java.util.Date(now - (6 - i) * dayMs))
                                dbInstance.glucoseDao().insertRecord(
                                    GlucoseRecord(
                                        date = logDate,
                                        timeSlot = when (i) {
                                            0 -> "ก่อนอาหารเช้า"
                                            1 -> "หลังอาหารเช้า"
                                            2 -> "ก่อนอาหารกลางวัน"
                                            3 -> "หลังอาหารเช้า" // Peak value of 241 mg/dL
                                            4 -> "ก่อนอาหารเย็น"
                                            5 -> "หลังอาหารเย็น"
                                            else -> "ก่อนนอน"
                                        },
                                        valMgDl = values[i],
                                        note = if (i == 3) "กินมื้อเช้าหนักแป้ง รู้สึกคอแห้งเล็กน้อย" else "รู้สึกปกติ แข็งแรงดี",
                                        timestamp = now - (6 - i) * dayMs
                                    )
                                )
                            }
                            
                            // Seed medical compliance history (to display 92% and some pill statistics)
                            // 32 times On-time, 3 times Late, 0 skipped
                            for (d in 0..11) {
                                val logDate = sdf.format(java.util.Date(now - d * dayMs))
                                val isLate = (d == 2 || d == 5 || d == 8)
                                
                                dbInstance.medicineLogDao().insertLog(
                                    MedicineLog(
                                        medicineId = 1,
                                        medicineName = "Glipizide 5 mg",
                                        date = logDate,
                                        takenTime = "07:28",
                                        status = "ตรงเวลา",
                                        timestamp = now - d * dayMs - 12 * 60 * 60 * 1000L
                                    )
                                )
                                dbInstance.medicineLogDao().insertLog(
                                    MedicineLog(
                                        medicineId = 2,
                                        medicineName = "Metformin 500 mg",
                                        date = logDate,
                                        takenTime = if (isLate && d == 2) "09:15" else "08:05",
                                        status = if (isLate && d == 2) "ล่าช้า" else "ตรงเวลา",
                                        timestamp = now - d * dayMs - 11 * 60 * 60 * 1000L
                                    )
                                )
                                dbInstance.medicineLogDao().insertLog(
                                    MedicineLog(
                                        medicineId = 3,
                                        medicineName = "Metformin 500 mg",
                                        date = logDate,
                                        takenTime = if (isLate && d == 5) "19:40" else "18:02",
                                        status = if (isLate && d == 5) "ล่าช้า" else "ตรงเวลา",
                                        timestamp = now - d * dayMs - 1 * 60 * 60 * 1000L
                                    )
                                )
                            }
                            
                            // Create upcoming appointment:
                            dbInstance.appointmentDao().insertAppointment(
                                Appointment(
                                    hospital = "โรงพยาบาลแพทย์รังสิต",
                                    department = "แผนกเบาหวาน ต่อมไร้ท่อ",
                                    date = sdf.format(java.util.Date(now + 3 * dayMs)),
                                    time = "09:00",
                                    reminderEnabled = true
                                )
                            )
                            dbInstance.appointmentDao().insertAppointment(
                                Appointment(
                                    hospital = "โรงพยาบาลแพทย์รังสิต",
                                    department = "แผนกเบาหวาน ต่อมไร้ท่อ",
                                    date = sdf.format(java.util.Date(now + 25 * dayMs)),
                                    time = "09:00",
                                    reminderEnabled = true
                                )
                            )
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
