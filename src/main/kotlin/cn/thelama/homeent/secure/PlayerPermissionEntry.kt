package cn.thelama.homeent.secure

data class PlayerPermissionEntry(var encryptedPassword: String, var permissionLevel: Int = 0)
