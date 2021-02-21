package com.omer.nasuhi.loodoscryptocurrency.data

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.omer.nasuhi.loodoscryptocurrency.data.models.response.CoinResponse

@Dao
interface CoinDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoinList(coinList: List<CoinResponse>): List<Long>

    @RawQuery(observedEntities = [CoinResponse::class])
    suspend fun searchCoin(query: SupportSQLiteQuery): List<CoinResponse>
}