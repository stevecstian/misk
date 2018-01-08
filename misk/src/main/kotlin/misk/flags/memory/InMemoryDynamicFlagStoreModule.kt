package misk.flags.memory

import misk.flags.DynamicFlagStore
import misk.inject.KAbstractModule
import misk.inject.asSingleton

class InMemoryDynamicFlagStoreModule : KAbstractModule() {
    override fun configure() {
        bind<DynamicFlagStore>().to<InMemoryDynamicFlagStore>().asSingleton()
    }
}