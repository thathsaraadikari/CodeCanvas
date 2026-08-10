package com.KotEdit.mobiletexteditor.ui

import android.content.Context
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.KotEdit.mobiletexteditor.data.FileVersion
import com.KotEdit.mobiletexteditor.data.VersionControlRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.nio.charset.Charset
import android.widget.Toast

class EditorViewModel : ViewModel() {
    private val _currentFileName = MutableStateFlow("Untitled.txt")
    val currentFileName: StateFlow<String> = _currentFileName.asStateFlow()

    private val CACHE_FILE_NAME = ".crash_recovery_cache.txt"

    private val _textValue = MutableStateFlow(TextFieldValue(""))
    val textValue: StateFlow<TextFieldValue> = _textValue.asStateFlow()

    private val _recentFiles = MutableStateFlow<List<String>>(emptyList())
    val recentFiles: StateFlow<List<String>> = _recentFiles.asStateFlow()

    private val _cursorLine = MutableStateFlow(1)
    val cursorLine: StateFlow<Int> = _cursorLine.asStateFlow()

    private val _cursorColumn = MutableStateFlow(1)
    val cursorColumn: StateFlow<Int> = _cursorColumn.asStateFlow()

    // Search and Replace States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _replaceQuery = MutableStateFlow("")
    val replaceQuery: StateFlow<String> = _replaceQuery.asStateFlow()

    private val _isSearchVisible = MutableStateFlow(false)
    val isSearchVisible: StateFlow<Boolean> = _isSearchVisible.asStateFlow()

    private val _isReadOnly = MutableStateFlow(false)
    val isReadOnly: StateFlow<Boolean> = _isReadOnly.asStateFlow()

    private val _isWordWrapEnabled = MutableStateFlow(true)
    val isWordWrapEnabled: StateFlow<Boolean> = _isWordWrapEnabled.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _fileVersions = MutableStateFlow<List<FileVersion>>(emptyList())
    val fileVersions: StateFlow<List<FileVersion>> = _fileVersions.asStateFlow()

    private var vcsRepo: VersionControlRepository? = null

    // Undo/Redo Stacks
    private val undoStack = mutableListOf<TextFieldValue>()
    private val redoStack = mutableListOf<TextFieldValue>()
    private var isUndoRedoAction = false

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onReplaceQueryChange(query: String) {
        _replaceQuery.value = query
    }

    fun toggleSearch() {
        _isSearchVisible.value = !_isSearchVisible.value
        if (!_isSearchVisible.value) {
            _searchQuery.value = ""
            _replaceQuery.value = ""
        }
    }

    fun replaceAll() {
        if (_searchQuery.value.isNotEmpty()) {
            val currentText = _textValue.value.text
            val newText = currentText.replace(_searchQuery.value, _replaceQuery.value)
            if (currentText != newText) {
                undoStack.add(_textValue.value)
                _textValue.value = TextFieldValue(newText)
                redoStack.clear()
            }
        }
    }

    fun onTextChange(newValue: TextFieldValue) {
        if (!isUndoRedoAction && newValue.text != _textValue.value.text) {
            // Save current state to undo stack before changing
            undoStack.add(_textValue.value)
            // Limit stack size to prevent memory issues
            if (undoStack.size > 50) undoStack.removeAt(0)
            // Clear redo stack on new typing
            redoStack.clear()
        }
        _textValue.value = newValue
        isUndoRedoAction = false
        updateCursorPosition(newValue)
    }

