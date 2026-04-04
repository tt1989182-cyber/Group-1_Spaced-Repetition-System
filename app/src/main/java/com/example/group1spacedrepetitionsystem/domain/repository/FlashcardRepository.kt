package com.example.estudapp.domain.repository

import android.net.Uri
import android.util.Log
import com.example.estudapp.data.model.DeckDTO
import com.example.estudapp.data.model.FlashcardDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import com.example.estudapp.data.model.DeckPlayStatDTO
import com.example.estudapp.data.model.ReviewResultDTO
import com.example.estudapp.data.model.SimpleChatMessageDTO
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.ServerValue
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume


class FlashcardRepository {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val decksRef = database.getReference("decks")
    private val flashcardsRef = database.getReference("flashcards")
    private val statsRef = database.getReference("stats")
    private val chatsRef = database.getReference("chats")
    private val usersRef = database.getReference("users")

    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    suspend fun getFlashcard(deckId: String, flashcardId: String): Result<FlashcardDTO?> {
        return try {
            val snapshot = flashcardsRef.child(deckId).child(flashcardId).get().await()
            val flashcard = snapshot.getValue(FlashcardDTO::class.java)
            Result.success(flashcard)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFlashcard(deckId: String, flashcard: FlashcardDTO): Result<Unit> {
        return try {
            val flashcardRef = flashcardsRef.child(deckId).child(flashcard.id)
            flashcardRef.setValue(flashcard).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFlashcard(deckId: String, flashcardId: String): Result<Unit> {
        return try {
            val flashcardRef = flashcardsRef.child(deckId).child(flashcardId)
            flashcardRef.removeValue().await()
            updateDeckCardCount(deckId, -1)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadFile(uri: Uri): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado."))
            val fileName = UUID.randomUUID().toString()
            val fileRef = storage.reference.child("$userId/$fileName")
            fileRef.putFile(uri).await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDeck(name: String, description: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado."))
            val newDeckRef = decksRef.child(userId).push()
            val deck = DeckDTO(
                id = newDeckRef.key!!,
                name = name,
                description = description,
                userId = userId
            )
            newDeckRef.setValue(deck).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteDeck(deckId: String): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado."))
            val cardsSnap = flashcardsRef.child(deckId).get().await()
            val updates = hashMapOf<String, Any?>()
            updates["decks/$userId/$deckId"] = null
            for (card in cardsSnap.children) {
                val cardId = card.key ?: continue
                updates["flashcards/$deckId/$cardId"] = null
            }
            FirebaseDatabase.getInstance().reference.updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDecks(): Flow<Result<List<DeckDTO>>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(Result.failure(Exception("Usuário não autenticado.")))
            awaitClose()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(DeckDTO::class.java) }
                trySend(Result.success(items))
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        decksRef.child(userId).addValueEventListener(listener)
        awaitClose { decksRef.child(userId).removeEventListener(listener) }
    }

    suspend fun saveFlashcard(deckId: String, flashcard: FlashcardDTO): Result<Unit> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado."))
            flashcard.userId = userId
            flashcard.deckId = deckId
            val newFlashcardRef = flashcardsRef.child(deckId).push()
            flashcard.id = newFlashcardRef.key!!
            newFlashcardRef.setValue(flashcard).await()
            updateDeckCardCount(deckId, 1)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFlashcards(deckId: String): Flow<Result<List<FlashcardDTO>>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(FlashcardDTO::class.java) }
                trySend(Result.success(items))
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        flashcardsRef.child(deckId).addValueEventListener(listener)
        awaitClose { flashcardsRef.child(deckId).removeEventListener(listener) }
    }

    suspend fun saveDeckSessionStat(session: DeckPlayStatDTO): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado."))
            val deckId = session.deckId ?: return Result.failure(IllegalArgumentException("deckId ausente."))
            val newSessionRef = statsRef.child(userId).child(deckId).push()
            val sessionId = newSessionRef.key!!
            session.id = sessionId
            session.userId = userId
            newSessionRef.setValue(session).await()
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getDeckSessions(deckId: String): kotlinx.coroutines.flow.Flow<Result<List<DeckPlayStatDTO>>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(Result.failure(Exception("Usuário não autenticado.")))
            awaitClose(); return@callbackFlow
        }
        val ref = statsRef.child(userId).child(deckId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = snapshot.children.mapNotNull { it.getValue(DeckPlayStatDTO::class.java) }
                trySend(Result.success(items))
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener)}
    }

    suspend fun getFlashcardsOnce(deckId: String): Result<List<FlashcardDTO>> {
        return try {
            val snapshot = flashcardsRef.child(deckId).get().await()
            val items = snapshot.children.mapNotNull { it.getValue(FlashcardDTO::class.java) }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun updateDeckCardCount(deckId: String, increment: Int) {
        val userId = getCurrentUserId() ?: return
        val deckRef = decksRef.child(userId).child(deckId).child("cardCount")

        suspendCancellableCoroutine<Unit> { continuation ->
            deckRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    val currentCount = currentData.getValue(Int::class.java) ?: 0
                    val newCount = currentCount + increment
                    currentData.value = if (newCount < 0) 0 else newCount
                    return Transaction.success(currentData)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error != null) {
                        continuation.resumeWithException(error.toException())
                    } else {
                        continuation.resume(Unit)
                    }
                }
            })
        }
    }

    suspend fun sendDirectMessage(text: String, sender: String = "USER"): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado."))
            val userChatRef = chatsRef.child(userId)
            val messageRef = userChatRef.push()
            val messageId = messageRef.key!!
            val message = SimpleChatMessageDTO(
                id = messageId,
                sender = sender,
                text = text
            )
            messageRef.setValue(message).await()
            Result.success(messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeUserMessages(): Flow<Result<List<SimpleChatMessageDTO>>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(Result.failure(Exception("Usuário não autenticado.")))
            awaitClose(); return@callbackFlow
        }
        val userChatRef = chatsRef.child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(SimpleChatMessageDTO::class.java) }
                trySend(Result.success(messages))
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(Result.failure(error.toException()))
            }
        }
        userChatRef.addValueEventListener(listener)
        awaitClose { userChatRef.removeEventListener(listener) }
    }

    suspend fun getAllUserStats(): Result<List<DeckPlayStatDTO>> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado."))
            val snapshot = statsRef.child(userId).get().await()
            val allStats = mutableListOf<DeckPlayStatDTO>()
            snapshot.children.forEach { deckSnapshot ->
                deckSnapshot.children.forEach { sessionSnapshot ->
                    val stat = sessionSnapshot.getValue(DeckPlayStatDTO::class.java)
                    stat?.let { allStats.add(it) }
                }
            }
            Result.success(allStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeckInfo(deckId: String): Result<DeckDTO?> {
        return try {
            val userId = getCurrentUserId() ?: return Result.failure(Exception("Usuário não autenticado."))
            val snapshot = decksRef.child(userId).child(deckId).get().await()
            val deck = snapshot.getValue(DeckDTO::class.java)
            Result.success(deck)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun calculateNextReview(deckId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                return@withContext Result.failure(Exception("Usuário não está logado."))
            }

            val token = try {
                Tasks.await(user.getIdToken(true)).token
            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Não foi possível obter o token."))
            }

            if (token == null) {
                return@withContext Result.failure(Exception("Token é nulo."))
            }

            val url =
                URL("https://estudapp-api-293741035243.southamerica-east1.run.app/decks/$deckId/calculate-next-review")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.connectTimeout = 30000
            connection.readTimeout = 60000

            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Erro do servidor: $responseCode"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            } finally {
                connection.disconnect()
            }
        }
    }
}
