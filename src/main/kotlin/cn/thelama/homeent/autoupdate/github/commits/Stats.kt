package cn.thelama.homeent.autoupdate.github.commits

data class Stats(
    @field: JvmField
    val additions: Int,
    @field: JvmField
    val deletions: Int,
    @field: JvmField
    val total: Int
)