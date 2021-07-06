package cn.thelama.homeent.module

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*
import kotlin.collections.HashMap

/**
 * Not tested feature!
 * TODO: Test this feature
 */
object ModuledPlayerDataManager {
    private val gson = Gson()
    //                             Player UUID    |   Module Name | Module Player Data Json Tree
    private val playerRoot: MutableMap<UUID, MutableMap<String, JsonObject>> = hashMapOf()

    fun init(baseFolder: File) {
        if(!baseFolder.exists()) {
            baseFolder.mkdir()
        }
        val dataBaseFolder = File(baseFolder, "player/")
        if(!dataBaseFolder.exists()) {
            dataBaseFolder.mkdir()
        }
        dataBaseFolder.listFiles()?.forEach {
            runCatching {
                if(it.extension == "json") {
                    val uuid = UUID.fromString(it.name)
                    val rootObject = gson.fromJson<JsonObject>(FileReader(it), object: TypeToken<JsonObject>() {}.type)
                    val root = hashMapOf<String, JsonObject>()
                    rootObject.entrySet().forEach {
                        runCatching {
                            root[it.key] = it.value.asJsonObject
                        }
                    }
                    playerRoot[uuid] = root
                }
            }.onFailure { _ ->
                val nf = File(dataBaseFolder, "${it.name}.error")
                if(nf.exists()) {
                    it.renameTo(File("${nf.name}.error"))
                } else {
                    it.renameTo(nf)
                }
            }
        }
    }

    fun get(uuid: UUID, module: String): JsonObject? {
        return playerRoot[uuid]?.get(module)
    }

    fun set(uuid: UUID, module: String, obj: JsonObject) {
        if(uuid !in playerRoot) {
            playerRoot[uuid] = hashMapOf()
        }
        playerRoot[uuid]!![module] = obj
    }

    fun <T> setTyped(uuid: UUID, module: String, obj: T) {
        set(uuid, module, gson.toJsonTree(obj) as JsonObject)
    }

    fun <T> getTyped(uuid: UUID, module: String): T {
        return gson.fromJson(get(uuid, module), object: TypeToken<T>() {}.type)
    }

    fun <T> setAllTyped(module: String, map: MutableMap<UUID, T>) {
        map.forEach { (uuid, obj) ->
            setTyped(uuid, module, obj)
        }
    }

    fun <T> getAllTyped(module: String): MutableMap<UUID, T> {
        val rtn = hashMapOf<UUID, T>()
        playerRoot.forEach { (k, v) ->
            runCatching {
                rtn[k] = gson.fromJson(v[module]!!, object: TypeToken<T>() {}.type)
            }
        }
        return rtn
    }

    fun save(baseFolder: File) {
        if(!baseFolder.exists()) {
            baseFolder.mkdir()
        }
        val dataBaseFolder = File(baseFolder, "player/")
        if(!dataBaseFolder.exists()) {
            dataBaseFolder.mkdir()
        }

        playerRoot.forEach { (k, v) ->
            val root = JsonObject()
            v.forEach { (module, obj) ->
                root.add(module, obj)
            }
            val file = File(dataBaseFolder, "$k.json")
            if(file.exists()) {
                file.delete()
            }
            file.createNewFile()
            val fw = FileWriter(file, false)
            fw.write(gson.toJson(root))
        }
    }
}