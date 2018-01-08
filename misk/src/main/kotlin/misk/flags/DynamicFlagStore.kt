package misk.flags

import java.util.concurrent.TimeUnit

/**
 * Applications register their interest in monitoring flags through one of the registerXXXFlag
 * calls. If registration and watching requires contacting a remote service (e.g. a flag service,
 * Zookeeper, etc), this should be done asynchronously so that multiple flags can be registered
 * in parallel, with the XXXFlag.get() calls blocking until the registration for that flag is
 * complete. An application can also call [awaitRegistrationsComplete] to block until all
 * pending registrations have completed, allowing e.g. the application to avoid processing
 * incoming requests until all of its flags are available and watched
 */
interface DynamicFlagStore {
    fun registerBooleanFlag(name: String, description: String): BooleanFlag
    fun registerStringFlag(name: String, description: String): StringFlag
    fun registerIntFlag(name: String, description: String): IntFlag
    fun registerDoubleFlag(name: String, description: String): DoubleFlag

    /** Blocks until all pending registrations have completed */
    fun awaitRegistrationsComplete(timeout: Long, unit: TimeUnit)
}