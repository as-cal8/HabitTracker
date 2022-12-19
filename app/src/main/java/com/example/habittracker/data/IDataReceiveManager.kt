package com.example.habittracker.data

import com.example.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow

interface IDataReceiveManager {

    val data: MutableSharedFlow<Resource<CurrentData>>
    fun reconnect()
    fun disconnect()
    fun startReceiving()
    fun closeConnection()
}