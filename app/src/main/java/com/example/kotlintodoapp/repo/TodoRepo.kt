package com.example.kotlintodoapp.repo

import android.annotation.SuppressLint
import com.example.kotlintodoapp.OnError
import com.example.kotlintodoapp.OnFailed
import com.example.kotlintodoapp.OnSend
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

    @SuppressLint("SimpleDateFormat")
    fun getCurrentDate():String{
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
        return sdf.format(Date())
    }

    fun sendTodoDetails(title: String, content: String ) {
        val collection = firestore.collection("todos")
        val date = getCurrentDate()
        val todo = Todo(title, content, date, false, false)
//        collection.add(todo)
        collection.add(todo).addOnSuccessListener { documentReference ->
            OnSend(documentReference)
        }
            .addOnFailureListener { e ->
                OnFailed(e)
            }
    }
}