package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.CountdownTimerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CountdownTimerDao {

    @Query("SELECT * FROM countdown_timers ORDER BY isActive DESC, targetTimestamp ASC")
    fun getAllTimers(): Flow<List<CountdownTimerEntity>>

    @Query("SELECT * FROM countdown_timers WHERE isActive = 1 LIMIT 1")
    fun getActiveTimer(): Flow<CountdownTimerEntity?>

    @Query("SELECT * FROM countdown_timers WHERE id = :id LIMIT 1")
    suspend fun getTimerById(id: Long): CountdownTimerEntity?

    @Query("SELECT COUNT(*) FROM countdown_timers")
    suspend fun getTimerCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: CountdownTimerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimers(timers: List<CountdownTimerEntity>)

    @Update
    suspend fun updateTimer(timer: CountdownTimerEntity)

    @Delete
    suspend fun deleteTimer(timer: CountdownTimerEntity)

    @Query("DELETE FROM countdown_timers WHERE id = :id")
    suspend fun deleteTimerById(id: Long)

    @Query("UPDATE countdown_timers SET isActive = 0")
    suspend fun clearAllActive()

    @Query("UPDATE countdown_timers SET isActive = 1 WHERE id = :id")
    suspend fun setActiveFlag(id: Long)

    @Transaction
    suspend fun selectActiveTimer(id: Long) {
        clearAllActive()
        setActiveFlag(id)
    }
}
