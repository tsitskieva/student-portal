package com.example.studentportal.data.model

data class Module(
    val title: String,
    val submodules: List<Submodule>,
    val type: String? = null
)