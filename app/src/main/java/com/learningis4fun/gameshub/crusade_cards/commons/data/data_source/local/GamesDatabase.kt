package com.learningis4fun.gameshub.crusade_cards.commons.data.data_source.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Card
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Game
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameStageRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.GameUserCrossRef
import com.learningis4fun.gameshub.crusade_cards.commons.domain.model.Player
import com.learningis4fun.gameshub.crusade_cards.di.CrusadeCardsDataModule
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.CardDao
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.GameCardDao
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.GameDao
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.PlayerDao
import com.learningis4fun.gameshub.crusade_cards.commons.domain.util.Constants
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.GameStageDao
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.data.data_source.GameUserCrossRefDao
import com.learningis4fun.gameshub.crusade_cards.play_crusade_cards.domain.model.GameCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Player::class, Card::class, Game::class, GameCard::class, GameUserCrossRef::class, GameStageRef::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(1, 2)
    ]
)
abstract class GamesDatabase : RoomDatabase() {
    abstract fun gameUserCrossRef(): GameUserCrossRefDao
    abstract fun cardDao(): CardDao
    @OptIn(ExperimentalCoroutinesApi::class)
    abstract fun gameCardDao(): GameCardDao
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
    abstract fun gameStageDao(): GameStageDao

    class InsertCardsCallback @ExperimentalCoroutinesApi
    @Inject constructor(
        private val database: Provider<GamesDatabase>,
        @CrusadeCardsDataModule.ApplicationScope private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val dao = database.get().cardDao()
            // dispatch background here
            scope.launch {
                prePopulateCards(dao)
            }
        }

        private suspend fun prePopulateCards(cardDao: CardDao) {
            val cards = Constants.Cards.cards
            cards.forEachIndexed { index, card ->
                cardDao.insertCard(card.copy(id = "$index"))
            }
        }
    }
}