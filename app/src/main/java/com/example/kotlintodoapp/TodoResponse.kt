package com.example.kotlintodoapp

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.lang.Exception

sealed class TodoResponse
data class OnSuccess(val querySnapshot: QuerySnapshot?): TodoResponse()
data class OnError(val exception: FirebaseFirestoreException?): TodoResponse()
data class OnSend(val documentReference: DocumentReference?): TodoResponse()
data class OnFailed(val error: Exception?): TodoResponse()
