package com.nejracoric.digitalnialbum.data.remote.dto

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/** API paketića ponekad ubaci grb s id-om tipa "grb-FRA" — mapiraj u -1 da se odfiltruje. */
class PlayerIdDeserializer : JsonDeserializer<Int> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Int {
        return try {
            if (json.isJsonPrimitive && json.asJsonPrimitive.isNumber) json.asInt else -1
        } catch (_: Exception) {
            -1
        }
    }
}
