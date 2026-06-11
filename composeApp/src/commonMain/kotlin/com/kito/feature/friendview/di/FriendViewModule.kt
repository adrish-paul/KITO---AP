package com.kito.feature.friendview.di

import com.kito.feature.friendview.data.FriendViewRepositoryImpl
import com.kito.feature.friendview.domain.repository.FriendViewRepository
import com.kito.feature.friendview.presentation.FriendViewViewmodel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val friendViewModule = module {
    single<FriendViewRepositoryImpl>() bind FriendViewRepository::class
    single<FriendViewViewmodel>()
}
