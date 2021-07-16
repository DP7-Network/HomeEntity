package cn.thelama.homeent.module

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

/**
 * Not tested feature!
 * TODO: Test this feature
 */
object ModuledPlayerDataManager {
    private val snake = Yaml()
    //                             Player UUID    |   Module Name | Module Player Data Json Tree
    private val playerRoot: MutableMap<UUID, MutableMap<String, Any>> = hashMapOf()

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
                if(it.extension == "yml") {
                    val fileReader = FileReader(it)
                    val rootObj = snake.loadAs(fileReader, MutableMap::class.java) // TODO: Bug fix for class not found
                    playerRoot[UUID.fromString(it.nameWithoutExtension)] = rootObj as MutableMap<String, Any>
                    fileReader.close()
                }
            }.onFailure { t ->
                t.printStackTrace()
                val nf = File(dataBaseFolder, "${it.name}.error")
                if(!nf.exists()) {
                    it.renameTo(nf)
                }
            }
        }
    }

    fun get(uuid: UUID, module: String): Any? {
        return playerRoot[uuid]?.get(module)
    }

    fun set(uuid: UUID, module: String, obj: Any) {
        if(uuid !in playerRoot) {
            playerRoot[uuid] = hashMapOf()
        }
        playerRoot[uuid]!![module] = obj
    }

    fun setTyped(uuid: UUID, module: String, obj: Any) {
        set(uuid, module, obj)
    }

    fun <T> getTyped(uuid: UUID, module: String): T {
        return get(uuid, module) as T
    }

    fun <T> setAllTyped(module: String, map: MutableMap<UUID, T>) {
        map.forEach { (uuid, obj) ->
            setTyped(uuid, module, obj as Any)
        }
    }

    fun <T> getAllTyped(module: String): MutableMap<UUID, T> {
        val rtn = hashMapOf<UUID, T>()
        playerRoot.forEach { (k, v) ->
            runCatching {
                rtn[k] = v[module]!! as T
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
            val file = File(dataBaseFolder, "$k.yml")
            if(file.exists()) {
                file.delete()
            }
            file.createNewFile()
            val fw = FileWriter(file, false)
            fw.write(snake.dump(v))
            fw.flush()
            fw.close()
        }
    }
}