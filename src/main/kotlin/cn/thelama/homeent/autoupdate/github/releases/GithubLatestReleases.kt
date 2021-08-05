package cn.thelama.homeent.autoupdate.github.releases

data class GithubLatestReleases(
    @field: JvmField
    val assets: List<Asset>,
    @field: JvmField
    val assetsUrl: String,
    @field: JvmField
    val author: Author,
    @field: JvmField
    val body: String,
    @field: JvmField
    val createdAt: String,
    @field: JvmField
    val draft: Boolean,
    @field: JvmField
    val htmlUrl: String,
    @field: JvmField
    val id: Int,
    @field: JvmField
    val name: String,
    @field: JvmField
    val nodeId: String,
    @field: JvmField
    val prerelease: Boolean,
    @field: JvmField
    val publishedAt: String,
    @field: JvmField
    val tagName: String,
    @field: JvmField
    val tarballUrl: String,
    @field: JvmField
    val targetCommitish: String,
    @field: JvmField
    val uploadUrl: String,
    @field: JvmField
    val url: String,
    @field: JvmField
    val zipballUrl: String
)