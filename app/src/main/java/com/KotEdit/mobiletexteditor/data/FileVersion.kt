package com.KotEdit.mobiletexteditor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_versions")
data class FileVersion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fileName: String,
    val versionNumber: Int,
    val timestamp: Long,
    val patchData: String, // Full text for version 1, diff patch for version > 1
    val isReadOnly: Boolean = false
)
