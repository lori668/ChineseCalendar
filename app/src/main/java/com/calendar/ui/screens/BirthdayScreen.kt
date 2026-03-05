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
import androidx.compose.ui.unit.dp
import com.calendar.data.entities.Birthday
import java.text.SimpleDateFormat
import java.util.*

/**
 * 生日管理屏幕
 */
@Composable
fun BirthdayScreen(
    viewModel: com.calendar.ui.viewmodels.BirthdayViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val allBirthdays by viewModel.allBirthdays.collectAsState()
    val searchedBirthdays by viewModel.searchedBirthdays.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedBirthday by remember { mutableStateOf<Birthday?>(null) }

    // 更新搜索结果
    LaunchedEffect(searchQuery) {
        viewModel.search(searchQuery)
    }

    val displayedBirthdays = if (searchQuery.isNotBlank()) searchedBirthdays else allBirthdays

    // 按类别分组
    val upcomingBirthdays = displayedBirthdays.filter {
        viewModel.getDaysUntilBirthday(it) <= 7
    }
    val thisMonthBirthdays = displayedBirthdays.filter {
        it.birthMonth == Calendar.getInstance().get(Calendar.MONTH) + 1
    }
    val otherBirthdays = displayedBirthdays.filter { birthday ->
        birthday !in upcomingBirthdays && birthday !in thisMonthBirthdays
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
                placeholder = { Text("搜索姓名...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "搜索")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 最近 7 天生日
                if (upcomingBirthdays.isNotEmpty()) {
                    item {
                        SectionHeader(title = "最近 7 天生日")
                    }
                    items(upcomingBirthdays) { birthday ->
                        BirthdayCard(
                            birthday = birthday,
                            daysUntil = viewModel.getDaysUntilBirthday(birthday),
                            onEdit = { selectedBirthday = it },
                            onDelete = { viewModel.deleteBirthday(it.id) }
                        )
                    }
                }

                // 本月生日
                if (thisMonthBirthdays.isNotEmpty()) {
                    item {
                        SectionHeader(title = "本月生日")
                    }
                    items(thisMonthBirthdays) { birthday ->
                        BirthdayCard(
                            birthday = birthday,
                            daysUntil = viewModel.getDaysUntilBirthday(birthday),
                            onEdit = { selectedBirthday = it },
                            onDelete = { viewModel.deleteBirthday(it.id) }
                        )
                    }
                }

                // 全部生日
                if (otherBirthdays.isNotEmpty()) {
                    item {
                        SectionHeader(title = "全部生日")
                    }
                    items(otherBirthdays) { birthday ->
                        BirthdayCard(
                            birthday = birthday,
                            daysUntil = viewModel.getDaysUntilBirthday(birthday),
                            onEdit = { selectedBirthday = it },
                            onDelete = { viewModel.deleteBirthday(it.id) }
                        )
                    }
                }

                // 空状态
                if (displayedBirthdays.isEmpty()) {
                    item {
                        EmptyState(
                            message = "暂无生日记录",
                            actionText = "添加第一个生日",
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
            Icon(Icons.Default.Add, contentDescription = "添加生日")
        }
    }

    // 添加/编辑对话框
    if (showAddDialog || selectedBirthday != null) {
        BirthdayEditDialog(
            birthday = selectedBirthday,
            onDismiss = {
                showAddDialog = false
                selectedBirthday = null
            },
            onSave = { birthday ->
                if (selectedBirthday == null) {
                    viewModel.addBirthday(birthday)
                } else {
                    viewModel.updateBirthday(birthday.copy(id = selectedBirthday!!.id))
                }
                showAddDialog = false
                selectedBirthday = null
            }
        )
    }
}

@Composable
fun SectionHeader(
    title: String
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun BirthdayCard(
    birthday: Birthday,
    daysUntil: Int,
    onEdit: (Birthday) -> Unit,
    onDelete: (Birthday) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = birthday.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(
                        onClick = { },
                        label = { Text(birthday.relation) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${if (birthday.birthdayType == "lunar") "农历" else "公历"} ${birthday.birthMonth}月${birthday.birthDay}日",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "生肖：${birthday.getShengXiao()}",
                    style = MaterialTheme.typography.bodySmall
                )

                if (birthday.remark.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = birthday.remark,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (daysUntil <= 0) {
                    Text(
                        text = "已过",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (daysUntil == 0) {
                    Text(
                        text = "今天!",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "还有$daysUntil 天",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    IconButton(onClick = { onEdit(birthday) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    IconButton(onClick = { onDelete(birthday) }) {
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
}

@Composable
fun BirthdayEditDialog(
    birthday: Birthday?,
    onDismiss: () -> Unit,
    onSave: (Birthday) -> Unit
) {
    var name by remember { mutableStateOf(birthday?.name ?: "") }
    var relation by remember { mutableStateOf(birthday?.relation ?: "朋友") }
    var birthdayType by remember { mutableStateOf(birthday?.birthdayType ?: "solar") }
    var year by remember { mutableStateOf(birthday?.birthYear ?: 2000) }
    var month by remember { mutableStateOf(birthday?.birthMonth ?: 1) }
    var day by remember { mutableStateOf(birthday?.birthDay ?: 1) }
    var remark by remember { mutableStateOf(birthday?.remark ?: "") }
    var reminderEnabled by remember { mutableStateOf(birthday?.reminderEnabled ?: true) }
    var reminderDays by remember { mutableStateOf(birthday?.reminderDaysBefore ?: 1) }

    val relations = listOf("父母", "配偶", "子女", "兄弟姐妹", "朋友", "同事", "其他")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (birthday == null) "添加生日" else "编辑生日") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名 *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 关系选择
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { }
                ) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        relations.forEach { rel ->
                            DropdownMenuItem(
                                text = { Text(rel) },
                                onClick = {
                                    relation = rel
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // 生日类型
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterChip(
                        selected = birthdayType == "solar",
                        onClick = { birthdayType = "solar" },
                        label = { Text("公历") }
                    )
                    FilterChip(
                        selected = birthdayType == "lunar",
                        onClick = { birthdayType = "lunar" },
                        label = { Text("农历") }
                    )
                }

                // 日期选择
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = year.toString(),
                        onValueChange = { year = it.toIntOrNull() ?: year },
                        label = { Text("年") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = month.toString(),
                        onValueChange = { month = it.toIntOrNull()?.coerceIn(1, 12) ?: month },
                        label = { Text("月") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = day.toString(),
                        onValueChange = { day = it.toIntOrNull()?.coerceIn(1, 31) ?: day },
                        label = { Text("日") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = remark,
                    onValueChange = { remark = it },
                    label = { Text("备注 (礼物、联系方式等)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

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
                            selected = reminderDays == 1,
                            onClick = { reminderDays = 1 },
                            label = { Text("提前 1 天") }
                        )
                        FilterChip(
                            selected = reminderDays == 3,
                            onClick = { reminderDays = 3 },
                            label = { Text("提前 3 天") }
                        )
                        FilterChip(
                            selected = reminderDays == 7,
                            onClick = { reminderDays = 7 },
                            label = { Text("提前 7 天") }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newBirthday = Birthday(
                        id = birthday?.id ?: 0,
                        name = name,
                        relation = relation,
                        birthdayType = birthdayType,
                        birthYear = year,
                        birthMonth = month,
                        birthDay = day,
                        remark = remark,
                        reminderEnabled = reminderEnabled,
                        reminderDaysBefore = reminderDays,
                        reminderTime = "09:00"
                    )
                    onSave(newBirthday)
                },
                enabled = name.isNotBlank()
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

@Composable
fun EmptyState(
    message: String,
    actionText: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Cake,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onAction) {
            Text(actionText)
        }
    }
}
