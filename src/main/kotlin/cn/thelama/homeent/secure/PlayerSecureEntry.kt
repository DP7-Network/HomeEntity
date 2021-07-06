package cn.thelama.homeent.secure

data class PlayerSecureEntry(var encryptedPassword: String, var permissionLevel: Int = 0)
