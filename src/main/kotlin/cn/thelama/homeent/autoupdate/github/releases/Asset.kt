package cn.thelama.homeent.autoupdate.github.releases

data class Asset(
    @field: JvmField
    val browserDownloadUrl: String,
    @field: JvmField
    val contentType: String,
    @field: JvmField
    val createdAt: String,
    @field: JvmField
    val downloadCount: Int,
    @field: JvmField
    val id: Int,
    @field: JvmField
    val label: String,
    @field: JvmField
    val name: String,
    @field: JvmField
    val nodeId: String,
    @field: JvmField
    val size: Int,
    @field: JvmField
    val state: String,
    @field: JvmField
    val updatedAt: String,
    @field: JvmField
    val uploader: Uploader,
    @field: JvmField
    val url: String
)