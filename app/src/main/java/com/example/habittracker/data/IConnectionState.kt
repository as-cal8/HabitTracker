package com.example.habittracker.data

sealed interface IConnectionState{
    object Connected: IConnectionState
    object Disconnected: IConnectionState
    object Uninitialized: IConnectionState
    object CurrentlyInitializing: IConnectionState
}