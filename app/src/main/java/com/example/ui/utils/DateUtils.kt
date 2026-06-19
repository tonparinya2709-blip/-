package com.example.ui.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val TH_MONTHS_SHORT = listOf("ม.ค.", "ก.พ.", "มี.ค.", "เม.ย.", "พ.ค.", "มิ.ย.", "ก.ค.", "ส.ค.", "ก.ย.", "ต.ค.", "พ.ย.", "ธ.ค.")
    private val TH_MONTHS_FULL = listOf("มกราคม", "กุมภาพันธ์", "มีนาคม", "เมษายน", "พฤษภาคม", "มิถุนายน", "กรกฎาคม", "สิงหาคม", "กันยายน", "ตุลาคม", "พฤศจิกายน", "ธันวาคม")
    private val TH_DAYS_FULL = listOf("วันอาทิตย์", "วันจันทร์", "วันอังคาร", "วันพุธ", "วันพฤหัสบดี", "วันศุกร์", "วันเสาร์")

    fun toThaiShortDate(dateStr: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val monthIdx = cal.get(Calendar.MONTH)
            val yearBe = cal.get(Calendar.YEAR) + 543
            return "$day ${TH_MONTHS_SHORT[monthIdx]} $yearBe"
        } catch (e: Exception) {
            return dateStr
        }
    }

    fun toThaiFullDate(dateStr: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance().apply { time = date }
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val monthIdx = cal.get(Calendar.MONTH)
            val yearBe = cal.get(Calendar.YEAR) + 543
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 7 = Saturday
            val dayName = TH_DAYS_FULL[dayOfWeek - 1]
            return "${dayName}ที่ $day ${TH_MONTHS_FULL[monthIdx]} พ.ศ. $yearBe"
        } catch (e: Exception) {
            return dateStr
        }
    }

    fun getDayOfWeekThai(dateStr: String): String {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return ""
            val cal = Calendar.getInstance().apply { time = date }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            return TH_DAYS_FULL[dayOfWeek - 1]
        } catch (e: Exception) {
            return ""
        }
    }

    fun formatThaiDateFromTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val dateStr = sdf.format(Date(timestamp))
        return toThaiShortDate(dateStr)
    }

    fun formatThaiTimeFromTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        return sdf.format(Date(timestamp))
    }
}
