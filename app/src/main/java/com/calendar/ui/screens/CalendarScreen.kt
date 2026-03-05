package com.calendar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.calendar.ui.viewmodels.CalendarViewModel
import com.calendar.ui.theme.LunarText
import com.calendar.ui.theme.HolidayText

/**
 * 日历屏幕
 */
@Composable
fun CalendarScreen(viewModel: CalendarViewModel = viewModel()) {
    val currentDate by viewModel.currentDate.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val viewType by viewModel.viewType.collectAsState()
    val calendarDays by viewModel.calendarDays.collectAsState()
    val lunarDateInfo by viewModel.lunarDateInfo.collectAsState()
    val almanacInfo by viewModel.almanacInfo.collectAsState()
    val solarTerm by viewModel.solarTerm.collectAsState()
    val holiday by viewModel.holiday.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 头部：日期导航
        CalendarHeader(
            currentDate = viewModel.getFormattedDate(),
            viewType = viewType,
            onNavigatePrevious = { viewModel.navigateToPrevious() },
            onNavigateNext = { viewModel.navigateToNext() },
            onNavigateToday = { viewModel.navigateToToday() },
            onViewTypeChange = { viewModel.setViewType(it) },
            onDateSelected = { showDatePicker = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 根据视图类型显示不同的日历
        when (viewType) {
            CalendarViewModel.CalendarViewType.YEAR -> YearView(
                year = currentDate.get(java.util.Calendar.YEAR),
                onMonthSelected = { /* TODO */ }
            )
            CalendarViewModel.CalendarViewType.MONTH -> MonthView(
                days = calendarDays,
                onDaySelected = { day ->
                    viewModel.selectDate(day.date)
                }
            )
            CalendarViewModel.CalendarViewType.WEEK -> WeekView(
                days = calendarDays.take(7),
                onDaySelected = { day ->
                    viewModel.selectDate(day.date)
                }
            )
            CalendarViewModel.CalendarViewType.DAY -> DayDetailView(
                selectedDate = selectedDate,
                lunarDateInfo = lunarDateInfo,
                almanacInfo = almanacInfo,
                solarTerm = solarTerm,
                holiday = holiday
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 选中日期详情
        if (viewType != CalendarViewModel.CalendarViewType.DAY) {
            SelectedDateDetail(
                selectedDate = viewModel.getSelectedDateFormatted(),
                lunarDateInfo = lunarDateInfo,
                almanacInfo = almanacInfo,
                solarTerm = solarTerm,
                holiday = holiday
            )
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { year, month, day ->
                val calendar = java.util.Calendar.getInstance()
                calendar.set(year, month, day)
                viewModel.selectDate(calendar)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun CalendarHeader(
    currentDate: String,
    viewType: CalendarViewModel.CalendarViewType,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigateToday: () -> Unit,
    onViewTypeChange: (CalendarViewModel.CalendarViewType) -> Unit,
    onDateSelected: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigatePrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "上个月")
        }

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentDate,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onDateSelected)
            )
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = "选择日期",
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onDateSelected)
            )
        }

        IconButton(onClick = onNavigateNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "下个月")
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = viewType == CalendarViewModel.CalendarViewType.YEAR,
            onClick = { onViewTypeChange(CalendarViewModel.CalendarViewType.YEAR) },
            label = { Text("年") }
        )
        FilterChip(
            selected = viewType == CalendarViewModel.CalendarViewType.MONTH,
            onClick = { onViewTypeChange(CalendarViewModel.CalendarViewType.MONTH) },
            label = { Text("月") }
        )
        FilterChip(
            selected = viewType == CalendarViewModel.CalendarViewType.WEEK,
            onClick = { onViewTypeChange(CalendarViewModel.CalendarViewType.WEEK) },
            label = { Text("周") }
        )
        FilterChip(
            selected = viewType == CalendarViewModel.CalendarViewType.DAY,
            onClick = { onViewTypeChange(CalendarViewModel.CalendarViewType.DAY) },
            label = { Text("日") }
        )

        OutlinedButton(onClick = onNavigateToday) {
            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("今天")
        }
    }
}

