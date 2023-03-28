package com.flaxstudio.drawon.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.utils.CustomDateTime
import com.flaxstudio.drawon.utils.FragmentType
import com.flaxstudio.drawon.utils.Project

class FavouriteRecyclerViewAdapter(private val contextApp: Context): RecyclerView.Adapter<FavouriteRecyclerViewAdapter.RecyclerViewHolder>() {
    private val projectsData = ArrayList<Project>()
    private var itemClickListener:((position:Int, project: Project)->Unit)? = null
    private var itemFavClickListener:((position:Int, project: Project)->Unit)? = null
    private var projectDeleteListener:((position:Int, project: Project)->Unit)? = null
    private val customDateTime = CustomDateTime()

    var longPressSelectedView: RelativeLayout? = null                   // null means no selection

    fun setOnClickListener(callback:(position:Int, project: Project)->Unit){
        itemClickListener = callback
    }

    fun setOnFavClickListener(callback:(position:Int, project: Project)->Unit){
        itemFavClickListener = callback
    }

    fun setOnProjectDeleteListener(callback:(position:Int, project: Project)->Unit){
        projectDeleteListener = callback
    }

    fun addProject(project: Project){
        projectsData.add(project)
    }

    fun removeProject(project: Project){
        projectsData.remove(project)
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_recyclerview_item, parent, false)

        return RecyclerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return projectsData.size
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        // binds the list items to a view
        val project = projectsData[position]

        holder.dateTimeView.text = customDateTime.getDateTimeInWord(project.lastModified)
        holder.nameView.text = project.projectName
        holder.favButton.isChecked = project.isFavourite

        val path = "${contextApp.filesDir}/${project.projectBitmapId}.png"

        Glide.with(contextApp)
            .load(path)
            .error(R.drawable.not_found_image)
            .into(holder.imageView)
    }

    inner class RecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.cardImage)
        val favButton: CheckBox = itemView.findViewById(R.id.cardFav)
        val nameView: TextView = itemView.findViewById(R.id.cardName)
        val dateTimeView: TextView = itemView.findViewById(R.id.cardDate)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.cardDetail)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if(position != RecyclerView.NO_POSITION){
                    itemClickListener?.invoke(position, projectsData[position])
                }
            }

            // delete
            itemView.findViewById<ImageButton>(R.id.buttonDelete).setOnClickListener {
                val position = adapterPosition
                if(position!=RecyclerView.NO_POSITION){
                    projectDeleteListener?.invoke(position, projectsData[position])
                }
            }

            // long click
            itemView.setOnLongClickListener {

                // clear previous selected project
                if(longPressSelectedView != null){
                    longPressSelectedView!!.visibility = View.INVISIBLE
                }

                // set new project to long press
                longPressSelectedView = itemView.findViewById(R.id.buttonDeleteParent)
                longPressSelectedView!!.visibility = View.VISIBLE
                return@setOnLongClickListener true
            }

            itemView.findViewById<CheckBox>(R.id.cardFav).setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if(position!=RecyclerView.NO_POSITION){
                    projectsData[position].isFavourite = isChecked
                    itemFavClickListener?.invoke(position, projectsData[position])
                }
            }
        }
    }
}