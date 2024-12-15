package com.example.codeviewerapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FileAdapter(private val fileList: List<FileEntity>) : RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val file = fileList[position]
        holder.fileName.text = file.fileName
        holder.fileSize.text = "Size: ${file.fileSize} KB"
        holder.fileExtension.text = "Extension: ${file.fileExtension}"
        holder.filePath.text = "Path: ${file.filePath}"
    }

    override fun getItemCount(): Int = fileList.size

    class FileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)
        val fileSize: TextView = view.findViewById(R.id.fileSize)
        val fileExtension: TextView = view.findViewById(R.id.fileExtension)
        val filePath: TextView = view.findViewById(R.id.filePath)
    }
}
