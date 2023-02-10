package com.flaxstudio.drawon.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.utils.Project

class HomeRecyclerViewAdapter(private val context: Context) : RecyclerView.Adapter<HomeRecyclerViewAdapter.RecyclerViewHolder>() {

    private val projectsData = ArrayList<Project>()
    private var itemClickListener:((position:Int, project: Project)->Unit)? = null

    fun setOnClickListener(callback:(position:Int, project: Project)->Unit){
        itemClickListener = callback
    }
    fun addProject(project: Project){
        projectsData.add(project)
    }

    fun clearProjects(){
        projectsData.clear()

        // Note: the below project is just to add 1 more element for creating new project only
        val tempPro = Project(0,"","",false,"",0,0)
        projectsData.add(tempPro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recyclerview_item, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        // binds the list items to a view
        val project = projectsData[position]

        // 0 index of array is reserved for creating new project only
        if(position == 0){
            holder.linearLayout.visibility = View.INVISIBLE
            holder.favButton.visibility = View.INVISIBLE
            Glide.with(context)
                .load(R.drawable.icon_create_project)
                .into(holder.imageView)
            return
        }

        holder.dateTimeView.text
        holder.nameView.text = project.projectName
        holder.favButton.isChecked = project.isFavourite

        val path = "${context.filesDir}/${project.projectId}.png"
        Glide.with(context)
            .load(path)
            .error(R.drawable.not_found_image)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return projectsData.size
    }


    inner class RecyclerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView: ImageView = itemView.findViewById(R.id.cardImage)
        val favButton: CheckBox = itemView.findViewById(R.id.cardFav)
        val nameView: TextView = itemView.findViewById(R.id.cardName)
        val dateTimeView: TextView = itemView.findViewById(R.id.cardDate)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.cardDetail)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if(position!=RecyclerView.NO_POSITION){
                    itemClickListener?.invoke(position, projectsData[position])
                }
            }
        }


    }
}