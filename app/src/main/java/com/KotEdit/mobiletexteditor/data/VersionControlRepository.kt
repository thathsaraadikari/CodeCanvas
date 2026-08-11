package com.KotEdit.mobiletexteditor.data

import android.content.Context
import com.github.difflib.DiffUtils
import com.github.difflib.UnifiedDiffUtils
import java.io.File

import java.nio.charset.Charset

class VersionControlRepository(private val context: Context) {
    private val dao = AppDatabase.getDatabase(context).fileVersionDao()

    suspend fun saveVersion(fileName: String, currentText: String, charset: Charset = Charsets.UTF_8) {
        val latestVersion = dao.getLatestVersionForFile(fileName)
        val timestamp = System.currentTimeMillis()

        if (latestVersion == null) {
            // First version: save full text
            val fileVersion = FileVersion(
                fileName = fileName,
                versionNumber = 1,
                timestamp = timestamp,
                patchData = currentText
            )
            dao.insertVersion(fileVersion)
            
            // Also save to actual file system as per requirements
            File(context.filesDir, fileName).writeText(currentText, charset)
        } else {
            // Subsequent version: save delta
            val baseText = reconstructFileAtVersion(fileName, latestVersion.versionNumber)
            val originalLines = baseText.lines()
            val revisedLines = currentText.lines()
            
            val patch = DiffUtils.diff(originalLines, revisedLines)
            val diffList = UnifiedDiffUtils.generateUnifiedDiff(fileName, fileName, originalLines, patch, 3)
            val diffString = diffList.joinToString("\n")

            // Only save if there's an actual change
            if (patch.deltas.isNotEmpty()) {
                val fileVersion = FileVersion(
                    fileName = fileName,
                    versionNumber = latestVersion.versionNumber + 1,
                    timestamp = timestamp,
                    patchData = diffString
                )
                dao.insertVersion(fileVersion)
                
                // Overwrite physical file to reflect latest state
                File(context.filesDir, fileName).writeText(currentText, charset)
            }
        }
    }

    suspend fun reconstructFileAtVersion(fileName: String, targetVersion: Int): String {
        val allVersions = dao.getAllVersionsForFile(fileName)
        if (allVersions.isEmpty()) return ""

        // Version 1 is always the full base text
        var currentLines = allVersions.first().patchData.lines()

        // Apply patches sequentially up to target version
        for (i in 1 until allVersions.size) {
            val version = allVersions[i]
            if (version.versionNumber > targetVersion) break

            val patchLines = version.patchData.lines()
            if (patchLines.isNotEmpty()) {
                val patch = UnifiedDiffUtils.parseUnifiedDiff(patchLines)
                currentLines = DiffUtils.patch(currentLines, patch)
            }
        }

        return currentLines.joinToString("\n")
    }
    
    suspend fun setReadOnly(fileName: String, isReadOnly: Boolean) {
        dao.updateReadOnlyStatus(fileName, isReadOnly)
    }

    suspend fun isReadOnly(fileName: String): Boolean {
        return dao.getLatestVersionForFile(fileName)?.isReadOnly ?: false
    }
    
    suspend fun getAllVersions(fileName: String): List<FileVersion> {
        return dao.getAllVersionsForFile(fileName)
    }

    // Restore a version to disk WITHOUT creating a new version entry in the DB
    suspend fun restoreToFile(fileName: String, text: String, charset: Charset = Charsets.UTF_8) {
        File(context.filesDir, fileName).writeText(text, charset)
    }
}
