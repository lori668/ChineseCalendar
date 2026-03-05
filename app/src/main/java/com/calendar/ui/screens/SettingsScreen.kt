package com.calendar.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.calendar.ui.theme.ChineseCalendarTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*

/**
 * 设置屏幕
 */
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var darkModeEnabled by remember { mutableStateOf(false) }
    var followSystem by remember { mutableStateOf(true) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "设置",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // 外观设置
        SettingsSection(title = "外观") {
            SettingsItem(
                icon = Icons.Default.Brightness4,
                title = "暗黑模式",
                subtitle = if (followSystem) "跟随系统" else if (darkModeEnabled) "已开启" else "已关闭",
                onClick = { }
            ) {
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it },
                    enabled = !followSystem
                )
            }

            SettingsItem(
                icon = Icons.Default.PhoneAndroid,
                title = "跟随系统",
                subtitle = "自动匹配系统主题",
                onClick = {
                    followSystem = !followSystem
                    if (followSystem) {
                        darkModeEnabled = false
                    }
                }
            ) {
                Switch(
                    checked = followSystem,
                    onCheckedChange = { followSystem = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 数据管理
        SettingsSection(title = "数据管理") {
            SettingsItem(
                icon = Icons.Default.Backup,
                title = "备份数据",
                subtitle = "将所有数据导出到本地存储",
                onClick = { showBackupDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Restore,
                title = "恢复数据",
                subtitle = "从备份文件恢复数据",
                onClick = { showRestoreDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 通知设置
        SettingsSection(title = "通知") {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "生日提醒",
                subtitle = "启用生日到期提醒",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Default.Task,
                title = "任务提醒",
                subtitle = "启用任务到期提醒",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 关于
        SettingsSection(title = "关于") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "使用说明",
                subtitle = "查看如何使用本应用",
                onClick = { showHelpDialog = true }
            )

            SettingsItem(
                icon = Icons.Default.Version,
                title = "版本信息",
                subtitle = "版本 1.0.0",
                onClick = { }
            )
        }
    }

    // 备份对话框
    if (showBackupDialog) {
        BackupDialog(
            onDismiss = { showBackupDialog = false },
            onSuccess = {
                Toast.makeText(context, "备份成功!", Toast.LENGTH_SHORT).show()
                showBackupDialog = false
            }
        )
    }

    // 恢复对话框
    if (showRestoreDialog) {
        RestoreDialog(
            onDismiss = { showRestoreDialog = false },
            onSuccess = {
                Toast.makeText(context, "恢复成功!", Toast.LENGTH_SHORT).show()
                showRestoreDialog = false
            }
        )
    }

    // 帮助对话框
    if (showHelpDialog) {
        HelpDialog(
            onDismiss = { showHelpDialog = false }
        )
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailingContent()
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
fun BackupDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isBackingUp by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    // 存储权限请求
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            performBackup(context,
                onProgress = { message = it },
                onSuccess = onSuccess,
                onFailure = { error ->
                    message = "备份失败：$error"
                    isBackingUp = false
                }
            )
        } else {
            message = "需要存储权限才能备份数据"
            isBackingUp = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("备份数据") },
        text = {
            Column {
                Text("将所有数据（生日、任务）备份到手机本地存储。")
                Spacer(modifier = Modifier.height(8.dp))
                if (message.isNotEmpty()) {
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
                if (isBackingUp) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isBackingUp = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Android 13+ 不需要存储权限
                        performBackup(context,
                            onProgress = { message = it },
                            onSuccess = onSuccess,
                            onFailure = { error ->
                                message = "备份失败：$error"
                                isBackingUp = false
                            }
                        )
                    } else {
                        permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                },
                enabled = !isBackingUp
            ) {
                Text(if (isBackingUp) "备份中..." else "开始备份")
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
fun RestoreDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var isRestoring by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var backupFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    // 加载备份文件
    LaunchedEffect(Unit) {
        backupFiles = getBackupFiles(context)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            backupFiles = getBackupFiles(context)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("恢复数据") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("从备份文件恢复数据到应用。")
                Spacer(modifier = Modifier.height(16.dp))

                if (backupFiles.isEmpty()) {
                    Text(
                        "未找到备份文件。请先进行备份。",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    backupFiles.forEach { file ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedFile == file)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFile = file }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = formatFileSize(file.length()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (selectedFile == file) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedFile != null) {
                        isRestoring = true
                        performRestore(context, selectedFile!!,
                            onSuccess = onSuccess,
                            onFailure = { error ->
                                message = "恢复失败：$error"
                                isRestoring = false
                            }
                        )
                    }
                },
                enabled = selectedFile != null && !isRestoring
            ) {
                Text(if (isRestoring) "恢复中..." else "恢复")
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
fun HelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("使用说明") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HelpSection(
                    title = "日历功能",
                    content = """
                    - 支持年/月/周/日四种视图切换
                    - 显示公历、农历、24 节气
                    - 标注法定节假日和调休
                    - 查看每日黄历详情（宜忌、冲煞等）
                    - 点击日期可查看详情
                    """.trimIndent()
                )

                HelpSection(
                    title = "生日管理",
                    content = """
                    - 添加/编辑/删除家人朋友生日
                    - 支持公历和农历生日
                    - 自定义提前提醒时间（1/3/7 天）
                    - 按最近 7 天、本月、全部分类显示
                    - 长按日期可快速添加生日
                    """.trimIndent()
                )

                HelpSection(
                    title = "任务管理",
                    content = """
                    - 添加/编辑/删除个人任务
                    - 设置任务优先级（高/中/低）
                    - 支持重复任务（每日/每周/每月/每年）
                    - 自定义提醒时间
                    - 按待完成、逾期、已完成分类
                    """.trimIndent()
                )

                HelpSection(
                    title = "数据备份",
                    content = """
                    - 备份：将所有数据导出到本地存储
                    - 恢复：从备份文件恢复数据
                    - 备份文件格式：JSON
                    - 建议定期备份以防数据丢失
                    """.trimIndent()
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

@Composable
fun HelpSection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// 备份功能实现
fun performBackup(
    context: Context,
    onProgress: (String) -> Unit,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        onProgress("正在准备备份...")

        val database = com.calendar.data.AppDatabase.getDatabase(context)
        val birthdays = database.birthdayDao().getAllBirthdaysSync()
        val tasks = database.taskDao().getAllTasksSync()

        onProgress("正在导出数据...")

        val backupData = mapOf(
            "birthdays" to birthdays,
            "tasks" to tasks,
            "backupTime" to System.currentTimeMillis()
        )

        val gson = Gson()
        val json = gson.toJson(backupData)

        // 创建备份目录
        val backupDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "万年历备份"
        )
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }

        // 生成文件名
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val fileName = "calendar_backup_${dateFormat.format(Date())}.json"
        val backupFile = File(backupDir, fileName)

        onProgress("正在写入文件...")

        // 写入文件
        FileWriter(backupFile).use { writer ->
            writer.write(json)
        }

        onProgress("备份完成：${backupFile.absolutePath}")
        onSuccess()

    } catch (e: Exception) {
        onFailure(e.message ?: "未知错误")
    }
}

// 恢复功能实现
fun performRestore(
    context: Context,
    backupFile: File,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    try {
        // 读取备份文件
        val gson = Gson()
        val json = FileReader(backupFile).use { reader ->
            reader.readText()
        }

        val type = object : TypeToken<Map<String, Any>>() {}.type
        val backupData: Map<String, Any> = gson.fromJson(json, type)

        val database = com.calendar.data.AppDatabase.getDatabase(context)

        // 恢复生日数据
        val birthdaysType = object : TypeToken<List<com.calendar.data.entities.Birthday>>() {}.type
        val birthdays: List<com.calendar.data.entities.Birthday> = gson.fromJson(
            backupData["birthdays"].toString(),
            birthdaysType
        )

        birthdays.forEach { birthday ->
            database.birthdayDao().insertBirthday(birthday)
        }

        // 恢复任务数据
        val tasksType = object : TypeToken<List<com.calendar.data.entities.Task>>() {}.type
        val tasks: List<com.calendar.data.entities.Task> = gson.fromJson(
            backupData["tasks"].toString(),
            tasksType
        )

        tasks.forEach { task ->
            database.taskDao().insertTask(task)
        }

        onSuccess()

    } catch (e: Exception) {
        onFailure(e.message ?: "未知错误")
    }
}

// 获取备份文件列表
fun getBackupFiles(context: Context): List<File> {
    val backupDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        "万年历备份"
    )

    if (!backupDir.exists()) {
        return emptyList()
    }

    return backupDir.listFiles { file ->
        file.name.endsWith(".json")
    }?.sortedByDescending { it.lastModified() } ?: emptyList()
}

// 格式化文件大小
fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        else -> String.format("%.2f MB", size / (1024.0 * 1024.0))
    }
}
