package cn.thelama.homeent.module

import java.util.*

interface PlayerDataProvider <T> {
    fun config(uuid: UUID): T
}