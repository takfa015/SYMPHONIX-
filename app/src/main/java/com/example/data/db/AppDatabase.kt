package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AuditEntry
import com.example.data.model.CashReplenishment
import com.example.data.model.CashSession
import com.example.data.model.Disbursement

@Database(
    entities = [
        CashSession::class,
        CashReplenishment::class,
        Disbursement::class,
        AuditEntry::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cashDao(): CashDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Désactivation temporaire des contraintes de clés étrangères pendant la restructuration
                db.execSQL("PRAGMA foreign_keys=OFF;")

                // 1. Migration de cash_sessions vers montants exacts en centimes (Long) + champs d'intégrité
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cash_sessions_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `reference` TEXT NOT NULL,
                        `dateText` TEXT NOT NULL,
                        `dateMillis` INTEGER NOT NULL,
                        `establishmentName` TEXT NOT NULL,
                        `establishmentSubTitle` TEXT NOT NULL,
                        `responsibleName` TEXT NOT NULL,
                        `managerName` TEXT NOT NULL,
                        `initialFund` INTEGER NOT NULL,
                        `initialFundTime` TEXT NOT NULL,
                        `initialFundSource` TEXT NOT NULL,
                        `countedCash` INTEGER,
                        `closingTime` TEXT,
                        `isClosed` INTEGER NOT NULL,
                        `reopenCount` INTEGER NOT NULL DEFAULT 0,
                        `integrityHash` TEXT,
                        `currency` TEXT NOT NULL,
                        `notes` TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO `cash_sessions_new` (
                        `id`, `reference`, `dateText`, `dateMillis`,
                        `establishmentName`, `establishmentSubTitle`, `responsibleName`, `managerName`,
                        `initialFund`, `initialFundTime`, `initialFundSource`,
                        `countedCash`, `closingTime`, `isClosed`, `reopenCount`, `integrityHash`,
                        `currency`, `notes`
                    )
                    SELECT
                        `id`, `reference`, `dateText`, `dateMillis`,
                        `establishmentName`, `establishmentSubTitle`, `responsibleName`, `managerName`,
                        CAST(ROUND(`initialFund` * 100) AS INTEGER),
                        `initialFundTime`, `initialFundSource`,
                        CASE WHEN `countedCash` IS NOT NULL THEN CAST(ROUND(`countedCash` * 100) AS INTEGER) ELSE NULL END,
                        `closingTime`, `isClosed`, 0, NULL,
                        `currency`, `notes`
                    FROM `cash_sessions`
                """.trimIndent())

                db.execSQL("DROP TABLE `cash_sessions`")
                db.execSQL("ALTER TABLE `cash_sessions_new` RENAME TO `cash_sessions`")

                // 2. Migration de cash_replenishments : montants en centimes, annulation douce et index unique
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cash_replenishments_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `orderNumber` INTEGER NOT NULL,
                        `time` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `reason` TEXT NOT NULL,
                        `sourceLocation` TEXT NOT NULL,
                        `registeredBy` TEXT NOT NULL,
                        `cancelledAt` INTEGER,
                        `cancelReason` TEXT,
                        FOREIGN KEY(`sessionId`) REFERENCES `cash_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO `cash_replenishments_new` (
                        `id`, `sessionId`, `orderNumber`, `time`, `amount`, `reason`, `sourceLocation`, `registeredBy`, `cancelledAt`, `cancelReason`
                    )
                    SELECT
                        `id`, `sessionId`, `orderNumber`, `time`,
                        CAST(ROUND(`amount` * 100) AS INTEGER),
                        `reason`, `sourceLocation`, `registeredBy`, NULL, NULL
                    FROM `cash_replenishments`
                """.trimIndent())

                db.execSQL("DROP TABLE `cash_replenishments`")
                db.execSQL("ALTER TABLE `cash_replenishments_new` RENAME TO `cash_replenishments`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cash_replenishments_sessionId` ON `cash_replenishments` (`sessionId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_cash_replenishments_sessionId_orderNumber` ON `cash_replenishments` (`sessionId`, `orderNumber`)")

                // 3. Migration de disbursements : montants en centimes, annulation douce et index unique
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `disbursements_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `orderNumber` INTEGER NOT NULL,
                        `time` TEXT NOT NULL,
                        `designation` TEXT NOT NULL,
                        `parentCategory` TEXT NOT NULL,
                        `subCategory` TEXT NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `recipient` TEXT NOT NULL,
                        `cancelledAt` INTEGER,
                        `cancelReason` TEXT,
                        FOREIGN KEY(`sessionId`) REFERENCES `cash_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())

                db.execSQL("""
                    INSERT INTO `disbursements_new` (
                        `id`, `sessionId`, `orderNumber`, `time`, `designation`, `parentCategory`, `subCategory`, `amount`, `recipient`, `cancelledAt`, `cancelReason`
                    )
                    SELECT
                        `id`, `sessionId`, `orderNumber`, `time`, `designation`, `parentCategory`, `subCategory`,
                        CAST(ROUND(`amount` * 100) AS INTEGER),
                        `recipient`, NULL, NULL
                    FROM `disbursements`
                """.trimIndent())

                db.execSQL("DROP TABLE `disbursements`")
                db.execSQL("ALTER TABLE `disbursements_new` RENAME TO `disbursements`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_disbursements_sessionId` ON `disbursements` (`sessionId`)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_disbursements_sessionId_orderNumber` ON `disbursements` (`sessionId`, `orderNumber`)")

                // 4. Nouvelle table de journal d'audit
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `audit_entries` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `action` TEXT NOT NULL,
                        `entityType` TEXT NOT NULL,
                        `entityId` INTEGER,
                        `detailsJson` TEXT NOT NULL,
                        `reason` TEXT NOT NULL,
                        FOREIGN KEY(`sessionId`) REFERENCES `cash_sessions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_audit_entries_sessionId` ON `audit_entries` (`sessionId`)")

                // Réactivation des contraintes de clés étrangères
                db.execSQL("PRAGMA foreign_keys=ON;")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recap_caisse_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