    private fun updateCursorPosition(value: TextFieldValue) {
        val cursorOffset = value.selection.start
        if (cursorOffset < 0 || cursorOffset > value.text.length) return
        
        val textBeforeCursor = value.text.substring(0, cursorOffset)
        val line = textBeforeCursor.count { it == '\n' } + 1
        val col = cursorOffset - textBeforeCursor.lastIndexOf('\n')
        
        _cursorLine.value = line
        _cursorColumn.value = col
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            isUndoRedoAction = true
            redoStack.add(_textValue.value)
            _textValue.value = undoStack.removeLast()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            isUndoRedoAction = true
            undoStack.add(_textValue.value)
            _textValue.value = redoStack.removeLast()
        }
    }

    fun loadRecentFiles(context: Context) {
        viewModelScope.launch {
            val files = context.filesDir.listFiles()?.map { it.name } ?: emptyList()
            _recentFiles.value = files
        }
    }

    fun newFile() {
        _currentFileName.value = "Untitled.txt"
        _textValue.value = TextFieldValue("")
        undoStack.clear()
        redoStack.clear()
    }

    fun saveFile(context: Context, fileName: String = _currentFileName.value, charset: Charset = Charsets.UTF_8) {
        if (_isReadOnly.value) return // Prevent saving if locked
        
        viewModelScope.launch {
            if (vcsRepo == null) vcsRepo = VersionControlRepository(context)
            
            vcsRepo?.saveVersion(fileName, _textValue.value.text, charset)
            
            _currentFileName.value = fileName
            loadRecentFiles(context)
            // Clear cache after explicit save
            File(context.filesDir, CACHE_FILE_NAME).delete()
            
            Toast.makeText(context, "File Saved Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun openFile(context: Context, fileName: String) {
        viewModelScope.launch {
            if (vcsRepo == null) vcsRepo = VersionControlRepository(context)
            
            val file = File(context.filesDir, fileName)
            if (file.exists()) {
                val text = file.readText()
                _currentFileName.value = fileName
                _textValue.value = TextFieldValue(text)
                _isReadOnly.value = vcsRepo?.isReadOnly(fileName) ?: false
                undoStack.clear()
                redoStack.clear()
            }
        }
    }

    fun toggleReadOnly(context: Context) {
        viewModelScope.launch {
            if (vcsRepo == null) vcsRepo = VersionControlRepository(context)
            val newState = !_isReadOnly.value
            vcsRepo?.setReadOnly(_currentFileName.value, newState)
            _isReadOnly.value = newState
        }
    }

    fun toggleWordWrap() {
        _isWordWrapEnabled.value = !_isWordWrapEnabled.value
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun formatCode() {
        val code = _textValue.value.text
        if (code.isBlank()) return

        // Pre-process: ensure braces have newlines around them for the line-by-line algorithm to catch them
        // We avoid messing with braces that might be next to each other by replacing them carefully
        val preProcessedCode = code
            .replace("{", "{\n")
            .replace("}", "\n}\n")

        val lines = preProcessedCode.split("\n")
        var indentLevel = 0
        val formatted = StringBuilder()
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue // Skip empty lines generated by the pre-processor
            
            if (trimmed.startsWith("}")) {
                indentLevel = maxOf(0, indentLevel - 1)
            }
            
            val indent = "    ".repeat(indentLevel)
            formatted.append(indent).append(trimmed).append("\n")
            
            if (trimmed.endsWith("{")) {
                indentLevel++
            }
        }
        
        val newText = formatted.toString().trimEnd()
        if (newText != code) {
            undoStack.add(_textValue.value)
            _textValue.value = TextFieldValue(newText)
            redoStack.clear()
        }
    }

    fun loadVersions(context: Context) {
        viewModelScope.launch {
            if (vcsRepo == null) vcsRepo = VersionControlRepository(context)
            _fileVersions.value = vcsRepo?.getAllVersions(_currentFileName.value) ?: emptyList()
        }
    }

    fun restoreVersion(context: Context, targetVersion: Int) {
        viewModelScope.launch {
            if (vcsRepo == null) vcsRepo = VersionControlRepository(context)
            val restoredText = vcsRepo?.reconstructFileAtVersion(_currentFileName.value, targetVersion) ?: return@launch
            
            _textValue.value = TextFieldValue(restoredText)
            undoStack.clear()
            redoStack.clear()
            
            // Automatically save this restored state as the newest version
            saveFile(context, _currentFileName.value)
        }
    }

    fun saveToCache(context: Context) {
        viewModelScope.launch {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            file.writeText(_textValue.value.text)
        }
    }

    fun checkCache(context: Context) {
        viewModelScope.launch {
            val file = File(context.filesDir, CACHE_FILE_NAME)
            if (file.exists() && file.readText().isNotBlank()) {
                _textValue.value = TextFieldValue(file.readText())
                _currentFileName.value = "Recovered_File.txt"
                // Don't delete it yet, wait for user to explicitly save
            }
        }
    }
}

