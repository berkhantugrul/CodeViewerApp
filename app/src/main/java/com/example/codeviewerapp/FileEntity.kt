package com.example.codeviewerapp

data class FileEntity(
    val id: Long,
    val fileName: String,
    val fileSize: Int,
    val filePath: String,
    val fileExtension: String
)
