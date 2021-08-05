package cn.thelama.homeent.autoupdate.github.commits

data class Verification(
    @field: JvmField
    val payload: String,
    @field: JvmField
    val reason: String,
    @field: JvmField
    val signature: String,
    @field: JvmField
    val verified: Boolean
)