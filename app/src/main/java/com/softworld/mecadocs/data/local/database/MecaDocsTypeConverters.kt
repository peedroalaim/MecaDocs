package com.softworld.mecadocs.data.local.database

import androidx.room.TypeConverter
import com.softworld.mecadocs.data.model.AlertaItem
import com.softworld.mecadocs.data.model.CodigoExemplo
import com.softworld.mecadocs.data.model.PinoutItem
import com.softworld.mecadocs.data.model.SpecsData
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Conversores de Tipo do Room utilizando Moshi para transformar objetos complexos em JSON String.
 */
class MecaDocsTypeConverters {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val specsAdapter = moshi.adapter(SpecsData::class.java)
    private val codigoAdapter = moshi.adapter(CodigoExemplo::class.java)

    private val pinoutListType = Types.newParameterizedType(List::class.java, PinoutItem::class.java)
    private val pinoutListAdapter = moshi.adapter<List<PinoutItem>>(pinoutListType)

    private val alertasListType = Types.newParameterizedType(List::class.java, AlertaItem::class.java)
    private val alertasListAdapter = moshi.adapter<List<AlertaItem>>(alertasListType)

    @TypeConverter
    fun fromSpecsData(specs: SpecsData?): String {
        return specs?.let { specsAdapter.toJson(it) } ?: ""
    }

    @TypeConverter
    fun toSpecsData(json: String): SpecsData? {
        if (json.isBlank()) return null
        return try {
            specsAdapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromPinoutList(list: List<PinoutItem>?): String {
        return list?.let { pinoutListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toPinoutList(json: String): List<PinoutItem> {
        if (json.isBlank()) return emptyList()
        return try {
            pinoutListAdapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromAlertasList(list: List<AlertaItem>?): String {
        return list?.let { alertasListAdapter.toJson(it) } ?: "[]"
    }

    @TypeConverter
    fun toAlertasList(json: String): List<AlertaItem> {
        if (json.isBlank()) return emptyList()
        return try {
            alertasListAdapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromCodigoExemplo(codigo: CodigoExemplo?): String {
        return codigo?.let { codigoAdapter.toJson(it) } ?: ""
    }

    @TypeConverter
    fun toCodigoExemplo(json: String): CodigoExemplo? {
        if (json.isBlank()) return null
        return try {
            codigoAdapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }
}
