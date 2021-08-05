package cn.thelama.homeent.autoupdate.github.commits

data class Commit(
    @field: JvmField
    val author: Author,
    @field: JvmField
    val commentCount: Int,
    @field: JvmField
    val committer: Committer,
    @field: JvmField
    val message: String,
    @field: JvmField
    val tree: Tree,
    @field: JvmField
    val url: String,
    @field: JvmField
    val verification: Verification
)