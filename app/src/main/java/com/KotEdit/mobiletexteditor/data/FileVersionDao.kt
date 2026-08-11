package com.KotEdit.mobiletexteditor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
@JvmSuppressWildcards
interface FileVersionDao {
    @Insert
    suspend fun insertVersion(fileVersion: FileVersion): Long

    @Query("SELECT * FROM file_versions WHERE fileName = :fileName ORDER BY versionNumber ASC")
    suspend fun getAllVersionsForFile(fileName: String): List<FileVersion>

    @Query("SELECT * FROM file_versions WHERE fileName = :fileName ORDER BY versionNumber DESC LIMIT 1")
    suspend fun getLatestVersionForFile(fileName: String): FileVersion?
    
    @Query("UPDATE file_versions SET isReadOnly = :isReadOnly WHERE fileName = :fileName")
    suspend fun updateReadOnlyStatus(fileName: String, isReadOnly: Boolean): Int

    @Query("DELETE FROM file_versions WHERE fileName = :fileName")
    suspend fun deleteVersionsForFile(fileName: String)
}
