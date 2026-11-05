package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE isArchived = 0 AND isTrash = 0 AND isGroup = 0 ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllDirectThreads(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 0 AND isTrash = 0 AND isGroup = 1 ORDER BY isPinned DESC, lastTimestamp DESC")
    fun getAllGroupThreads(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 0 AND isTrash = 0 AND isPinned = 1 ORDER BY lastTimestamp DESC LIMIT 4")
    fun getPinnedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 0 AND isTrash = 0 AND isPinned = 1 AND isGroup = 0 ORDER BY lastTimestamp DESC LIMIT 4")
    fun getPinnedDirectConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 0 AND isTrash = 0 AND isPinned = 1 AND isGroup = 1 ORDER BY lastTimestamp DESC LIMIT 4")
    fun getPinnedGroupConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 AND isTrash = 0 ORDER BY lastTimestamp DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isTrash = 1 ORDER BY lastTimestamp DESC")
    fun getTrashConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT COUNT(*) FROM conversations WHERE isArchived = 0 AND isTrash = 0 AND isPinned = 1")
    suspend fun getPinnedCount(): Int

    @Query("SELECT COUNT(*) FROM conversations WHERE isArchived = 0 AND isTrash = 0 AND isPinned = 1 AND isGroup = 0")
    suspend fun getPinnedDirectCount(): Int

    @Query("SELECT COUNT(*) FROM conversations WHERE isArchived = 0 AND isTrash = 0 AND isPinned = 1 AND isGroup = 1")
    suspend fun getPinnedGroupCount(): Int

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun getConversationFlow(id: Long): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE recipientPhone = :phone LIMIT 1")
    suspend fun getConversationByPhone(phone: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessage = :lastMsg, lastTimestamp = :timestamp, activeProtocol = :protocol, isE2EE = :isEncrypted WHERE id = :id")
    suspend fun updateLastMessage(id: Long, lastMsg: String, timestamp: Long, protocol: String, isEncrypted: Boolean)

    @Query("UPDATE conversations SET unreadCount = CASE WHEN unreadCount = 0 THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun toggleReadStatus(id: Long)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE id = :id")
    suspend fun setPinned(id: Long, pinned: Boolean)

    @Query("UPDATE conversations SET isArchived = NOT isArchived, isTrash = 0 WHERE id = :id")
    suspend fun toggleArchive(id: Long)

    @Query("UPDATE conversations SET isTrash = 1, isPinned = 0 WHERE id = :id")
    suspend fun moveToTrash(id: Long)

    @Query("UPDATE conversations SET isTrash = 0 WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM conversations WHERE isTrash = 1")
    suspend fun emptyTrash()

    @Query("UPDATE conversations SET activeProtocol = :protocol WHERE id = :id")
    suspend fun updateProtocol(id: Long, protocol: String)

    @Query("UPDATE conversations SET isE2EE = :isE2ee WHERE id = :id")
    suspend fun updateE2EE(id: Long, isE2ee: Boolean)

    @Query("UPDATE conversations SET customUserBubbleColor = :userColor, customPeerBubbleColor = :peerColor, customUserBubbleShape = :userShape, customPeerBubbleShape = :peerShape, customBackgroundUri = :bgUri, groupBubbleConfigJson = :groupConfig WHERE id = :id")
    suspend fun updateCustomTheming(
        id: Long,
        userColor: Long?,
        peerColor: Long?,
        userShape: String?,
        peerShape: String?,
        bgUri: String?,
        groupConfig: String?
    )

    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :id")
    suspend fun incrementUnread(id: Long)

    @Query("DELETE FROM conversations WHERE recipientPhone NOT LIKE 'gm:%'")
    suspend fun deleteNonBridgedConversations()

    @Delete
    suspend fun deleteConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversation(convId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE attachmentType IS NOT NULL ORDER BY timestamp DESC")
    fun getAllMediaMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE attachmentType = :type ORDER BY timestamp DESC")
    fun getMediaMessagesByType(type: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: Long): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET reaction = :reaction WHERE id = :id")
    suspend fun updateReaction(id: Long, reaction: String?)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :convId AND text = :text AND timestamp = :timestamp AND isFromUser = 0")
    suspend fun countMatching(convId: Long, text: String, timestamp: Long): Int

    @Query("DELETE FROM messages WHERE conversationId NOT IN (SELECT id FROM conversations)")
    suspend fun deleteOrphanMessages()

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE conversationId = :convId")
    suspend fun deleteMessagesForConversation(convId: Long)
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: Long): ContactEntity?

    @Query("SELECT * FROM contacts WHERE phone = :phone LIMIT 1")
    suspend fun getContactByPhone(phone: String): ContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("UPDATE contacts SET isE2EEVerified = :isVerified WHERE phone = :phone")
    suspend fun updateE2EEVerification(phone: String, isVerified: Boolean)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)
}
