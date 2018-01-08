package misk.flags.memory

import misk.flags.DynamicFlagStore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/** In memory implementation of a [DynamicFlagStore], suitable for use in testing */
@Singleton
class InMemoryDynamicFlagStore : DynamicFlagStore {
    private val _booleanFlags = ConcurrentHashMap<String, InMemoryBooleanFlag>()
    private val _stringFlags = ConcurrentHashMap<String, InMemoryStringFlag>()
    private val _intFlags = ConcurrentHashMap<String, InMemoryIntFlag>()
    private val _doubleFlags = ConcurrentHashMap<String, InMemoryDoubleFlag>()

    val booleanFlags: Map<String, InMemoryBooleanFlag> get() = _booleanFlags
    val intFlags: Map<String, InMemoryIntFlag> get() = _intFlags
    val stringFlags: Map<String, InMemoryStringFlag> get() = _stringFlags
    val doubleFlags: Map<String, InMemoryDoubleFlag> get() = _doubleFlags

    override fun registerBooleanFlag(name: String, description: String): InMemoryBooleanFlag {
        val newFlag = InMemoryBooleanFlag(name, description)
        return _booleanFlags.putIfAbsent(name, newFlag) ?: newFlag
    }

    override fun registerStringFlag(name: String, description: String): InMemoryStringFlag {
        val newFlag = InMemoryStringFlag(name, description)
        return _stringFlags.putIfAbsent(name, newFlag) ?: newFlag
    }

    override fun registerIntFlag(name: String, description: String): InMemoryIntFlag {
        val newFlag = InMemoryIntFlag(name, description)
        return _intFlags.putIfAbsent(name, newFlag) ?: newFlag
    }

    override fun registerDoubleFlag(name: String, description: String): InMemoryDoubleFlag {
        val newFlag = InMemoryDoubleFlag(name, description)
        return _doubleFlags.putIfAbsent(name, newFlag) ?: newFlag
    }

    override fun awaitRegistrationsComplete(timeout: Long, unit: TimeUnit) {}
}