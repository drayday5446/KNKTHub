package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.crypto.CryptoEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        ContactEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "knkt_messages_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val contactDao = database.contactDao()
            val convDao = database.conversationDao()
            val msgDao = database.messageDao()

            val now = System.currentTimeMillis()
            val min = 60 * 1000L

            val alexPhone = "+1 (555) 019-2834"
            val sarahPhone = "+1 (555) 014-9921"
            val davidPhone = "+1 (555) 018-7744"
            val elenaPhone = "+1 (555) 012-3388"
            val marcusPhone = "+1 (555) 017-6655"

            val contacts = listOf(
                ContactEntity(
                    id = 1,
                    name = "Alex Rivera",
                    phone = alexPhone,
                    avatarColorHex = 0xFF06B6D4, // Neon Cyan
                    status = "Designing OLED interfaces",
                    isE2EEVerified = true,
                    rcsCapable = true,
                    keyFingerprint = CryptoEngine.generateKeyFingerprint(alexPhone)
                ),
                ContactEntity(
                    id = 2,
                    name = "Sarah Chen",
                    phone = sarahPhone,
                    avatarColorHex = 0xFF10B981, // Emerald Green
                    status = "Exploring Seoul ✈️",
                    isE2EEVerified = true,
                    rcsCapable = true,
                    keyFingerprint = CryptoEngine.generateKeyFingerprint(sarahPhone)
                ),
                ContactEntity(
                    id = 3,
                    name = "David Kim",
                    phone = davidPhone,
                    avatarColorHex = 0xFF3B82F6, // Royal Blue
                    status = "Running late for coffee ☕",
                    isE2EEVerified = false,
                    rcsCapable = true,
                    keyFingerprint = CryptoEngine.generateKeyFingerprint(davidPhone)
                ),
                ContactEntity(
                    id = 4,
                    name = "Elena Rostova",
                    phone = elenaPhone,
                    avatarColorHex = 0xFFA855F7, // Neon Purple
                    status = "Security Key: Verified 🔐",
                    isE2EEVerified = true,
                    rcsCapable = true,
                    keyFingerprint = CryptoEngine.generateKeyFingerprint(elenaPhone)
                ),
                ContactEntity(
                    id = 5,
                    name = "Marcus Vance",
                    phone = marcusPhone,
                    avatarColorHex = 0xFFF97316, // Vibrant Orange
                    status = "Testing SMS/MMS fallbacks",
                    isE2EEVerified = false,
                    rcsCapable = false,
                    keyFingerprint = ""
                )
            )
            contactDao.insertAll(contacts)

            // Direct Threads (1-on-1)
            // Pin 1 & Pin 2 pre-seeded to demonstrate side-by-side card grid!
            val threadAlex = ConversationEntity(
                id = 1,
                recipientName = "Alex Rivera",
                recipientPhone = alexPhone,
                avatarColorHex = 0xFF06B6D4,
                activeProtocol = "RCS",
                isE2EE = true,
                isGroup = false,
                lastMessage = "Pure black OLED theme is flawless! ⚡",
                lastTimestamp = now - 3 * min,
                unreadCount = 2,
                isPinned = true,
                isArchived = false,
                isTrash = false,
                customUserBubbleColor = 0xFF06B6D4,
                customPeerBubbleColor = 0xFF1E293B,
                customUserBubbleShape = "ROUNDED",
                customPeerBubbleShape = "ROUNDED"
            )

            val threadSarah = ConversationEntity(
                id = 2,
                recipientName = "Sarah Chen",
                recipientPhone = sarahPhone,
                avatarColorHex = 0xFF10B981,
                activeProtocol = "RCS",
                isE2EE = true,
                isGroup = false,
                lastMessage = "Sent a photo from Namsan Tower 📷",
                lastTimestamp = now - 22 * min,
                unreadCount = 0,
                isPinned = true,
                isArchived = false,
                isTrash = false,
                customUserBubbleColor = 0xFF10B981,
                customPeerBubbleColor = 0xFF064E3B,
                customUserBubbleShape = "PILL",
                customPeerBubbleShape = "PILL"
            )

            val threadDavid = ConversationEntity(
                id = 3,
                recipientName = "David Kim",
                recipientPhone = davidPhone,
                avatarColorHex = 0xFF3B82F6,
                activeProtocol = "SMS",
                isE2EE = false,
                isGroup = false,
                lastMessage = "Meeting you downstairs in 5 mins",
                lastTimestamp = now - 45 * min,
                unreadCount = 0,
                isPinned = false,
                isArchived = false,
                isTrash = false
            )

            val threadElena = ConversationEntity(
                id = 4,
                recipientName = "Elena Rostova",
                recipientPhone = elenaPhone,
                avatarColorHex = 0xFFA855F7,
                activeProtocol = "RCS",
                isE2EE = true,
                isGroup = false,
                lastMessage = "Confirmed: AES-256-GCM cipher exchange valid 🔒",
                lastTimestamp = now - 2 * 60 * min,
                unreadCount = 0,
                isPinned = false,
                isArchived = false,
                isTrash = false
            )

            val threadMarcus = ConversationEntity(
                id = 5,
                recipientName = "Marcus Vance",
                recipientPhone = marcusPhone,
                avatarColorHex = 0xFFF97316,
                activeProtocol = "SMS",
                isE2EE = false,
                isGroup = false,
                lastMessage = "Got the MMS dispatch notification 👍",
                lastTimestamp = now - 6 * 60 * min,
                unreadCount = 0,
                isPinned = false,
                isArchived = false,
                isTrash = false
            )

            // Group Threads (3+ people)
            val groupDevs = ConversationEntity(
                id = 6,
                recipientName = "KNKT Core Devs",
                recipientPhone = "group_knkt_devs",
                avatarColorHex = 0xFF8B5CF6,
                activeProtocol = "RCS",
                isE2EE = true,
                isGroup = true,
                groupParticipants = "Alex Rivera, Sarah Chen, Elena Rostova, David Kim",
                lastMessage = "Elena: All 4 pinned cards render with zero latency!",
                lastTimestamp = now - 8 * min,
                unreadCount = 3,
                isPinned = false,
                isArchived = false,
                isTrash = false,
                groupBubbleConfigJson = "Alex Rivera|0xFF06B6D4|ROUNDED;Sarah Chen|0xFF10B981|PILL;Elena Rostova|0xFFA855F7|RECTANGULAR;David Kim|0xFF3B82F6|ROUNDED"
            )

            val groupTravel = ConversationEntity(
                id = 7,
                recipientName = "Seoul Travel Crew",
                recipientPhone = "group_seoul_crew",
                avatarColorHex = 0xFFEC4899,
                activeProtocol = "RCS",
                isE2EE = false,
                isGroup = true,
                groupParticipants = "Sarah Chen, Elena Rostova, Marcus Vance",
                lastMessage = "Sarah: Check out this dinner spot recommendation!",
                lastTimestamp = now - 3 * 60 * min,
                unreadCount = 0,
                isPinned = false,
                isArchived = false,
                isTrash = false,
                groupBubbleConfigJson = "Sarah Chen|0xFFEC4899|PILL;Elena Rostova|0xFFA855F7|ROUNDED;Marcus Vance|0xFFF97316|RECTANGULAR"
            )

            convDao.insertAll(listOf(threadAlex, threadSarah, threadDavid, threadElena, threadMarcus, groupDevs, groupTravel))

            // Seed Messages with various media attachments for Media Tab!
            val enc1 = CryptoEngine.encryptMessage("Pure black OLED theme is flawless! ⚡", alexPhone)
            val msgAlex1 = MessageEntity(
                conversationId = 1,
                sender = "Alex Rivera",
                isFromUser = false,
                text = "Pure black OLED theme is flawless! ⚡",
                timestamp = now - 3 * min,
                status = "READ",
                protocol = "RCS",
                isEncrypted = true,
                cipherTextBase64 = enc1.cipherTextBase64,
                ivHex = enc1.ivHex,
                tagHex = enc1.tagHex,
                reaction = "🔥"
            )

            val msgSarahMedia = MessageEntity(
                conversationId = 2,
                sender = "Sarah Chen",
                isFromUser = false,
                text = "Sent a photo from Namsan Tower 📷",
                timestamp = now - 22 * min,
                status = "READ",
                protocol = "RCS",
                isEncrypted = true,
                attachmentType = "IMAGE",
                attachmentTitle = "namsan_seoul_tower.jpg",
                attachmentUri = "https://images.unsplash.com/photo-1546874177-9e664107314e?w=800",
                reaction = "❤️"
            )

            val msgAudio = MessageEntity(
                conversationId = 2,
                sender = "ME",
                isFromUser = true,
                text = "Voice Memo (0:18)",
                timestamp = now - 20 * min,
                status = "READ",
                protocol = "RCS",
                isEncrypted = true,
                attachmentType = "AUDIO",
                attachmentTitle = "Voice Memo (0:18)"
            )

            val msgLocation = MessageEntity(
                conversationId = 7,
                sender = "Sarah Chen",
                isFromUser = false,
                text = "Gwangjang Market Food Alley",
                timestamp = now - 3 * 60 * min,
                status = "READ",
                protocol = "RCS",
                attachmentType = "LOCATION",
                attachmentTitle = "37.5700° N, 126.9997° E"
            )

            val msgDoc = MessageEntity(
                conversationId = 6,
                sender = "Elena Rostova",
                isFromUser = false,
                text = "KNKT_Architecture_Spec_v2.pdf",
                timestamp = now - 12 * min,
                status = "READ",
                protocol = "RCS",
                attachmentType = "FILE",
                attachmentTitle = "KNKT_Architecture_Spec_v2.pdf"
            )

            val msgGroupDevs = MessageEntity(
                conversationId = 6,
                sender = "Elena Rostova",
                isFromUser = false,
                text = "All 4 pinned cards render with zero latency!",
                timestamp = now - 8 * min,
                status = "READ",
                protocol = "RCS",
                isEncrypted = true
            )

            msgDao.insertAll(listOf(msgAlex1, msgSarahMedia, msgAudio, msgLocation, msgDoc, msgGroupDevs))
        }
    }
}
