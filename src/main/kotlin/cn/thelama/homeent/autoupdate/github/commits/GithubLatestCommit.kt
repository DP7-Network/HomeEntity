package cn.thelama.homeent.autoupdate.github.commits

data class GithubLatestCommit(
    @field: JvmField
    val author: Author,
    @field: JvmField
    val commentsUrl: String,
    @field: JvmField
    val commit: Commit,
    @field: JvmField
    val committer: Committer,
    @field: JvmField
    val files: List<File>,
    @field: JvmField
    val htmlUrl: String,
    @field: JvmField
    val nodeId: String,
    @field: JvmField
    val parents: List<Parent>,
    @field: JvmField
    val sha: String,
    @field: JvmField
    val stats: Stats,
    @field: JvmField
    val url: String
)