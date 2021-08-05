package cn.thelama.homeent.autoupdate.github.commits

data class File(
    @field: JvmField
    val additions: Int,
    @field: JvmField
    val blobUrl: String,
    @field: JvmField
    val changes: Int,
    @field: JvmField
    val contentsUrl: String,
    @field: JvmField
    val deletions: Int,
    @field: JvmField
    val filename: String,
    @field: JvmField
    val patch: String,
    @field: JvmField
    val rawUrl: String,
    @field: JvmField
    val sha: String,
    @field: JvmField
    val status: String
)