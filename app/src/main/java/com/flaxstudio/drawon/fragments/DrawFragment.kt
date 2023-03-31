package com.flaxstudio.drawon.fragments

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.abhishek.colorpicker.ColorPickerDialog
import com.flaxstudio.drawon.ProjectApplication
import com.flaxstudio.drawon.ProjectExportActivity
import com.flaxstudio.drawon.R
import com.flaxstudio.drawon.databinding.FragmentDrawBinding
import com.flaxstudio.drawon.utils.ShapeType
import com.flaxstudio.drawon.utils.ToolProperties
import com.flaxstudio.drawon.viewmodels.MainActivityViewModel
import com.flaxstudio.drawon.viewmodels.MainActivityViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DrawFragment : Fragment() {

    private lateinit var binding: FragmentDrawBinding
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels {
        MainActivityViewModelFactory((requireActivity().application as ProjectApplication).repository)
    }
    private lateinit var contextApp: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // used to handle back press
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if(!binding.drawingView.isProjectSaved()){
                // ask user to save project by show dialog
                saveDialog.show()
            }else{
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDrawBinding.inflate(inflater, container, false)
        return binding.root
    }


    private lateinit var colorPickerDialog: ColorPickerDialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contextApp = requireContext()
        colorPickerDialog = ColorPickerDialog()
        addListeners()
        loadUiData()
    }


    private var isFillColorContainerSelected = false
    private var isColorPickerOpened = false

    private fun addListeners() {

        binding.clearAll.setOnClickListener {
            clearAllDialog.show()
        }

        // your text box
        binding.fileName.setOnEditorActionListener(OnEditorActionListener { view, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                val text = view.text.toString().trim()
                if(text.isBlank()){
                    Toast.makeText(contextApp, "Name must not be empty", Toast.LENGTH_SHORT).show()
                    return@OnEditorActionListener false
                }else{

                    mainActivityViewModel.renameProjectTask(text)
                    view.clearFocus()
                    // hide keyboard after done
                    val imm = contextApp.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    Toast.makeText(contextApp, "Renamed", Toast.LENGTH_SHORT).show()
                    return@OnEditorActionListener true
                }
            }
            false
        })

        // save project
        binding.saveButton.setOnClickListener {
            saveProject()
        }

        // favourite
        binding.favCheckBox.setOnCheckedChangeListener { _, isChecked ->
            mainActivityViewModel.openedProject.isFavourite = isChecked
            mainActivityViewModel.updateProjectTask(mainActivityViewModel.openedProject){
                // callback of update
            }
        }

        // toggle selected tools
        binding.toolbarGroup.addOnButtonCheckedListener { toggleGroup, _, _ ->

            when (toggleGroup.checkedButtonId) {
                R.id.toolBrush -> {
                    binding.drawingView.setSelectedTool(ShapeType.Brush)
                    updatePropertiesPanelData(ShapeType.Brush)
                }
                R.id.toolRect -> {
                    binding.drawingView.setSelectedTool(ShapeType.Rectangle)
                    updatePropertiesPanelData(ShapeType.Rectangle)
                }
                R.id.toolLine -> {
                    binding.drawingView.setSelectedTool(ShapeType.Line)
                    updatePropertiesPanelData(ShapeType.Line)
                }
                R.id.toolOval -> {
                    binding.drawingView.setSelectedTool(ShapeType.Oval)
                    updatePropertiesPanelData(ShapeType.Oval)
                }
                R.id.toolTriangle -> {
                    binding.drawingView.setSelectedTool(ShapeType.Triangle)
                    updatePropertiesPanelData(ShapeType.Triangle)
                }
                R.id.toolEraser -> {
                    binding.drawingView.setSelectedTool(ShapeType.Eraser)
                    updatePropertiesPanelData(ShapeType.Eraser)
                }
            }
        }

        // show and hide properties panel with animation
        binding.toolProperties.setOnClickListener {
            togglePropertiesPanel()
        }


        // color picker dialog
        colorPickerDialog.setOnOkCancelListener { isOk, color ->

            isColorPickerOpened = false
            // setting color
            if (isOk) {
                if (isFillColorContainerSelected) {
                    tempToolProperties!!.fillColor = color

                    // fill
                    binding.fillColorView.setColor(tempToolProperties!!.fillColor)
                    binding.fillColorText.text = colorToHex(tempToolProperties!!.fillColor)

                } else {
                    tempToolProperties!!.strokeColor = color

                    // stroke
                    binding.strokeColorView.setColor(tempToolProperties!!.strokeColor)
                    binding.strokeColorText.text = colorToHex(tempToolProperties!!.strokeColor)
                }

                binding.drawingView.updateToolData(tempToolProperties!!)
            }
        }

        // properties panel listeners
        binding.strokeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (tempToolProperties == null) return
                if (seekBar == null) return
                tempToolProperties!!.strokeWidth = seekBar.progress.toFloat()
                binding.drawingView.updateToolData(tempToolProperties!!)
            }

            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.strokeTextView.text = progress.toString() + "px"
            }

        })

        binding.strokeColorContainer.setOnClickListener {
            if(isColorPickerOpened) return@setOnClickListener
            isColorPickerOpened = true
            isFillColorContainerSelected = false
            colorPickerDialog.show(parentFragmentManager)
        }

        binding.fillColorContainer.setOnClickListener {
            if(isColorPickerOpened) return@setOnClickListener
            isColorPickerOpened = true
            isFillColorContainerSelected = true
            colorPickerDialog.show(parentFragmentManager)
        }


        // redo and undo
        binding.undoButton.setOnClickListener {
            binding.drawingView.undo()
            updateDrawingCanvas()
        }

        binding.redoButton.setOnClickListener {
            binding.drawingView.redo()
            updateDrawingCanvas()
        }

        binding.drawingView.setUndoRedoListener { isUndoVisible, isRedoVisible ->
            if (isUndoVisible) binding.undoButton.visibility =
                View.VISIBLE else binding.undoButton.visibility = View.INVISIBLE
            if (isRedoVisible) binding.redoButton.visibility =
                View.VISIBLE else binding.redoButton.visibility = View.INVISIBLE
        }

        // project export
        binding.exportButton.setOnClickListener {


            mainActivityViewModel.saveBitmapTask(contextApp, binding.drawingView.getCanvasBitmap(),  "export-bitmap"){
                val intent = Intent(activity, ProjectExportActivity::class.java)
                intent.putExtra("project-name", mainActivityViewModel.openedProject.projectName)
                startActivity(intent)
            }
        }
    }

    private var tempToolProperties: ToolProperties? = null
    private fun updatePropertiesPanelData(toolType: ShapeType) {

        tempToolProperties = binding.drawingView.getSelectedToolProp(toolType) ?: return

        binding.selectedTool.text = toolType.toString()
        binding.strokeColorParent.visibility = View.VISIBLE

        when (toolType) {
            ShapeType.Brush -> {
                binding.fillColorParent.visibility = View.GONE

            }
            ShapeType.Eraser -> {
                binding.fillColorParent.visibility = View.GONE
                binding.strokeColorParent.visibility = View.GONE
            }
            ShapeType.Line -> {
                binding.fillColorParent.visibility = View.GONE
            }
            ShapeType.Oval -> {
                binding.fillColorParent.visibility = View.VISIBLE
            }
            ShapeType.Rectangle -> {
                binding.fillColorParent.visibility = View.VISIBLE
            }
            ShapeType.Triangle -> {
                binding.fillColorParent.visibility = View.VISIBLE
            }
        }

        // stroke
        binding.strokeColorView.setColor(tempToolProperties!!.strokeColor)
        binding.strokeColorText.text = colorToHex(tempToolProperties!!.strokeColor)

        // fill
        binding.fillColorView.setColor(tempToolProperties!!.fillColor)
        binding.fillColorText.text = colorToHex(tempToolProperties!!.fillColor)

        // width
        binding.strokeSeekBar.progress = tempToolProperties!!.strokeWidth.toInt()

    }

    private fun togglePropertiesPanel() {

        if(binding.propertiesPanel.visibility == View.VISIBLE){     // panel close animation

            val animator = binding.propertiesPanel.animate().translationY(resources.getDimension(R.dimen.panel_translate)).alpha(0f).setDuration(200)
            animator.setListener(object: AnimatorListenerAdapter(){
                override fun onAnimationEnd(animation: Animator) {
                    binding.propertiesPanel.visibility = View.INVISIBLE
                }
            })

        }else{                                                      // panel open animation
            binding.propertiesPanel.alpha = 0f
            binding.propertiesPanel.translationY = resources.getDimension(R.dimen.panel_translate)
            val animator = binding.propertiesPanel.animate().translationY(0f).alpha(1f).setDuration(200)
            animator.setListener(object: AnimatorListenerAdapter(){
                override fun onAnimationStart(animation: Animator) {
                    binding.propertiesPanel.visibility = View.VISIBLE
                }
            })
        }
    }

    private fun colorToHex(colorInt: Int): String {
        // hex color
        return String.format("#%06X", (0xFFFFFF and colorInt))
    }


    // ui update work here
    private fun loadUiData() {

        setupSaveDialog()
        setupClearAllDialog()

        mainActivityViewModel.loadProjectDataTask(contextApp){ toolsData ->
            if (toolsData != null) {
                binding.drawingView.setToolData(toolsData)
            }

            binding.fileName.setText(mainActivityViewModel.openedProject.projectName)
            binding.favCheckBox.isChecked = mainActivityViewModel.openedProject.isFavourite

            // loading saved bitmap
            mainActivityViewModel.getBitmapTask(contextApp,  mainActivityViewModel.openedProject.projectBitmapId){ bitmap ->
                val width = mainActivityViewModel.openedProject.whiteboardWidth
                val height = mainActivityViewModel.openedProject.whiteboardHeight
                binding.drawingView.setWhiteBoardSize(width, height)
                binding.drawingView.setProjectSavedBitmap(bitmap)

            }
            // default selected tool
            updatePropertiesPanelData(ShapeType.Rectangle)
        }
    }

    private fun updateDrawingCanvas() {
        lifecycleScope.launch(Dispatchers.Default) {
            binding.drawingView.reDraw()
            withContext(Dispatchers.Main) {
                binding.drawingView.invalidate()
            }
        }
    }


    private fun saveProject(){
        // saving project
        mainActivityViewModel.saveProjectTask(
            contextApp,
            binding.drawingView.getCanvasBitmap(),
            binding.drawingView.getToolData()
        ){
            binding.drawingView.setProjectSaved()
            Toast.makeText(contextApp, "Saved", Toast.LENGTH_SHORT).show()

            // back to home
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }


    // save alert dialog box

    private lateinit var saveDialog: AlertDialog
    private fun setupSaveDialog(){

        val alertBuilder = AlertDialog.Builder(requireContext())
        val customAlertBox : View = layoutInflater.inflate(R.layout.save_alert_box , null)
        alertBuilder.setView(customAlertBox)
        saveDialog = alertBuilder.create()
        saveDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        saveDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        customAlertBox.findViewById<Button>(R.id.cancel_btn).setOnClickListener {

            // closing without saving
            binding.drawingView.setProjectSaved()
            saveDialog.dismiss()

            // back to home
            requireActivity().onBackPressedDispatcher.onBackPressed()

        }
        customAlertBox.findViewById<Button>(R.id.btn_save).setOnClickListener {
            // saving project before closing
            saveProject()
            saveDialog.dismiss()
        }
    }

    private lateinit var clearAllDialog: AlertDialog
    private fun setupClearAllDialog(){

        val alertBuilder = AlertDialog.Builder(requireContext())
        val customAlertBox : View = layoutInflater.inflate(R.layout.clear_canvas_alert_box , null)
        alertBuilder.setView(customAlertBox)
        clearAllDialog = alertBuilder.create()
        clearAllDialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        clearAllDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        customAlertBox.findViewById<Button>(R.id.btn_clear_all).setOnClickListener {

            // clear the canvas
            binding.drawingView.clearCanvas()
            clearAllDialog.hide()

        }
        customAlertBox.findViewById<Button>(R.id.cancel_btn).setOnClickListener {
            clearAllDialog.hide()
        }
    }


}
