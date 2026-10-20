package com.softworld.mecadocs.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.softworld.mecadocs.data.model.AlertaItem
import com.softworld.mecadocs.data.model.CodigoExemplo
import com.softworld.mecadocs.data.model.Componente
import com.softworld.mecadocs.data.model.PinoutItem
import com.softworld.mecadocs.data.model.SpecsData

/**
 * Entidade do Room Database para armazenar componentes marcados como favoritos pelo usuário.
 */
@Entity(tableName = "favorite_components")
data class FavoriteComponentEntity(
    @PrimaryKey
    val nome: String,
    val descricao: String,
    val categoria: String,
    val specs: SpecsData,
    val pinout: List<PinoutItem>,
    val alertas: List<AlertaItem>,
    val codigo: CodigoExemplo?,
    val timestampFav: Long = System.currentTimeMillis()
) {
    /**
     * Converte esta entidade Room para o modelo de domínio [Componente].
     */
    fun toDomainModel(): Componente {
        return Componente(
            nome = nome,
            descricao = descricao,
            categoria = categoria,
            specs = specs,
            pinout = pinout,
            alertas = alertas,
            codigo = codigo
        )
    }

    companion object {
        /**
         * Cria uma nova entidade a partir de um objeto [Componente].
         */
        fun fromDomainModel(componente: Componente): FavoriteComponentEntity {
            return FavoriteComponentEntity(
                nome = componente.nome,
                descricao = componente.descricao,
                categoria = componente.categoria,
                specs = componente.specs,
                pinout = componente.pinout,
                alertas = componente.alertas,
                codigo = componente.codigo
            )
        }
    }
}
