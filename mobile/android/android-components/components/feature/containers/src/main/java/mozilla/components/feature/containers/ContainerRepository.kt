/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package mozilla.components.feature.containers

import android.content.Context
import android.util.AtomicFile
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import mozilla.appservices.fxcontainers.ContainersCallback
import mozilla.appservices.fxcontainers.ContainersStore
import mozilla.appservices.fxcontainers.InitException
import mozilla.appservices.fxcontainers.InternalException
import mozilla.appservices.fxcontainers.StoreException
import mozilla.components.browser.state.state.ContainerState
import mozilla.components.support.base.log.logger.Logger
import org.json.JSONException
import org.json.JSONObject

/** A repository for reading and modifying the user's containers. */
interface ContainerRepository {
    /** A [Flow] of the public containers, in the order the user arranged them. */
    val containersFlow: Flow<List<ContainerState>>

    /**
     * Creates a new container.
     *
     * @param name The name of the container. It must not be blank.
     * @param icon The [ContainerState.Icon] of the container.
     * @param color The [ContainerState.Color] of the container.
     * @return The created [ContainerState], or null if it could not be created.
     */
    suspend fun createContainer(name: String, icon: ContainerState.Icon, color: ContainerState.Color): ContainerState?

    /**
     * Updates an existing container. Renaming a default container gives it a [ContainerState.Label.Custom] label.
     *
     * @param contextId The [ContainerState.contextId] of the container to update.
     * @param name The new name of the container. It must not be blank.
     * @param icon The new [ContainerState.Icon] of the container.
     * @param color The new [ContainerState.Color] of the container.
     * @return The updated [ContainerState], or null if there is no such container or it could not be updated.
     */
    suspend fun updateContainer(
        contextId: String,
        name: String,
        icon: ContainerState.Icon,
        color: ContainerState.Color,
    ): ContainerState?

    /**
     * Removes a container. Closing its tabs and clearing its data are the caller's responsibility.
     *
     * @param contextId The [ContainerState.contextId] of the container to remove.
     * @return The removed [ContainerState], or null if there is no such container.
     */
    suspend fun removeContainer(contextId: String): ContainerState?

    /**
     * Moves the given containers before [position], or to the end when [position] is -1.
     *
     * @param contextIds The [ContainerState.contextId]s of the containers to move.
     * @param position The index to move the containers to, or -1 to move them to the end.
     */
    suspend fun moveContainers(contextIds: List<String>, position: Int)
}

/**
 * The default [ContainerRepository], backed by the application-services containers store shared with Firefox
 * Desktop. The store is seeded with the default containers on first use and persisted to `containers.json` in the
 * application's files directory.
 *
 * Like Firefox Desktop, stored containers that cannot be parsed are replaced with the default containers. The
 * context IDs of the replaced containers can be reused by containers created afterwards, so [onContainersReset] has
 * to close the tabs of those containers and clear their data.
 *
 * @param context The application [Context].
 * @param ioDispatcher The [CoroutineDispatcher] used to read and write the store.
 * @param onContainersReset Invoked on the [ioDispatcher] when the stored containers were replaced with the default
 *   containers, with the context IDs of the containers they had, as far as they could be recovered.
 */
class DefaultContainerRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val onContainersReset: (staleContextIds: Set<String>) -> Unit = {},
) : ContainerRepository {
    private val logger = Logger("DefaultContainerRepository")
    private val file = AtomicFile(File(context.filesDir, CONTAINERS_FILE_NAME))
    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())
    private val writeMutex = Mutex()

    @Volatile private var canPersist = true
    private val containers = MutableStateFlow<List<ContainerState>?>(null)
    private val store = scope.async(start = CoroutineStart.LAZY) { openStore() }

    override val containersFlow: Flow<List<ContainerState>> =
        containers.onStart { store.await() }.filterNotNull()

    override suspend fun createContainer(
        name: String,
        icon: ContainerState.Icon,
        color: ContainerState.Color,
    ): ContainerState? =
        mutate { store ->
            store.create(name, icon.toContainerIcon(), color.toContainerColor()).toContainerState()
        }

    override suspend fun updateContainer(
        contextId: String,
        name: String,
        icon: ContainerState.Icon,
        color: ContainerState.Color,
    ): ContainerState? {
        val userContextId = contextId.toUserContextId() ?: return null
        return mutate { store ->
            store.update(userContextId, name, icon.toContainerIcon(), color.toContainerColor())?.toContainerState()
        }
    }

    override suspend fun removeContainer(contextId: String): ContainerState? {
        val userContextId = contextId.toUserContextId() ?: return null
        return mutate { store -> store.remove(userContextId)?.toContainerState() }
    }

    override suspend fun moveContainers(contextIds: List<String>, position: Int) {
        mutate { store ->
            store.moveContainers(contextIds.mapNotNull { it.toUserContextId() }, position.toLong())
        }
    }

    private suspend fun <T> mutate(block: (ContainersStore) -> T): T? =
        withContext(ioDispatcher) {
            try {
                val store = store.await()
                try {
                    block(store)
                } finally {
                    refresh(store)
                }
            } catch (e: StoreException) {
                logger.warn("Failed to modify the containers", e)
                null
            } catch (e: InternalException) {
                logger.error("Failed to modify the containers", e)
                null
            }
        }

    private fun openStore(): ContainersStore {
        val callback =
            object : ContainersCallback {
                override fun persist() {
                    scope.launch { write() }
                }
            }

        val bytes =
            try {
                file.readFully()
            } catch (_: FileNotFoundException) {
                null
            } catch (e: IOException) {
                logger.warn("Failed to read the containers, keeping them in memory only", e)
                canPersist = false
                null
            }

        val store =
            try {
                ContainersStore(bytes, null, callback)
            } catch (e: InitException) {
                logger.warn("Failed to parse the containers, resetting them to the defaults", e)
                ContainersStore(null, null, callback).also {
                    onContainersReset(bytes?.let(::recoverContextIds).orEmpty())
                }
            }

        refresh(store)
        return store
    }

    private fun refresh(store: ContainersStore) {
        containers.value = store.publicIdentities().map { it.toContainerState() }
    }

    private suspend fun write() {
        if (!canPersist) return

        val store = store.await()
        writeMutex.withLock {
            var stream: FileOutputStream? = null
            try {
                stream = file.startWrite()
                stream.write(store.serialize())
                file.finishWrite(stream)
            } catch (e: IOException) {
                stream?.let { file.failWrite(it) }
                logger.warn("Failed to write the containers", e)
            }
        }
    }

    private companion object {
        const val CONTAINERS_FILE_NAME = "containers.json"
    }
}

private const val MAX_RECOVERED_ID_RANGE = 1000L

/**
 * Recovers what it can of the context IDs of the public containers in an unparsable containers document: the IDs of
 * its public identities, and every ID up to its `lastUserContextId` since IDs are handed out in sequence, capped to
 * [MAX_RECOVERED_ID_RANGE]. Returns an empty set when the document is not even JSON.
 */
private fun recoverContextIds(bytes: ByteArray): Set<String> {
    val document =
        try {
            JSONObject(String(bytes, Charsets.UTF_8))
        } catch (_: JSONException) {
            return emptySet()
        }

    val ids = mutableSetOf<Long>()
    val identities = document.optJSONArray("identities")
    for (i in 0 until (identities?.length() ?: 0)) {
        val identity = identities?.optJSONObject(i) ?: continue
        if (identity.optBoolean("public", true)) {
            ids += identity.optLong("userContextId")
        }
    }
    ids += 1L..document.optLong("lastUserContextId").coerceAtMost(MAX_RECOVERED_ID_RANGE)

    return ids.filter { it in 1L until UInt.MAX_VALUE.toLong() }.mapTo(mutableSetOf()) { it.toUInt().toContextId() }
}
