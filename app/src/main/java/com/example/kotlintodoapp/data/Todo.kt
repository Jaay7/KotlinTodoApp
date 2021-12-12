package com.example.kotlintodoapp.data

data class Todo(
    val todoTitle: String,
    val todoContent: String,
    val time: String,
    val isCompleted: Boolean,
    val remaind: Boolean,
) {
    constructor() : this("", "", "", false, false)
}
