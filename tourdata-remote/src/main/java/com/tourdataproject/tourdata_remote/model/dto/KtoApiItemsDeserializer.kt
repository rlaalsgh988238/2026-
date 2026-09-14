package com.tourdataproject.tourdata_remote.model.dto

import com.google.gson.*
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class KtoApiItemsDeserializer : JsonDeserializer<KtoApiItems<*>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): KtoApiItems<*> {
        if (json == null || json.isJsonPrimitive || !json.isJsonObject) {
            return KtoApiItems(emptyList<Any>())
        }

        val obj = json.asJsonObject
        val itemElem = obj.get("item") ?: return KtoApiItems(emptyList<Any>())

        val targetType = if (typeOfT is ParameterizedType) {
            typeOfT.actualTypeArguments.firstOrNull() ?: Any::class.java
        } else {
            Any::class.java
        }

        val list = mutableListOf<Any?>()
        try {
            when {
                itemElem.isJsonArray -> {
                    itemElem.asJsonArray.forEach { elem ->
                        context?.deserialize<Any>(elem, targetType)?.let { list.add(it) }
                    }
                }
                itemElem.isJsonObject -> {
                    context?.deserialize<Any>(itemElem, targetType)?.let { list.add(it) }
                }
            }
        } catch (e: Exception) {
            return KtoApiItems(emptyList<Any>())
        }

        @Suppress("UNCHECKED_CAST")
        return KtoApiItems(list as List<Any>)
    }
}