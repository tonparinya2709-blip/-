package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {
    @Query("SELECT * FROM glucose_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<GlucoseRecord>>

    @Query("SELECT * FROM glucose_records WHERE date = :date")
    fun getRecordsByDate(date: String): Flow<List<GlucoseRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GlucoseRecord)

    @Delete
    suspend fun deleteRecord(record: GlucoseRecord)

    @Query("DELETE FROM glucose_records WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY timeStr ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine)

    @Delete
    suspend fun deleteMedicine(medicine: Medicine)

    @Query("DELETE FROM medicines WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface MedicineLogDao {
    @Query("SELECT * FROM medicine_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<MedicineLog>>

    @Query("SELECT * FROM medicine_logs WHERE date = :date")
    fun getLogsByDate(date: String): Flow<List<MedicineLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: MedicineLog)

    @Delete
    suspend fun deleteLog(log: MedicineLog)

    @Query("SELECT * FROM medicine_logs WHERE medicineId = :medicineId AND date = :date LIMIT 1")
    suspend fun getLogForMedicineOnDate(medicineId: Int, date: String): MedicineLog?
    
    @Query("DELETE FROM medicine_logs WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY date ASC, time ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfileSync(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)
}