@Composable
fun MonthView(
    days: List<CalendarViewModel.CalendarDay>,
    onDaySelected: (CalendarViewModel.CalendarDay) -> Unit
) {
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")

    Column {
        // 星期标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center,
                    color = if (day == "日" || day == "六") HolidayText else MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 日期网格
        for (i in 0 until 6) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (j in 0 until 7) {
                    val index = i * 7 + j
                    if (index < days.size) {
                        val day = days[index]
                        DayCell(
                            day = day,
                            onClick = { onDaySelected(day) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    day: CalendarViewModel.CalendarDay,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        day.isToday -> MaterialTheme.colorScheme.primaryContainer
        day.isSelected -> MaterialTheme.colorScheme.primary
        !day.isCurrentMonth -> MaterialTheme.colorScheme.surface
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        day.isToday || day.isSelected -> MaterialTheme.colorScheme.onPrimary
        !day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        day.isWeekend -> HolidayText
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .weight(1f)
            .padding(2.dp)
            .background(backgroundColor, shape = MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.date.get(java.util.Calendar.DAY_OF_MONTH).toString(),
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )

            // 农历或节气显示
            val lunarText = day.solarTerm ?: day.holiday ?: day.lunarInfo?.lunarDayStr
            if (lunarText != null) {
                Text(
                    text = lunarText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.solarTerm != null || day.holiday != null)
                        HolidayText else LunarText,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun YearView(
    year: Int,
    onMonthSelected: (Int) -> Unit
) {
    val months = listOf(
        "一月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "十一月", "十二月"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "$year 年",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        LazyVerticalGrid(
            columns = androidx.compose.foundation.layout.GridCells.Fixed(3),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(12) { index ->
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .aspectRatio(4f / 3f),
                    onClick = { onMonthSelected(index) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = months[index],
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeekView(
    days: List<CalendarViewModel.CalendarDay>,
    onDaySelected: (CalendarViewModel.CalendarDay) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        days.forEach { day ->
            DayCell(
                day = day,
                onClick = { onDaySelected(day) }
            )
        }
    }
}

@Composable
fun DayDetailView(
    selectedDate: java.util.Calendar,
    lunarDateInfo: com.calendar.domain.calendar.LunarDateInfo?,
    almanacInfo: com.calendar.domain.calendar.AlmanacInfo?,
    solarTerm: String?,
    holiday: com.calendar.domain.calendar.HolidayManager.Holiday?
) {
    val year = selectedDate.get(java.util.Calendar.YEAR)
    val month = selectedDate.get(java.util.Calendar.MONTH) + 1
    val day = selectedDate.get(java.util.Calendar.DAY_OF_MONTH)
    val weekDays = listOf("日", "一", "二", "三", "四", "五", "六")
    val weekDay = weekDays[selectedDate.get(java.util.Calendar.DAY_OF_WEEK) - 1]

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 公历日期
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$month.$day",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "星期$weekDay",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 农历信息
            if (lunarDateInfo != null) {
                Text(
                    text = lunarDateInfo.getFullString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = LunarText,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 节气
            if (solarTerm != null) {
                Text(
                    text = solarTerm,
                    style = MaterialTheme.typography.titleMedium,
                    color = HolidayText,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // 节假日
            if (holiday != null) {
                Text(
                    text = holiday.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = HolidayText,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 黄历信息
            if (almanacInfo != null) {
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "【黄历】${almanacInfo.ganZhiYear} ${almanacInfo.ganZhiMonth} ${almanacInfo.ganZhiDay}",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Text("宜：", fontWeight = FontWeight.Bold)
                    Text(
                        text = almanacInfo.yi.joinToString("、"),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Text("忌：", fontWeight = FontWeight.Bold)
                    Text(
                        text = almanacInfo.ji.joinToString("、"),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Text("冲煞：", fontWeight = FontWeight.Bold)
                    Text("${almanacInfo.chong} ${almanacInfo.sha}")
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row {
                    Text("胎神：", fontWeight = FontWeight.Bold)
                    Text(almanacInfo.taiShen)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "彭祖：${almanacInfo.pengZu}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SelectedDateDetail(
    selectedDate: String,
    lunarDateInfo: com.calendar.domain.calendar.LunarDateInfo?,
    almanacInfo: com.calendar.domain.calendar.AlmanacInfo?,
    solarTerm: String?,
    holiday: com.calendar.domain.calendar.HolidayManager.Holiday?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = selectedDate,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (lunarDateInfo != null) {
                Text(
                    text = lunarDateInfo.getFullString(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = LunarText
                )
            }

            if (solarTerm != null) {
                Text(
                    text = "节气：$solarTerm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HolidayText
                )
            }

            if (holiday != null) {
                Text(
                    text = "节日：${holiday.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = HolidayText
                )
            }

            if (almanacInfo != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "宜：${almanacInfo.yi.take(5).joinToString("、")}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "忌：${almanacInfo.ji.take(5).joinToString("、")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun DatePickerDialog(
    onDateSelected: (Int, Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = java.util.Calendar.getInstance()
    var year by remember { mutableStateOf(calendar.get(java.util.Calendar.YEAR)) }
    var month by remember { mutableStateOf(calendar.get(java.util.Calendar.MONTH)) }
    var day by remember { mutableStateOf(calendar.get(java.util.Calendar.DAY_OF_MONTH)) }

    android.app.DatePickerDialog(
        androidx.compose.ui.platform.LocalContext.current,
        { _, y, m, d -> onDateSelected(y, m, d) },
        year,
        month,
        day
    ).apply {
        setOnCancelListener { onDismiss() }
        show()
    }
}
