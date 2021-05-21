package cn.thelama.homeent.report

import java.util.*

data class ReportEntry(var type: String, var msg: String, var reportPlayer: UUID, var isOpen: Boolean, var reply: String?, var replyMaintainer: UUID?, var replyState: String)