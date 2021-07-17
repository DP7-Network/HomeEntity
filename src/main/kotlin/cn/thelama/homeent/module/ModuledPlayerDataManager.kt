package cn.thelama.homeent.module

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mongodb.*
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import org.bson.Document
import org.bukkit.configuration.file.FileConfiguration
import java.lang.reflect.Type
import java.util.*

/**
 * Not tested feature!
 * TODO: Test this feature
 */
object ModuledPlayerDataManager {
    private val GSON = Gson()
    private lateinit var mongoClient: MongoClient
    private lateinit var database: MongoDatabase

    fun init(config: FileConfiguration) {
        mongoClient = MongoClients.create(config.getString("mongo.url"))
        database = mongoClient.getDatabase(config.getString("mongo.database"))
    }

    fun get(uuid: UUID, module: String): Any? {
        return database.getCollection(module).find(Filters.eq("uuid", uuid.toString())).cursor().tryNext()?.get("cfgObject")
    }

    fun set(uuid: UUID, module: String, obj: Any) {
        val collection = database.getCollection(module)
        if(collection.find(Filters.eq("uuid", uuid.toString())).cursor().hasNext()) {
            database.getCollection(module).replaceOne(Filters.eq("uuid", uuid.toString()), Document.parse(GSON.toJson(ModuleEntryWrapper(uuid.toString(), obj))))
        } else {
            database.getCollection(module).insertOne(Document.parse(GSON.toJson(ModuleEntryWrapper(uuid.toString(), obj))))
        }
    }

    fun setTyped(uuid: UUID, module: String, obj: Any) {
        set(uuid, module, obj)
    }

    fun <T> getTyped(uuid: UUID, module: String): T? {
        return database.getCollection(module).find(Filters.eq("uuid", uuid.toString())).cursor().tryNext()?.get("cfgObject", object: TypeToken<T>() {}.rawType) as T?
    }

    fun <T> setAllTyped(module: String, map: MutableMap<UUID, T>) {
        map.forEach { (uuid, obj) ->
            setTyped(uuid, module, obj as Any)
        }
    }

    fun <T> getAllTyped(module: String): MutableMap<UUID, T> {
        return getAllTyped(module, object: TypeToken<T>() {}.type)
    }

    fun <T> getAllTyped(module: String, typeOfT: Type): MutableMap<UUID, T> {
        val rtn = hashMapOf<UUID, T>()
        database.getCollection(module).find().cursor().iterator().forEach {
            rtn[UUID.fromString(it.getString("uuid"))] = GSON.fromJson((it.get("cfgObject") as Document).toJson(), typeOfT) as T
        }
        return rtn
    }
}