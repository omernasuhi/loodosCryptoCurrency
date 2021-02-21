package com.omer.nasuhi.loodoscryptocurrency.data.models.response

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "Coins")
data class CoinResponse(
    @PrimaryKey @ColumnInfo(name = "coinId") @SerializedName("id")
    val coinId: String,

    @ColumnInfo(name = "coinSymbol")
    val symbol: String,

    @ColumnInfo(name = "coinName")
    val name: String
) : Serializable {
    override fun toString(): String {
        return "CoinResponse(id='$coinId', symbol='$symbol', name='$name')"
    }
}