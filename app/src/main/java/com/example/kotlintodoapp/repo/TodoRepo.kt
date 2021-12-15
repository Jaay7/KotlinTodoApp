package com.example.kotlintodoapp.repo

import android.annotation.SuppressLint
import com.example.kotlintodoapp.OnError

import com.example.kotlintodoapp.OnSuccess
import com.example.kotlintodoapp.data.Todo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.text.SimpleDateFormat
import java.util.*

class TodoRepo {
    private val firestore = FirebaseFirestore.getInstance()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTodosDetails() = callbackFlow {

        val collection = firestore.collection("todos")
        val snapshotListener = collection.addSnapshotListener { value, error ->
            val response = if (error == null) {
                OnSuccess(value)
            } else {
                OnError(error)
            }

            offer(response)
        }

        awaitClose {
            snapshotListener.remove()
        }
    }

}