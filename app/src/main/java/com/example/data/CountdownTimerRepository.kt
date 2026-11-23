package com.example.data

import com.example.data.dao.CountdownTimerDao
import com.example.data.model.CountdownTimerEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

/**
 * Repository providing clean, reactive abstraction for multiple marketing launch countdown timers.
 * Handles database operations and initializes default strategic marketing campaigns.
 */
class CountdownTimerRepository(private val dao: CountdownTimerDao) {

    val allTimers: Flow<List<CountdownTimerEntity>> = dao.getAllTimers()
    val activeTimer: Flow<CountdownTimerEntity?> = dao.getActiveTimer()

    suspend fun insertTimer(timer: CountdownTimerEntity): Long {
        return dao.insertTimer(timer)
    }

    suspend fun updateTimer(timer: CountdownTimerEntity) {
        dao.updateTimer(timer)
    }

    suspend fun deleteTimerById(id: Long) {
        dao.deleteTimerById(id)
    }

    suspend fun selectActiveTimer(id: Long) {
        dao.selectActiveTimer(id)
    }

    suspend fun seedInitialTimersIfEmpty() {
        if (dao.getTimerCount() == 0) {
            val now = Calendar.getInstance()

            val cal1 = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 33) }
            val cal2 = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 21) }
            val cal3 = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 14) }

            val initialTimers = listOf(
                CountdownTimerEntity(
                    title = "Q4 Major Product Launch",
                    targetTimestamp = cal1.timeInMillis,
                    categoryTag = "Product",
                    notes = "Global cross-platform feature release with press embargo and VIP previews",
                    accentColorHex = "#00FFFF",
                    isActive = true
                ),
                CountdownTimerEntity(
                    title = "Black Friday Campaign",
                    targetTimestamp = cal2.timeInMillis,
                    categoryTag = "Growth",
                    notes = "Early-bird email sequences, paid search blitz, and tiered conversion discounts",
                    accentColorHex = "#BF40FF",
                    isActive = false
                ),
                CountdownTimerEntity(
                    title = "Creator & Influencer Drop",
                    targetTimestamp = cal3.timeInMillis,
                    categoryTag = "Social",
                    notes = "VIP kit unboxings and viral TikTok spark ads coordination",
                    accentColorHex = "#39FF14",
                    isActive = false
                )
            )
            dao.insertTimers(initialTimers)
        }
    }
}
