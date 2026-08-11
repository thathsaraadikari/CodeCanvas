package com.KotEdit.mobiletexteditor.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.nio.charset.Charset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(viewModel: EditorViewModel = viewModel()) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Collect states from ViewModel
    val currentFileName by viewModel.currentFileName.collectAsState()
    val textValue by viewModel.textValue.collectAsState()
    val recentFiles by viewModel.recentFiles.collectAsState()
    val isSearchVisible by viewModel.isSearchVisible.collectAsState()
    val isReadOnly by viewModel.isReadOnly.collectAsState()
    val isWordWrapEnabled by viewModel.isWordWrapEnabled.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val replaceQuery by viewModel.replaceQuery.collectAsState()
    val fileVersions by viewModel.fileVersions.collectAsState()
    var showSearchDialog by remember { mutableStateOf(false) }
    
    val cursorLine by viewModel.cursorLine.collectAsState()
    val cursorColumn by viewModel.cursorColumn.collectAsState()

    // UI States
    var showMenu by remember { mutableStateOf(false) }
    var showSaveAsDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var saveAsFileName by remember { mutableStateOf("") }
    var fileToDelete by remember { mutableStateOf<String?>(null) }
    
    // Encoding selection state
    val charsetOptions = listOf(Charsets.UTF_8, Charsets.US_ASCII, Charsets.ISO_8859_1, Charsets.UTF_16)
    var selectedCharset by remember { mutableStateOf(Charsets.UTF_8) }
    var expandedCharsetMenu by remember { mutableStateOf(false) }
    
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }

    LaunchedEffect(Unit) {
        viewModel.loadRecentFiles(context)
        viewModel.checkCache(context)
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000L) // Wait 10 seconds
            viewModel.saveToCache(context)
        }
    }

    if (showSaveAsDialog) {
        AlertDialog(
            onDismissRequest = { showSaveAsDialog = false },
            title = { Text("Save As") },
            text = {
                Column {
                    OutlinedTextField(
                        value = saveAsFileName,
                        onValueChange = { saveAsFileName = it },
                        label = { Text("File Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedCharsetMenu,
                        onExpandedChange = { expandedCharsetMenu = !expandedCharsetMenu }
                    ) {
                        OutlinedTextField(
                            value = selectedCharset.name(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Encoding") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCharsetMenu) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCharsetMenu,
                            onDismissRequest = { expandedCharsetMenu = false }
                        ) {
                            charsetOptions.forEach { charset ->
                                DropdownMenuItem(
                                    text = { Text(charset.name()) },
                                    onClick = {
                                        selectedCharset = charset
                                        expandedCharsetMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (saveAsFileName.isNotBlank()) {
                        val finalName = if (!saveAsFileName.endsWith(".txt") && !saveAsFileName.endsWith(".md") && !saveAsFileName.endsWith(".kt")) "${saveAsFileName}.txt" else saveAsFileName
                        viewModel.saveFile(context, finalName, selectedCharset)
                        showSaveAsDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveAsDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showSearchDialog) {
        var localSearchQuery by remember { mutableStateOf(searchQuery) }
        var localReplaceQuery by remember { mutableStateOf(replaceQuery) }
        
        AlertDialog(
            onDismissRequest = { showSearchDialog = false },
            title = { Text("Search & Replace") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = localSearchQuery,
                        onValueChange = { localSearchQuery = it },
                        label = { Text("Search") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = localReplaceQuery,
                        onValueChange = { localReplaceQuery = it },
                        label = { Text("Replace With") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onSearchQueryChange(localSearchQuery)
                    viewModel.onReplaceQueryChange(localReplaceQuery)
                    viewModel.replaceAll()
                    showSearchDialog = false
                }) { Text("Replace All") }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.onSearchQueryChange(localSearchQuery)
                    showSearchDialog = false 
                }) { Text("Search Only") }
            }
        )
    }

    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("Version History") },
            text = {
                var expandedVersion by remember { mutableStateOf<Int?>(null) }
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(fileVersions) { version ->
                        val isExpanded = expandedVersion == version.versionNumber
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedVersion = if (isExpanded) null else version.versionNumber }
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Version ${version.versionNumber}", 
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = dateFormatter.format(Date(version.timestamp)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(modifier = Modifier.padding(top = 8.dp)) {
                                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                                        val scrollState = rememberScrollState()
                                        Text(
                                            text = if (version.versionNumber == 1) "Initial Commit (Full text)" else version.patchData.ifEmpty { "No Changes" },
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
                                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).verticalScroll(scrollState),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        viewModel.restoreVersion(context, version.versionNumber)
                                        showHistoryDialog = false
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Restore this version")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("Close") }
            }
        )
    }

    if (fileToDelete != null) {
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to delete '$fileToDelete'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFile(context, fileToDelete!!)
                        fileToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxHeight().padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "CodeCanvas IDE", 
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                        )
                    }
                    
                    Surface(
                        onClick = { scope.launch { drawerState.close() } },
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(32.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Editor", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("RECENT FILES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(recentFiles) { fileName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .combinedClickable(
                                        onClick = {
                                            viewModel.openFile(context, fileName)
                                            scope.launch { drawerState.close() }
                                        },
                                        onLongClick = {
                                            fileToDelete = fileName
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically, 
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(fileName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column {
                    TopAppBar(
                        title = { 
                            Column {
                                Text(currentFileName.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                Text("/storage/internal/${currentFileName.ifBlank { "Untitled" }}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                viewModel.onTextChange(androidx.compose.ui.text.input.TextFieldValue(""))
                            }) {
                                Icon(Icons.Default.Add, contentDescription = "New", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { viewModel.toggleReadOnly(context) }) {
                                Icon(if (isReadOnly) Icons.Default.Lock else Icons.Default.Edit, contentDescription = "Toggle Read Only", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Save") },
                                        onClick = {
                                            viewModel.saveFile(context)
                                            showMenu = false
                                        },
                                        leadingIcon = { Icon(Icons.Default.Save, "Save") }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Save As...") },
                                        onClick = {
                                            saveAsFileName = currentFileName
                                            showSaveAsDialog = true
                                            showMenu = false
                                        }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Version History") },
                                        onClick = { 
                                            viewModel.loadVersions(context)
                                            showHistoryDialog = true
                                            showMenu = false 
                                        },
                                        leadingIcon = { Icon(Icons.Default.History, "History") }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Undo") },
                                        onClick = { viewModel.undo(); showMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Undo, "Undo") }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Redo") },
                                        onClick = { viewModel.redo(); showMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Redo, "Redo") }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("Search & Replace") },
                                        onClick = { showSearchDialog = true; showMenu = false },
                                        leadingIcon = { Icon(Icons.Default.Search, "Search") }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Word Wrap") },
                                        onClick = { viewModel.toggleWordWrap() },
                                        trailingIcon = { Checkbox(checked = isWordWrapEnabled, onCheckedChange = null) }
                                    )
                                    val isDarkMode by viewModel.isDarkMode.collectAsState()
                                    DropdownMenuItem(
                                        text = { Text("Dark Mode") },
                                        onClick = { viewModel.toggleDarkMode() },
                                        trailingIcon = { Checkbox(checked = isDarkMode, onCheckedChange = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Format Code") },
                                        onClick = { viewModel.formatCode(); showMenu = false }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            titleContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }
        ) { paddingValues ->
            val verticalScrollState = rememberScrollState()
            
            Crossfade(
                targetState = currentFileName,
                modifier = Modifier.padding(paddingValues).fillMaxSize(),
                label = "FileTransition"
            ) { _ ->
                Row(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).verticalScroll(verticalScrollState)
                ) {
                    // Line Numbers Gutter
                    Column(
                        modifier = Modifier
                            .width(48.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                            .padding(top = 16.dp, end = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        val lineCount = maxOf(1, textValue.text.count { it == '\n' } + 1)
                        for (i in 1..lineCount) {
                            Text(
                                text = i.toString(),
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                    
                    VerticalDivider(modifier = Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Code Editor
                    val isDarkMode by viewModel.isDarkMode.collectAsState()
                    val cursorLineOffset = cursorLine - 1
                    
                    Box(modifier = Modifier.weight(1f)) {
                        TextField(
                            value = textValue,
                            onValueChange = { viewModel.onTextChange(it) },
                            readOnly = isReadOnly,
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (!isWordWrapEnabled) Modifier.horizontalScroll(rememberScrollState()) else Modifier),
                            placeholder = { Text("Start typing...") },
                            visualTransformation = SyntaxTransformation(fileName = currentFileName, searchQuery = searchQuery),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    }
}

