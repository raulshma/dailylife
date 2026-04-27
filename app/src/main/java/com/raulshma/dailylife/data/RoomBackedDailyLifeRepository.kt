package com.raulshma.dailylife.data

import com.raulshma.dailylife.data.db.DailyLifeDatabase

class RoomBackedDailyLifeRepository(
    database: DailyLifeDatabase,
) : DailyLifeRepository by InMemoryDailyLifeRepository(
    seedItems = emptyList(),
    store = RoomDailyLifeStore(database),
)
