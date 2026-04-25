package com.raulshma.dailylife.data

class RoomBackedDailyLifeRepository(
    database: com.raulshma.dailylife.data.db.DailyLifeDatabase,
) : DailyLifeRepository by InMemoryDailyLifeRepository(
    seedItems = emptyList(),
    store = RoomDailyLifeStore(database),
)
