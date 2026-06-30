package com.godapp.ggkeep.ui.screens.taskdetail

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.godapp.ggkeep.domain.model.KeepTask
import com.godapp.ggkeep.domain.model.ColorLevel
import com.godapp.ggkeep.ui.theme.color
import com.godapp.ggkeep.ui.theme.tint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: Long,
    triggerSms: Boolean,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    onConsumed: () -> Unit,
    onOpenCommands: () -> Unit = {},
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(triggerSms, state.task?.id) {
        if (triggerSms && state.task != null) {
            viewModel.onSmsTrigger()
        }
    }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onDeleted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("任务详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(taskId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                    IconButton(onClick = viewModel::showDeleteConfirm) {
                        Icon(Icons.Default.Delete, contentDescription = "删除")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.task == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "任务不存在",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) {
                            Text("返回列表")
                        }
                    }
                }
            }
            else -> {
                val task = state.task!!
                DetailContent(
                    task = task,
                    onConfirmConsume = viewModel::showDatePicker,
                    onSendSms = viewModel::onSmsTrigger,
                    onOpenCommands = onOpenCommands,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }

    // Dialogs
    state.task?.let { task ->
        if (state.showSmsConfirm && task.smsToNumber != null) {
            SmsConfirmDialog(
                phoneNumber = task.smsToNumber,
                onDismiss = viewModel::dismissSmsConfirm
            )
        }

        if (state.showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = viewModel::dismissDeleteConfirm,
                title = { Text("删除任务？") },
                text = { Text("确定删除「${task.name}」吗？此操作无法撤销。") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = viewModel::deleteTask,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) { Text("删除") }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = viewModel::dismissDeleteConfirm) {
                        Text("取消")
                    }
                }
            )
        }

        if (state.showDatePicker) {
            val lastDate = Instant.ofEpochMilli(task.lastConsumeDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    viewModel.confirmConsume(LocalDate.of(year, month + 1, day))
                },
                lastDate.year,
                lastDate.monthValue - 1,
                lastDate.dayOfMonth
            ).also { it.setOnDismissListener { viewModel.dismissDatePicker() } }.show()
        }
    }
}

@Composable
private fun DetailContent(
    task: KeepTask,
    onConfirmConsume: () -> Unit,
    onSendSms: () -> Unit,
    onOpenCommands: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = task.colorLevel.color()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = task.colorLevel.tint())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(task.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "${task.remainingDays}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        " / ${KeepTask.TOTAL_DAYS} 天",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (task.colorLevel) {
                        ColorLevel.EXPIRED -> "已过期，请立即消费保号！"
                        ColorLevel.CRITICAL -> "即将到期（7天内），请尽快消费"
                        ColorLevel.WARNING -> "即将到期（15天内），建议尽快消费"
                        ColorLevel.NORMAL -> "状态正常"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Info rows
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoRow(label = "手机号码", value = task.phoneNumber)
                InfoRow(
                    label = "最后消费",
                    value = Instant.ofEpochMilli(task.lastConsumeDate)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
                )
                InfoRow(label = "提醒时间", value = "${task.reminderHour}:00")
                InfoRow(label = "提前提醒", value = "${task.remindDaysBefore} 天")
                InfoRow(label = "短信接收", value = task.smsToNumber ?: "未设置")
            }
        }

        // Actions
        Button(
            onClick = onConfirmConsume,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("登记最后消费日期")
        }

        OutlinedButton(
            onClick = onOpenCommands,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("常用指令")
        }

        if (!task.smsToNumber.isNullOrBlank() && (task.isUrgent || task.isExpired)) {
            OutlinedButton(
                onClick = onSendSms,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = statusColor)
            ) {
                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("发送保号短信")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
