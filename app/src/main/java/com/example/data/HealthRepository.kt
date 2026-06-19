package com.example.data

import kotlinx.coroutines.flow.Flow

class HealthRepository(private val db: AppDatabase) {

    // Glucose Records
    val allGlucoseRecords: Flow<List<GlucoseRecord>> = db.glucoseDao().getAllRecords()
    fun getGlucoseByDate(date: String): Flow<List<GlucoseRecord>> = db.glucoseDao().getRecordsByDate(date)
    suspend fun insertGlucose(record: GlucoseRecord) = db.glucoseDao().insertRecord(record)
    suspend fun deleteGlucoseById(id: Int) = db.glucoseDao().deleteById(id)

    // Medicines
    val allMedicines: Flow<List<Medicine>> = db.medicineDao().getAllMedicines()
    suspend fun insertMedicine(medicine: Medicine) = db.medicineDao().insertMedicine(medicine)
    suspend fun deleteMedicineById(id: Int) = db.medicineDao().deleteById(id)

    // Medication Logs
    val allMedicineLogs: Flow<List<MedicineLog>> = db.medicineLogDao().getAllLogs()
    fun getLogsByDate(date: String): Flow<List<MedicineLog>> = db.medicineLogDao().getLogsByDate(date)
    suspend fun insertMedicineLog(log: MedicineLog) = db.medicineLogDao().insertLog(log)
    suspend fun getLogForMedicineOnDate(medicineId: Int, date: String): MedicineLog? =
        db.medicineLogDao().getLogForMedicineOnDate(medicineId, date)
    suspend fun deleteMedicineLogById(id: Int) = db.medicineLogDao().deleteById(id)

    // Appointments
    val allAppointments: Flow<List<Appointment>> = db.appointmentDao().getAllAppointments()
    suspend fun insertAppointment(appointment: Appointment) = db.appointmentDao().insertAppointment(appointment)
    suspend fun deleteAppointmentById(id: Int) = db.appointmentDao().deleteById(id)

    // User Profile
    val userProfile: Flow<UserProfile?> = db.userProfileDao().getProfile()
    suspend fun getProfileSync(): UserProfile? = db.userProfileDao().getProfileSync()
    suspend fun updateProfile(profile: UserProfile) = db.userProfileDao().insertProfile(profile)
}
