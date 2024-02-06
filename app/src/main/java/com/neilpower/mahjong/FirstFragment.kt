package com.neilpower.mahjong

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.neilpower.mahjong.databinding.FragmentFirstBinding
import java.util.function.ToDoubleFunction
import java.util.stream.Stream

//To do
//breaks with 5+ tiles for kong
//add pungs
//add winds and seasons


//GLOBAL VARIABLES ---------------------------------------------------------------------------------
private var selectedTileNumber = 0
private var selectedTileList: MutableList<String> = ArrayList()

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

//INITIALISATION FUNCTIONS----------------------------------------------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        //Set up binding on creation
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Set score text to 0
        val scoreText = getString(R.string.score, 0)
        updateText(R.id.score_text,scoreText)
        //val scoreDisplay: TextView = requireView().findViewById(R.id.score_text)

        //scoreDisplay.text = scoreText


        //Calculate score
        binding.calculateScoreButton.setOnClickListener {
            if (selectedTileList.count() == 11){
                calculateScore()
            }else{
                updateText(R.id.score_text,"Select 11 tiles")
            }

            //findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        // Reset everything
        binding.clearButton.setOnClickListener {
            clearAll()
        }

        //Loop through all buttons in tile selection table and set event listeners
        val tileTable: TableLayout = view.findViewById(R.id.tile_selection)

        for (i in 0 until tileTable.childCount) {
            val tileRow = tileTable.getChildAt(i) as TableRow
            for (j in 0 until tileRow.childCount) {
                val imageView = tileRow.getChildAt(j) as ImageView
                setClickListenerForImage(imageView)
            }
        }
    }

    override fun onDestroyView() {
        clearAll()
        super.onDestroyView()
        _binding = null
    }

    //EVENT FUNCTIONS-------------------------------------------------------------------------------
    private fun updateSelected(tileClicked: ImageView) {
        //Update tileToUpdate with image of tile clicked

        val selectedTileRow: TableRow = requireView().findViewById(R.id.selected_row)

        if (selectedTileNumber < selectedTileRow.childCount) {
            val tileToUpdate: ImageView = selectedTileRow.getChildAt(selectedTileNumber) as ImageView
            tileToUpdate.setImageDrawable(tileClicked.drawable)
            tileToUpdate.setBackgroundResource(R.drawable.tile_front)

            selectedTileNumber += 1

            val clickedTileId = tileClicked.id
            val clickedTileName = resources.getResourceEntryName(clickedTileId)
            selectedTileList.add(clickedTileName)

            //Add name of clicked tile to selection display
            updateTextNewline(R.id.selectionDisplay,clickedTileName)
        }
    }

    private fun clearAll(){
        //Clear all

        //Reset all selection tiles to blank
        val selectedTileRow: TableRow = requireView().findViewById(R.id.selected_row)
        for (j in 0 until selectedTileRow.childCount) {
            val tileToClear = selectedTileRow.getChildAt(j) as ImageView
            tileToClear.setImageResource(R.drawable.tile_front)
            tileToClear.setBackgroundResource(R.drawable.tile_front)
        }

        //Reset selected tile count to 0, clear list of selected tiles
        selectedTileNumber = 0
        selectedTileList.clear()

        //Reset selected tiles list
        updateText(R.id.selectionDisplay,"Selected Tiles:" )

        //Set score text to 0
        val scoreText = getString(R.string.score, 0)
        updateText(R.id.score_text,scoreText)

        updateTextNewline(R.id.scorerDisplay, "Points scored by:")
    }

    //UTILITY FUNCTIONS-----------------------------------------------------------------------------
    private fun updateText(textID: Int, newText: String){
        //Replaces text in textbox
        val textview: TextView = requireView().findViewById(textID)
        textview.text = newText
    }

    private fun updateTextNewline(textID: Int, newTextLine: String){
        //Appends text in textbox
        val textview: TextView = requireView().findViewById(textID)
        val newText  = getString(R.string.concatenated_newline, textview.text, newTextLine)
        textview.text = newText
    }

    private fun setClickListenerForImage(imageView: ImageView) {
        //Set listener for a view
        imageView.setOnClickListener {
            updateSelected(imageView)
        }
    }

    private fun argmax(list: ArrayList<Long>): Int? {
        //Returns index of max number
        var max: Long? = null
        var argmax: Int? = null
        for (i in 0 until list.count()){
            val item :Long = list[i]
            if (max == null || item > max) {
                max = item
                argmax = i
            }
        }
        return argmax
    }

    private fun <T> countUniqueElements(list: MutableList<T>): Map<T, Int> {
        val elementCountMap = mutableMapOf<T, Int>()

        // Count occurrences of each element in the list
        for (element in list) {
            if (elementCountMap.containsKey(element)) {
                elementCountMap[element] = elementCountMap[element]!! + 1
            } else {
                elementCountMap[element] = 1
            }
        }

        return elementCountMap
    }


//    private fun <T> countUniqueElements(list: MutableList<T>): Map<T, Pair<Int, MutableList<Int>>> {
//        val elementCountMap = mutableMapOf<T, Pair<Int, MutableList<Int>>>()
//
//        // Count occurrences of each element in the list and record their positions
//        for ((index, element) in list.withIndex()) {
//            if (elementCountMap.containsKey(element)) {
//                val (count, positions) = elementCountMap[element]!!
//                elementCountMap[element] = Pair(count + 1, positions.apply { add(index) })
//            } else {
//                elementCountMap[element] = Pair(1, mutableListOf(index))
//            }
//        }
//        return elementCountMap
//    }


    //CALCULATING SCORE FUNCTIONS-------------------------------------------------------------------
    private fun calculateScore() {

        //change so selected tiles are counted up on click
        //Calculates score from array of selected tiles
        var score = 0


        // Check for kongs
        var uniqueElementsCount = countUniqueElements(selectedTileList)
        //List of names, counts
       // var uniqueTiles = uniqueElementsCount.keys
        // counts = uniqueElementsCount.values


        //CHECK FOR KONGS ------------------------------------------------------
        //While there are 4 or more of the same tiles
        while (uniqueElementsCount.values.max()==4) {
            for (uniqueTile in uniqueElementsCount) { //Run through all tiles
                if (uniqueTile.value == 4) { //If count is greater than 4

                    score += 4 //Update score and score text
                    val scorerText = getString(R.string.scorer, "kong", uniqueTile.key)
                    updateTextNewline(R.id.scorerDisplay, scorerText)

                    //Remove all tiles that are part of a kong
                    selectedTileList.removeAll(listOf(uniqueTile.key))
                    uniqueElementsCount = countUniqueElements(selectedTileList)
                }
            }
        }

        //CHECK FOR PUNGS ------------------------------------------------------
        //While there are 3 or more of the same tiles
        while (uniqueElementsCount.values.max()==3) {
            for (uniqueTile in uniqueElementsCount) { //Run through all tiles
                if (uniqueTile.value == 3) { //If count is greater than 4

                    score += 2 //Update score and score text
                    val scorerText = getString(R.string.scorer, "pung", uniqueTile.key)
                    updateTextNewline(R.id.scorerDisplay, scorerText)

                    //Remove all tiles that are part of a kong
                    selectedTileList.removeAll(listOf(uniqueTile.key))
                    uniqueElementsCount = countUniqueElements(selectedTileList)
                }
            }
        }


        //Check for pungs

        //

        val scoreDisplay: TextView = requireView().findViewById(R.id.score_text)
        val scoreText = getString(R.string.score, score)
        scoreDisplay.text = scoreText
    }


}