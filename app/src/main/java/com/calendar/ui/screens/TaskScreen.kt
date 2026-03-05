package com.calendar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.calendar.data.entities.Task
import java.text.SimpleDateFormat
import java.util.*

/**
 * 任务管理屏幕
 */
@Composable
fun TaskScreen(
    viewModel: com.calendar.ui.viewmodels.TaskViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val allTasks by viewModel.allTasks.collectAsState()
    val pendingTasks by viewModel.pendingTasks.collectAsState()
    val completedTasks by viewModel.completedTasks.collectAsState()
    val overdueTasks by viewModel.overdueTasks.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("待完成", "逾期", "已完成", "全部")

    val displayedTasks = when (selectedTab) {
        0 -> pendingTasks
        1 -> overdueTasks
        2 -> completedTasks
        else -> allTasks
    }

    // 更新任务状态
    LaunchedEffect(Unit) {
        viewModel.updateTaskStatuses()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 搜索栏
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("搜索任务...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 分类 Tab
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(title)
                                val count = when (index) {
                                    0 -> pendingTasks.size
                                    1 -> overdueTasks.size
                                    2 -> completedTasks.size
                                    else -> allTasks.size
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge {
                                    Text(count.toString())
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 任务列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(displayedTasks) { task ->
                    TaskCard(
                        task = task,
                        onToggleComplete = {
                            if (task.status == Task.TaskStatus.COMPLETED) {
                                viewModel.uncompleteTask(task.id)
                            } else {
                                viewModel.completeTask(task.id)
                            }
                        },
                        onEdit = { selectedTask = it },
                        onDelete = { viewModel.deleteTask(it.id) }
                    )
                }

                if (displayedTasks.isEmpty()) {
                    item {
                        EmptyState(
                            message = "暂无任务",
                            actionText = "添加第一个任务",
                            onAction = { showAddDialog = true }
                        )
                    }
                }
            }
        }

        // 添加按钮
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加任务")
        }
    }

    // 添加/编辑对话框
    if (showAddDialog || selectedTask != null) {
        TaskEditDialog(
            task = selectedTask,
            onDismiss = {
                showAddDialog = false
                selectedTask = null
            },
            onSave = { task ->
                if (selectedTask == null) {
                    viewModel.addTask(task)
                } else {
                    viewModel.updateTask(task.copy(id = selectedTask!!.id))
                }
                showAddDialog = false
                selectedTask = null
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggleComplete: () -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (task.status) {
                Task.TaskStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer
                Task.TaskStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.status == Task.TaskStatus.COMPLETED,
                    onCheckedChange = { onToggleComplete() }
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (task.status == Task.TaskStatus.COMPLETED)
                                TextDecoration.LineThrough else null
                        )
                        Spacer(modifier = Modifier.width(8.dp))

                        // 优先级标记
                        AssistChip(
                            onClick = { },
                            label = { Text(task.getPriorityDesc()) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = when (task.priority) {
                                    Task.Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                    Task.Priority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                                    Task.Priority.LOW -> MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (task.description.isNotEmpty()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${task.deadlineMonth}/${task.deadlineDay} ${task.deadlineHour}:${task.deadlineMinute.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (task.status == Task.TaskStatus.OVERDUE)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (task.category.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Folder,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.category,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (task.repeatType != Task.RepeatType.NONE) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.getRepeatTypeDesc(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Row {
                IconButton(onClick = { onEdit(task) }) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                IconButton(onClick = { onDelete(task) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun TaskEditDialog(
    task: Task?,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var category by remember { mutableStateOf(task?.category ?: "") }
    var priority by remember { mutableStateOf(task?.priority ?: Task.Priority.MEDIUM) }

    var deadlineYear by remember { mutableStateOf(task?.deadlineYear ?: 2024) }
    var deadlineMonth by remember { mutableStateOf(task?.deadlineMonth ?: 1) }
    var deadlineDay by remember { mutableStateOf(task?.deadlineDay ?: 1) }
    var deadlineHour by remember { mutableStateOf(task?.deadlineHour ?: 23) }
    var deadlineMinute by remember { mutableStateOf(task?.deadlineMinute ?: 59) }

    var repeatType by remember { mutableStateOf(task?.repeatType ?: Task.RepeatType.NONE) }
    var reminderEnabled by remember { mutableStateOf(task?.reminderEnabled ?: true) }
    var reminderMinutes by remember { mutableStateOf(task?.reminderMinutesBefore ?: 60) }

    val categories = listOf("工作", "生活", "学习", "健康", "财务", "其他")
    val priorities = listOf(Task.Priority.LOW, Task.Priority.MEDIUM, Task.Priority.HIGH)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (task == null) "添加任务" else "编辑任务") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("任务标题 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("任务描述") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // 分类选择
                Text("分类", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                // 优先级选择
                Text("优先级", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    priorities.forEach { prio ->
                        FilterChip(
                            selected = priority == prio,
                            onClick = { priority = prio },
                            label = { Text(when (prio) {
                                Task.Priority.LOW -> "低"
                                Task.Priority.MEDIUM -> "中"
                                Task.Priority.HIGH -> "高"
                            }) }
                        )
                    }
                }

                // 截止时间
                Text("截止时间", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deadlineYear.toString(),
                        onValueChange = { deadlineYear = it.toIntOrNull() ?: deadlineYear },
                        label = { Text("年") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deadlineMonth.toString(),
                        onValueChange = { deadlineMonth = it.toIntOrNull()?.coerceIn(1, 12) ?: deadlineMonth },
                        label = { Text("月") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deadlineDay.toString(),
                        onValueChange = { deadlineDay = it.toIntOrNull()?.coerceIn(1, 31) ?: deadlineDay },
                        label = { Text("日") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = deadlineHour.toString().padStart(2, '0'),
                        onValueChange = { deadlineHour = it.toIntOrNull()?.coerceIn(0, 23) ?: deadlineHour },
                        label = { Text("时") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = deadlineMinute.toString().padStart(2, '0'),
                        onValueChange = { deadlineMinute = it.toIntOrNull()?.coerceIn(0, 59) ?: deadlineMinute },
                        label = { Text("分") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 重复规则
                Text("重复规则", style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = repeatType == Task.RepeatType.NONE,
                        onClick = { repeatType = Task.RepeatType.NONE },
                        label = { Text("不重复") }
                    )
                    FilterChip(
                        selected = repeatType == Task.RepeatType.DAILY,
                        onClick = { repeatType = Task.RepeatType.DAILY },
                        label = { Text("每日") }
                    )
                    FilterChip(
                        selected = repeatType == Task.RepeatType.WEEKLY,
                        onClick = { repeatType = Task.RepeatType.WEEKLY },
                        label = { Text("每周") }
                    )
                    FilterChip(
                        selected = repeatType == Task.RepeatType.MONTHLY,
                        onClick = { repeatType = Task.RepeatType.MONTHLY },
                        label = { Text("每月") }
                    )
                    FilterChip(
                        selected = repeatType == Task.RepeatType.YEARLY,
                        onClick = { repeatType = Task.RepeatType.YEARLY },
                        label = { Text("每年") }
                    )
                }

                // 提醒设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("启用提醒")
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }

                if (reminderEnabled) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = reminderMinutes == 30,
                            onClick = { reminderMinutes = 30 },
                            label = { Text("提前 30 分钟") }
                        )
                        FilterChip(
                            selected = reminderMinutes == 60,
                            onClick = { reminderMinutes = 60 },
                            label = { Text("提前 1 小时") }
                        )
                        FilterChip(
                            selected = reminderMinutes == 1440,
                            onClick = { reminderMinutes = 1440 },
                            label = { Text("提前 1 天") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newTask = Task(
                        id = task?.id ?: 0,
                        title = title,
                        description = description,
                        category = category,
                        priority = priority,
                        deadlineYear = deadlineYear,
                        deadlineMonth = deadlineMonth,
                        deadlineDay = deadlineDay,
                        deadlineHour = deadlineHour,
                        deadlineMinute = deadlineMinute,
                        repeatType = repeatType,
                        reminderEnabled = reminderEnabled,
                        reminderMinutesBefore = reminderMinutes
                    )
                    onSave(newTask)
                },
                enabled = title.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
