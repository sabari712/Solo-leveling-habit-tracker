package com.example.sololeveling90days.data

import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class SocialRepository(
    private val authRepository: AuthRepository
) {
    private val postgrest = SupabaseClient.client.postgrest

    @Serializable
    private data class GuildInsert(
        val name: String,
        val description: String,
        @SerialName("created_by") val createdBy: String,
        @SerialName("invite_code") val inviteCode: String
    )

    @Serializable
    private data class MemberInsert(
        @SerialName("guild_id") val guildId: String,
        @SerialName("user_id") val userId: String
    )

    @Serializable
    private data class MessageInsert(
        @SerialName("guild_id") val guildId: String,
        @SerialName("user_id") val userId: String,
        @SerialName("sender_name") val senderName: String,
        val message: String
    )

    // Generate a unique 6-character guild invite code
    private fun generateInviteCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    suspend fun createGuild(name: String, description: String): Guild? = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext null
        try {
            val code = generateInviteCode()
            val insertModel = GuildInsert(
                name = name.trim(),
                description = description.trim(),
                createdBy = userId,
                inviteCode = code
            )
            // Insert the guild
            postgrest["guilds"].insert(insertModel)
            
            // Query the newly created guild by name to get its generated ID
            val guild = postgrest["guilds"].select {
                filter { eq("name", name.trim()) }
            }.decodeSingleOrNull<Guild>()

            if (guild != null) {
                // Auto-join the creator to the guild members list
                joinGuildMember(guild.id, userId)
            }
            guild
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun joinGuild(inviteCode: String): Boolean = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext false
        try {
            // Find the guild with this invite code
            val guild = postgrest["guilds"].select {
                filter { eq("invite_code", inviteCode.trim().uppercase()) }
            }.decodeSingleOrNull<Guild>() ?: return@withContext false

            // Join the guild members list
            joinGuildMember(guild.id, userId)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private suspend fun joinGuildMember(guildId: String, userId: String) {
        // First delete any existing guild membership to make sure they can only belong to one guild at a time
        try {
            postgrest["guild_members"].delete {
                filter { eq("user_id", userId) }
            }
        } catch (e: Exception) {
            // Ignore if not present
        }
        postgrest["guild_members"].insert(MemberInsert(guildId, userId))
    }

    suspend fun leaveGuild(): Boolean = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext false
        try {
            postgrest["guild_members"].delete {
                filter { eq("user_id", userId) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getCurrentGuild(): Guild? = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext null
        try {
            // Find the user's guild member record
            val member = postgrest["guild_members"].select {
                filter { eq("user_id", userId) }
            }.decodeSingleOrNull<GuildMember>() ?: return@withContext null

            // Find the guild details
            postgrest["guilds"].select {
                filter { eq("id", member.guildId) }
            }.decodeSingleOrNull<Guild>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getGuildLeaderboard(guildId: String): List<GuildLeaderboardEntry> = withContext(Dispatchers.IO) {
        try {
            postgrest["guild_leaderboard"].select {
                filter { eq("guild_id", guildId) }
            }.decodeList<GuildLeaderboardEntry>()
             .sortedByDescending { it.weeklyXp }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getGuildMessages(guildId: String): List<GuildMessage> = withContext(Dispatchers.IO) {
        try {
            postgrest["guild_messages"].select {
                filter { eq("guild_id", guildId) }
            }.decodeList<GuildMessage>()
             .sortedBy { it.createdAt ?: "" }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun sendGuildMessage(guildId: String, message: String): Boolean = withContext(Dispatchers.IO) {
        val userId = authRepository.currentUserId() ?: return@withContext false
        try {
            // Query user's display name
            val profile = postgrest["profiles"].select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<UserProfile>()
            val displayName = profile?.name ?: "Hunter"

            val msg = MessageInsert(
                guildId = guildId,
                userId = userId,
                senderName = displayName,
                message = message.trim()
            )
            postgrest["guild_messages"].insert(msg)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
