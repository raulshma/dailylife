package com.raulshma.dailylife.data

import com.raulshma.dailylife.data.db.DailyLifeDatabase

@Suppress("unused")
class RoomBackedDailyLifeRepository(
    database: DailyLifeDatabase,
) {
    private val delegate = InMemoryDailyLifeRepository(
        seedItems = emptyList(),
        store = RoomDailyLifeStore(database),
    )
}
