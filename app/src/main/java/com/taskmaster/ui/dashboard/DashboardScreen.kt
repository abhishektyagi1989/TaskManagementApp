package com.taskmaster.ui.dashboard

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.taskmaster.common.UiEvent
import com.taskmaster.domain.model.Task
import com.taskmaster.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToCreateTask: () -> Unit,
    onNavigateToEditTask: (Long) -> Unit,
    onNavigateToTaskDetails: (Long) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showDeleteDialog by remember { mutableStateOf(false) }
    var taskToDeleteId by remember { mutableLongStateOf(0L) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    // Collect UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.Navigate -> {
                    if (event.route == "login") {
                        onNavigateToLogin()
                    }
                }
                is UiEvent.ShowSnackbar -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Task Management", 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (state.userEmail.isNotEmpty()) {
                            Text(state.userEmail, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                },
                actions = {
                    // Logout button
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Log Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        // Handle Orientation: Landscape vs Portrait
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Left Panel: Stats and search/filters
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard(
                            title = "Total",
                            value = state.totalTasksCount.toString(),
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pending",
                            value = state.pendingTasksCount.toString(),
                            containerColor = Color(0xFFFFF3E0),
                            contentColor = Color(0xFFE65100),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Completed",
                            value = state.completedTasksCount.toString(),
                            containerColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF1B5E20),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SearchAndFilterControls(
                        searchQuery = state.searchQuery,
                        onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                        statusFilter = state.statusFilter,
                        onFilterChanged = { viewModel.onFilterChanged(it) },
                        sortOption = state.sortOption,
                        onSortChanged = { viewModel.onSortOptionChanged(it) },
                        sortMenuExpanded = sortMenuExpanded,
                        onSortMenuExpandChange = { sortMenuExpanded = it }
                    )
                }

                Divider(modifier = Modifier.fillMaxHeight().width(1.dp))

                // Right Panel: Task list
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    TaskListSection(
                        state = state,
                        onCheckedChange = { viewModel.toggleTaskComplete(it) },
                        onTaskClick = { onNavigateToTaskDetails(it.id) },
                        onDeleteClick = {
                            taskToDeleteId = it
                            showDeleteDialog = true
                        },
                        onRetry = { viewModel.refreshTasks() }
                    )
                }
            }
        } else {
            // Portrait layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                // Statistics Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Total Tasks",
                        value = state.totalTasksCount.toString(),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending",
                        value = state.pendingTasksCount.toString(),
                        containerColor = Color(0xFFFFF3E0),
                        contentColor = Color(0xFFE65100),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Completed",
                        value = state.completedTasksCount.toString(),
                        containerColor = Color(0xFFE8F5E9),
                        contentColor = Color(0xFF1B5E20),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Search & Filter controls
                SearchAndFilterControls(
                    searchQuery = state.searchQuery,
                    onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                    statusFilter = state.statusFilter,
                    onFilterChanged = { viewModel.onFilterChanged(it) },
                    sortOption = state.sortOption,
                    onSortChanged = { viewModel.onSortOptionChanged(it) },
                    sortMenuExpanded = sortMenuExpanded,
                    onSortMenuExpandChange = { sortMenuExpanded = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Task list section
                Box(modifier = Modifier.weight(1f)) {
                    TaskListSection(
                        state = state,
                        onCheckedChange = { viewModel.toggleTaskComplete(it) },
                        onTaskClick = { onNavigateToTaskDetails(it.id) },
                        onDeleteClick = {
                            taskToDeleteId = it
                            showDeleteDialog = true
                        },
                        onRetry = { viewModel.refreshTasks() }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    ConfirmationDialog(
        show = showDeleteDialog,
        title = "Delete Task",
        message = "Are you sure you want to delete this task? This action cannot be undone.",
        confirmText = "Delete",
        onConfirm = { viewModel.deleteTask(taskToDeleteId) },
        onDismiss = { showDeleteDialog = false }
    )
}

@Composable
fun StatCard(
    title: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 11.sp, color = contentColor.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = contentColor)
        }
    }
}

@Composable
fun SearchAndFilterControls(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    statusFilter: String,
    onFilterChanged: (String) -> Unit,
    sortOption: SortOption,
    onSortChanged: (SortOption) -> Unit,
    sortMenuExpanded: Boolean,
    onSortMenuExpandChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = { Text("Search by task name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Filter chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf("All", "Pending", "Completed")
                filters.forEach { filter ->
                    FilterChip(
                        selected = statusFilter == filter,
                        onClick = { onFilterChanged(filter) },
                        label = { Text(filter, fontSize = 12.sp) }
                    )
                }
            }

            // Sort Option Trigger
            Box {
                IconButton(onClick = { onSortMenuExpandChange(true) }) {
                    Icon(Icons.Default.Sort, contentDescription = "Sort Options")
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { onSortMenuExpandChange(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text("Due Date: Ascending") },
                        onClick = {
                            onSortChanged(SortOption.DUE_DATE_ASC)
                            onSortMenuExpandChange(false)
                        },
                        leadingIcon = {
                            if (sortOption == SortOption.DUE_DATE_ASC) {
                                Icon(Icons.Default.Check, contentDescription = "Selected")
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Due Date: Descending") },
                        onClick = {
                            onSortChanged(SortOption.DUE_DATE_DESC)
                            onSortMenuExpandChange(false)
                        },
                        leadingIcon = {
                            if (sortOption == SortOption.DUE_DATE_DESC) {
                                Icon(Icons.Default.Check, contentDescription = "Selected")
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Priority: Highest") },
                        onClick = {
                            onSortChanged(SortOption.PRIORITY)
                            onSortMenuExpandChange(false)
                        },
                        leadingIcon = {
                            if (sortOption == SortOption.PRIORITY) {
                                Icon(Icons.Default.Check, contentDescription = "Selected")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskListSection(
    state: DashboardUiState,
    onCheckedChange: (Task) -> Unit,
    onTaskClick: (Task) -> Unit,
    onDeleteClick: (Long) -> Unit,
    onRetry: () -> Unit
) {
    val brush = ShimmerBrush()

    when {
        state.isLoading -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(5) {
                    LoadingSkeletonItem(brush = brush)
                }
            }
        }
        state.errorMessage != null && state.tasks.isEmpty() -> {
            ErrorState(
                message = state.errorMessage,
                onRetry = onRetry
            )
        }
        state.tasks.isEmpty() -> {
            EmptyState()
        }
        else -> {
            // Note: Since we are not using SwipeRefresh dependencies which might clash with compile SDK 36,
            // we use custom vertical scroll lists or simple LazyColumn. A manual refresh button in TopBar is provided for convenience.
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(state.tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onCheckedChange = { onCheckedChange(task) },
                        onClick = { onTaskClick(task) },
                        onDelete = { onDeleteClick(task.id) }
                    )
                }
            }
        }
    }
}
