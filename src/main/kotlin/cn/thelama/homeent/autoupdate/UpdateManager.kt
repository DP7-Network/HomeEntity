package cn.thelama.homeent.autoupdate

import cn.thelama.homeent.HomeEntity
import cn.thelama.homeent.autoupdate.github.releases.GithubLatestReleases
import com.google.common.base.Preconditions
import com.google.gson.Gson
import com.google.gson.JsonElement
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils
import org.bukkit.scheduler.BukkitTask
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Not tested!!
 * TODO: Test this class
 */
object UpdateManager {
    private val UA = listOf("Mozilla/5.0 (Charmless Server II; CharmlessServerII64; x64) Java11 & Kotlin 1.5 Repo(DP7-Network/HomeEntity) ForAutoUpdateOnly; <3 Github")
    private val GSON = Gson()

    private lateinit var updateTask: BukkitTask

    fun launchAsyncUpdateChecker() {
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(HomeEntity.instance, Runnable {
            if(checkUpdateSync() && updateSync()) {
                scheduleShutdown("可用的插件更新已下载成功, 执行重启更新", "HomeEntity",shutdown = false)
            }
        }, 0, 60 * 60 * 20)
    }

    fun stopAsyncUpdateChecker() {
        updateTask.cancel()
    }

    fun checkerStatus(): Boolean = !updateTask.isCancelled

    fun checkUpdateSync(stream: String = HomeEntity.REPO): Boolean {
        println("尝试更新中...")
        val urlString = "${HomeEntity.GITHUB_API_URL}/repos/$stream/"
        println("Http请求 GET $urlString 获取最后一次提交hash...")
        val req = URL(urlString).openConnection() as HttpsURLConnection
        req.requestProperties["User-Agent"] = UA

        if(req.responseCode == 200) {
            val rootObj = GSON.fromJson(InputStreamReader(req.inputStream), JsonElement::class.java).asJsonObject
            return if(rootObj["sha"].asString != HomeEntity.COMMIT_HASH) {
                println("当前提交: ${HomeEntity.COMMIT_HASH.substring(0..8)} 与远程提交: ${rootObj["sha"].asString.substring(0..8)} 不符!")
                true
            } else {
                false
            }
        } else {
            println("${ChatColor.RED}请求更新检查失败! 服务器返回了错误码: ${req.responseCode}")
        }
        return false
    }

    fun updateSync(stream: String = HomeEntity.REPO): Boolean {
        val urlString = "${HomeEntity.GITHUB_API_URL}/repos/$stream/releases/latest"
        println("Http请求 GET $urlString 获取最新版本下载地址...")
        val req = URL(urlString).openConnection() as HttpsURLConnection
        req.requestProperties["User-Agent"] = UA

        if(req.responseCode == 200) {
            val obj = GSON.fromJson(InputStreamReader(req.inputStream), GithubLatestReleases::class.java)

            val updateFolder = File(HomeEntity.instance.dataFolder, "../update")
            if(!updateFolder.exists()) {
                updateFolder.mkdir()
            }

            obj.assets.forEach {
                FileUtils.copyURLToFile(URL(it.browserDownloadUrl), File(updateFolder, it.name))
            }
            return true
        } else {
            println("${ChatColor.RED}请求更新地址失败! 服务器返回了错误码: ${req.responseCode}")
            return false
        }
    }

    fun scheduleShutdown(reason: String, operator: String, time: Int = 5, shutdown: Boolean = true) {
        Preconditions.checkArgument(time >= 1)

        val action = if(shutdown) { "关闭" } else { "重启" }

        Bukkit.broadcastMessage("${ChatColor.GOLD}!! 警告 !! 服务器即将在 ${time}分钟 后进行$action! 原因: $reason 操作者: $operator. 请尽快完成您手头的工作! ")

        for(t in time downTo 0) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(HomeEntity.instance, Runnable {
                Bukkit.broadcastMessage("${ChatColor.GOLD}!! 警告 !! 服务器即将在 ${t}分钟 后进行$action! 原因: $reason 操作者: $operator. 请尽快完成您手头的工作!")
                Bukkit.spigot().restart()
            }, t * 60 * 20L)
        }

        Bukkit.getScheduler().runTaskLaterAsynchronously(HomeEntity.instance, Runnable {
            for(t in 10 downTo 0) {
                Bukkit.broadcastMessage("${ChatColor.GOLD}!! 警告 !! 服务器即将在 ${t}秒后 后进行$action! 原因: $reason 操作者: $operator. 请尽快完成您手头的工作!")
                Thread.sleep(1000)
            }

            Bukkit.broadcastMessage("${ChatColor.GOLD}!! 警告 !! 服务器即将$action")

            if(shutdown) {
                Bukkit.getServer().shutdown()
            } else {
                Bukkit.spigot().restart()
            }
        }, time * 60 * 20L)
    }
}