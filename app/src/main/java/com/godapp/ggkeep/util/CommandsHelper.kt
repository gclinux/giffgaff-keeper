package com.godapp.ggkeep.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * giffgaff 常用指令助手。
 *
 * - 打开官网：调用默认浏览器
 * - 查询 SIM 卡号码：发短信 NUMBER 到 2020
 * - 查询余额或消费记录：拨号 *100#
 * - 关闭语音信箱：拨号 ##002#
 *
 * 所有方法均使用系统 Intent，不会自动执行，会调起对应系统应用并预填内容，
 * 由用户最终确认发送/拨打。
 */
object CommandsHelper {

    private const val OFFICIAL_URL = "https://www.giffgaff.com/"
    private const val SIM_NUMBER_SHORTCODE = "2020"
    private const val SIM_NUMBER_COMMAND = "NUMBER"
    private const val USSD_BALANCE = "*100#"
    private const val USSD_DISABLE_VOICEMAIL = "##002#"

    /** 打开默认浏览器访问 giffgaff 官网。 */
    fun openOfficialWebsite(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OFFICIAL_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /** 调起系统短信应用，预填收件人 2020 和内容 NUMBER。 */
    fun sendQuerySimNumberSms(context: Context) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$SIM_NUMBER_SHORTCODE")
            putExtra("sms_body", SIM_NUMBER_COMMAND)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /** 调起系统拨号盘，预填 USSD 代码 *100#（查询余额/消费记录）。 */
    fun dialBalanceUssd(context: Context) {
        dialUssd(context, USSD_BALANCE)
    }

    /** 调起系统拨号盘，预填 USSD 代码 ##002#（关闭语音信箱）。 */
    fun disableVoicemail(context: Context) {
        dialUssd(context, USSD_DISABLE_VOICEMAIL)
    }

    /**
     * 通用 USSD 拨号方法。
     * 注意：# 字符在 URI 中必须编码为 %23，否则会被截断。
     */
    private fun dialUssd(context: Context, code: String) {
        val encoded = Uri.encode(code)
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$encoded")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /** 对外暴露的指令元数据，供 UI 显示。 */
    data class Command(
        val id: String,
        val title: String,
        val subtitle: String,
        val confirmMessage: String,
        val isImportant: Boolean = false
    )

    val commands: List<Command> = listOf(
        Command(
            id = "open_website",
            title = "打开官网",
            subtitle = "https://www.giffgaff.com/",
            confirmMessage = "你将打开网址：\nhttps://www.giffgaff.com/"
        ),
        Command(
            id = "query_sim_number",
            title = "查询 SIM 卡号码",
            subtitle = "发送 NUMBER 到 2020",
            confirmMessage = "你将发送短信：\nNUMBER\n到 2020"
        ),
        Command(
            id = "query_balance",
            title = "查询余额或消费记录",
            subtitle = "拨打 *100#",
            confirmMessage = "你将拨打 USSD 代码：\n*100#"
        ),
        Command(
            id = "disable_voicemail",
            title = "关闭语音信箱",
            subtitle = "拨打 ##002#",
            confirmMessage = "你将拨打 USSD 代码：\n##002#\n\n此操作会关闭所有呼叫转移（包括语音信箱）。",
            isImportant = true
        )
    )

    /** 根据指令 id 执行对应系统调用。 */
    fun execute(context: Context, commandId: String) {
        when (commandId) {
            "open_website" -> openOfficialWebsite(context)
            "query_sim_number" -> sendQuerySimNumberSms(context)
            "query_balance" -> dialBalanceUssd(context)
            "disable_voicemail" -> disableVoicemail(context)
        }
    }
}
