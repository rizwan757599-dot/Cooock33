package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VoiceCommand
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceCommandDao {
    @Query("SELECT * FROM voice_commands ORDER BY timestamp DESC")
    fun getAllCommands(): Flow<List<VoiceCommand>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommand(command: VoiceCommand): Long

    @Delete
    suspend fun deleteCommand(command: VoiceCommand)

    @Query("DELETE FROM voice_commands")
    suspend fun clearAll()
}
