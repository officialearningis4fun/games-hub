package com.learningis4fun.gameshub.feature_crusade_cards.feature_play_crusade_cards.data.data_source

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.google.common.truth.Truth.assertThat
import com.learningis4fun.gameshub.feature_crusade_cards.commons.data.data_source.local.GamesDatabase
import com.learningis4fun.gameshub.feature_crusade_cards.commons.domain.model.Card
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named


@ExperimentalCoroutinesApi
@MediumTest
@HiltAndroidTest
class CardDaoTest {

    @get:Rule
    var hiltAndroidRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test-db")
    lateinit var db: GamesDatabase
    private lateinit var cardDao: CardDao

    @Before
    fun setup() {
        hiltAndroidRule.inject()
        cardDao = db.cardDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertCardWithEmptyId_notSave() = runBlockingTest{
        val card = Card(
            name = "name",
            type = "type"
        )
        cardDao.insertCard(card)

        assertThat(cardDao.getCards()).isEmpty()
    }

    @Test
    fun insertCardWithValidInput_SaveSuccessfully() = runBlockingTest{
        val card = Card(
            id = "id",
            name = "name",
            type = "type"
        )
        cardDao.insertCard(card)

        assertThat(cardDao.getCards()).contains(card)
    }

    @Test
    fun updateCardWithValidInput_UpdateSuccessfully() = runBlockingTest{
        val card = Card(
            id = "id",
            name = "name",
            type = "type"
        )
        cardDao.insertCard(card)

        val updatedCard = card.copy(name = "new name")
        cardDao.updateCard(updatedCard)

        assertThat(cardDao.getCards()).contains(updatedCard)
        assertThat(cardDao.getCards()).doesNotContain(card)
    }
}