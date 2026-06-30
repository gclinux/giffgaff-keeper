package com.godapp.ggkeep.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.godapp.ggkeep.MainActivity
import com.godapp.ggkeep.di.KeepSimEntryPoint
import com.godapp.ggkeep.domain.model.KeepTask
import com.godapp.ggkeep.domain.model.ColorLevel
import dagger.hilt.android.EntryPointAccessors
import kotlin.math.ceil

val TASK_ID_KEY = ActionParameters.Key<Long>("task_id")
val TRIGGER_SMS_KEY = ActionParameters.Key<Boolean>("trigger_sms")
val MAX_PAGE_KEY = ActionParameters.Key<Int>("max_page")

private const val PREFS_NAME = "ggkeep_widget_prefs"
private const val KEY_CURRENT_PAGE = "current_page"
private const val PAGE_SIZE = 2  // 每页显示 2 个任务（适配 2 行高度）

// 现代简约配色（半透明，融入壁纸）
private val SurfaceLight = Color(0x80FFFFFF)  // 50% 白
private val SurfaceDark = Color(0x801A1A1A)   // 50% 深灰
private val TextPrimary = Color(0xF21F1F1F)
private val TextPrimaryDark = Color(0xF2EDEDED)
private val TextSecondary = Color(0xC08A8A8A)
private val TextSecondaryDark = Color(0xC0AAAAAA)
private val DividerLight = Color(0x14000000)
private val DividerDark = Color(0x14FFFFFF)

// 固定阈值颜色：过期=灰, ≤7天=红, ≤15天=橙, >15天=绿
private val StatusExpired = Color(0xFF9E9E9E)   // gray
private val StatusCritical = Color(0xFFE53935)  // red
private val StatusWarning = Color(0xFFFF9800)   // orange
private val StatusNormal = Color(0xFF4CAF50)    // green

object WidgetPrefs {
    fun getPage(context: Context): Int =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getInt(KEY_CURRENT_PAGE, 0)

    fun setPage(context: Context, page: Int) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putInt(KEY_CURRENT_PAGE, page).apply()
    }
}

class WidgetPrevPageAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val current = WidgetPrefs.getPage(context)
        WidgetPrefs.setPage(context, (current - 1).coerceAtLeast(0))
        KeepSimWidget().updateAll(context)
    }
}

class WidgetNextPageAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val maxPage = parameters.get(MAX_PAGE_KEY) ?: 0
        val current = WidgetPrefs.getPage(context)
        WidgetPrefs.setPage(context, (current + 1).coerceAtMost(maxPage))
        KeepSimWidget().updateAll(context)
    }
}

class KeepSimWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            KeepSimEntryPoint::class.java
        )
        val repository = entryPoint.keepTaskRepository()

        // 按剩余天数升序（越少越前面），过期的排最前
        val allTasks = repository.getAllTasksOnce().sortedBy { it.remainingDays }

        val totalPages = if (allTasks.isEmpty()) 1 else ceil(allTasks.size.toDouble() / PAGE_SIZE).toInt()
        val currentPage = WidgetPrefs.getPage(context).coerceIn(0, totalPages - 1)
        val pageTasks = allTasks.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)
        val hasPrev = currentPage > 0
        val hasNext = currentPage < totalPages - 1
        val showPager = allTasks.size > PAGE_SIZE

        provideContent {
            GlanceTheme {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(SurfaceLight, SurfaceDark))
                        .cornerRadius(16.dp)
                ) {
                    Row(
                        modifier = GlanceModifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalAlignment = Alignment.Start
                    ) {
                        // 任务列表区域
                        Column(
                            modifier = GlanceModifier.defaultWeight()
                        ) {
                            if (pageTasks.isEmpty()) {
                                Text(
                                    text = "暂无保号任务",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = ColorProvider(TextSecondary, TextSecondaryDark)
                                    )
                                )
                            } else {
                                pageTasks.forEachIndexed { index, task ->
                                    if (index > 0) {
                                        Spacer(modifier = GlanceModifier.height(3.dp))
                                        // 分隔线通过带背景的极薄 Spacer 模拟
                                        Spacer(
                                            modifier = GlanceModifier
                                                .fillMaxWidth()
                                                .height(1.dp)
                                                .background(ColorProvider(DividerLight, DividerDark))
                                        )
                                        Spacer(modifier = GlanceModifier.height(3.dp))
                                    }
                                    TaskWidgetItem(task, context)
                                }
                            }
                        }

                        // 右侧翻页按钮区（只显示一个，根据状态切换◀▶）
                        if (showPager) {
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (hasPrev) {
                                    PageButton(
                                        text = "‹",
                                        action = actionRunCallback<WidgetPrevPageAction>()
                                    )
                                } else if (hasNext) {
                                    PageButton(
                                        text = "›",
                                        action = actionRunCallback<WidgetNextPageAction>(
                                            parameters = actionParametersOf(MAX_PAGE_KEY to (totalPages - 1))
                                        )
                                    )
                                }
                                // 页码指示
                                Spacer(modifier = GlanceModifier.height(4.dp))
                                Text(
                                    text = "${currentPage + 1}/$totalPages",
                                    style = TextStyle(
                                        fontSize = 9.sp,
                                        color = ColorProvider(TextSecondary, TextSecondaryDark)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun PageButton(text: String, action: androidx.glance.action.Action) {
        Box(
            modifier = GlanceModifier
                .background(ColorProvider(Color(0x14000000), Color(0x22FFFFFF)))
                .cornerRadius(10.dp)
                .clickable(action)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(TextPrimary, TextPrimaryDark)
                )
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun TaskWidgetItem(task: KeepTask, context: Context) {
        val statusColor = when (task.colorLevel) {
            ColorLevel.EXPIRED  -> StatusExpired
            ColorLevel.CRITICAL -> StatusCritical
            ColorLevel.WARNING  -> StatusWarning
            ColorLevel.NORMAL   -> StatusNormal
        }

        val shouldTriggerSms = !task.smsToNumber.isNullOrBlank() && task.colorLevel != ColorLevel.NORMAL

        val launchIntent = Intent(context.applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(
                    actionStartActivity(
                        intent = launchIntent,
                        parameters = actionParametersOf(
                            TASK_ID_KEY to task.id,
                            TRIGGER_SMS_KEY to shouldTriggerSms
                        )
                    )
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧状态色条（极简竖条指示状态）
            Box(
                modifier = GlanceModifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(ColorProvider(statusColor, statusColor))
                    .cornerRadius(2.dp)
            ) {}
            Spacer(modifier = GlanceModifier.width(6.dp))

            // 中部：名称 + 号码
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    text = task.name,
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorProvider(TextPrimary, TextPrimaryDark)
                    )
                )
                Text(
                    text = task.phoneNumber,
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = ColorProvider(TextSecondary, TextSecondaryDark)
                    )
                )
            }

            // 右侧：剩余天数（突出显示）
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${task.remainingDays}",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(statusColor, statusColor),
                        textAlign = TextAlign.End
                    )
                )
                Text(
                    text = "天",
                    style = TextStyle(
                        fontSize = 9.sp,
                        color = ColorProvider(TextSecondary, TextSecondaryDark),
                        textAlign = TextAlign.End
                    )
                )
            }
        }
    }
}
