package com.example.kotlintodoapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlintodoapp.TodoResponse
import com.example.kotlintodoapp.repo.TodoRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TodosViewModel(val todoRepo: TodoRepo): ViewModel() {
    val todoStateFlow = MutableStateFlow<TodoResponse?>(null)

    init {
        viewModelScope.launch {
            todoRepo.getTodosDetails().collect {
                todoStateFlow.value = it
            }
        }
    }
    fun getTodosInfo() = todoRepo.getTodosDetails()
}