package com.brainzomatch.app.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brainzomatch.app.core.data.local.dao.GameProgressDao
import com.brainzomatch.app.core.data.local.dao.ParentalControlsDao
import com.brainzomatch.app.core.data.local.dao.PlayerProfileDao
import com.brainzomatch.app.core.data.local.entities.GameProgressEntity
import com.brainzomatch.app.core.data.local.entities.ParentalControlsEntity
import com.brainzomatch.app.core.data.local.entities.PlayerProfileEntity

/**
 * BrainzoDatabase — Room Database v2 managing local persistence.
 * File Path: app/src/main/java/com/brainzomatch/app/core/data/local/BrainzoDatabase.kt
 */
@Database(
    entities = [
        PlayerProfileEntity::class,
        GameProgressEntity::class,
        ParentalControlsEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(BrainzoTypeConverters::class)
abstract class BrainzoDatabase : RoomDatabase() {

    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun gameProgressDao(): GameProgressDao
    abstract fun parentalControlsDao(): ParentalControlsDao

    companion object {
        @Volatile
        private var INSTANCE: BrainzoDatabase? = null

        /**
         * Migration script from DB Version 1 to Version 2.
         * Adds wardrobe customization fields, avatar unlock lists, audio sliders, and timestamp records.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Alter player_profile
                db.execSQL("ALTER TABLE player_profile ADD COLUMN unlocked_avatar_ids TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN equipped_hat_id TEXT NOT NULL DEFAULT 'item_crown'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN equipped_accessory_id TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN unlocked_item_ids TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN total_playtime_seconds INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE player_profile ADD COLUMN last_login_timestamp INTEGER NOT NULL DEFAULT 0")

                // 2. Alter game_progress
                db.execSQL("ALTER TABLE game_progress ADD COLUMN level_star_map_json TEXT NOT NULL DEFAULT '{}'")
                db.execSQL("ALTER TABLE game_progress ADD COLUMN last_played_timestamp INTEGER NOT NULL DEFAULT 0")

                // 3. Alter parental_controls
                db.execSQL("ALTER TABLE parental_controls ADD COLUMN allowed_game_ids TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE parental_controls ADD COLUMN music_volume INTEGER NOT NULL DEFAULT 75")
                db.execSQL("ALTER TABLE parental_controls ADD COLUMN sfx_volume INTEGER NOT NULL DEFAULT 90")
                db.execSQL("ALTER TABLE parental_controls ADD COLUMN haptics_enabled INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE parental_controls ADD COLUMN parent_email TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getInstance(context: Context): BrainzoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BrainzoDatabase::class.java,
                    "brainzo_match_database.db"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
