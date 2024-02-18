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

//To do
//Prevent user adding 5+ of the same tile
//chow of winds/dragons are currently allowed
//Add terminal multiplier
//seasons and flowers
//wind of the round
//click to remove
//terminal multiplier
//pairs shown as unused
//set loop to fill table with tiles (last)
//Add text to tiles


//VARIABLES ---------------------------------------------------------------------------------
private const val CHOWPOINTS = 0
private const val PUNGPOINTS = 2
private const val KONGPOINTS = 4
private const val SEASONPOINTS = 4
private const val FLOWERPOINTS = 4

private const val MAHJONGPOINTS = 50
private const val DRAGONMULTIPLIER = 2
private const val WINDMULTIPLIER = 2
private const val TERMINALMULTIPLIER = 2 //1s or 9s
private const val SAMESEASONMULTIPLIER = 2
private const val SAMEFLOWERMULTIPLIER = 2
private const val SAMEWINDMULTIPLIER = 2
private const val WINDROUNDMULTIPLIER = 2

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

        updateText(R.id.scorerDisplay, "Points scored by:")
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

    private fun extractNumber(str: String): Int {
        // Extract the last character and parse it as an integer
        return str.last().toString().toInt()
    }

    private fun extractSuit(str: String): String {
        // Extract the string up to _
        return str.split("_")[0]
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

    private fun findFirstConsecutiveRun(list: MutableList<String>): List<Int>? {
        var startIndex = 0
        var endIndex = 0

        while (endIndex < list.size) {
            // Check if the next two strings form a consecutive run of three numbers
            if (endIndex + 2 < list.size) { //If not at end of list
                if (extractNumber(list[endIndex]) + 1 == extractNumber(list[endIndex + 1]) &&
                    extractNumber(list[endIndex]) + 2 == extractNumber(list[endIndex + 2]) //If consecutive run
                ) {
                    if (extractSuit(list[endIndex]) != "dragon" && extractSuit(list[endIndex]) != "wind") { //If chow is not of dragon or wind
                        return (startIndex..endIndex + 2).toList()
                    }
                } else {
                    // Move to the next index
                    endIndex++
                    startIndex = endIndex
                }
            } else {
                // Move to the next index
                endIndex++
                startIndex = endIndex
            }
        }

        return null // No consecutive run of three found
    }

    //CALCULATING SCORE FUNCTIONS-------------------------------------------------------------------
    private fun calculateScore() {
        //Calculates score from array of selected tiles
        var score = 0
        var multiplier = 0
        //Reset score breakdown
        updateText(R.id.scorerDisplay, "Points scored by:")

        val hand = selectedTileList.toMutableList() //Create copy of selected tile list for scoring
        var uniqueElementsCount = countUniqueElements(hand) //Count unique elements


        //CHECK FOR KONGS ------------------------------------------------------
        //While there are 4 or more of the same tiles
        while (uniqueElementsCount.values.max()==4) {
            for (uniqueTile in uniqueElementsCount) { //Run through all tiles
                if (uniqueTile.value == 4) { //If count is greater than 4

                    score += if (extractSuit(uniqueTile.key)=="dragon"){
                        KONGPOINTS* DRAGONMULTIPLIER //KONG OF DRAGON
                    }else if(extractSuit(uniqueTile.key)=="wind"){
                        KONGPOINTS* WINDMULTIPLIER //KONG OF WIND
                    }else if(extractNumber(uniqueTile.key)== 1 || extractNumber(uniqueTile.key)==9){
                        KONGPOINTS* TERMINALMULTIPLIER //TERMINAL KONG
                    }else{
                        KONGPOINTS //STANDARD KONG
                    }
                    val scorerText = getString(R.string.scorer, "kong", uniqueTile.key)
                    updateTextNewline(R.id.scorerDisplay, scorerText)

                    //Remove all tiles that are part of a kong
                    hand.removeAll(listOf(uniqueTile.key))
                    uniqueElementsCount = countUniqueElements(hand)
                }
            }
        }

        //CHECK FOR PUNGS ------------------------------------------------------
        //While there are 3 or more of the same tiles
        while (uniqueElementsCount.values.max()==3) {
            for (uniqueTile in uniqueElementsCount) { //Run through all tiles
                if (uniqueTile.value == 3) { //If count is greater than 4

                    score += if (extractSuit(uniqueTile.key)=="dragon"){
                        PUNGPOINTS* DRAGONMULTIPLIER //PUNG OF DRAGON
                    }else if(extractSuit(uniqueTile.key)=="wind"){
                        PUNGPOINTS* WINDMULTIPLIER //PUNG OF WIND
                    }else if(extractNumber(uniqueTile.key)== 1 || extractNumber(uniqueTile.key)==9){
                        PUNGPOINTS* TERMINALMULTIPLIER //TERMINAL PUNG
                    }else{
                        PUNGPOINTS //STANDARD PUNG
                    }
                    val scorerText = getString(R.string.scorer, "pung", uniqueTile.key)
                    updateTextNewline(R.id.scorerDisplay, scorerText)

                    //Remove all tiles that are part of a kong
                    hand.removeAll(listOf(uniqueTile.key))
                    uniqueElementsCount = countUniqueElements(hand)
                }
            }
        }


        //CHECK FOR CHOWS ------------------------------------------------------

        hand.sort() //Sort hand
        var firstChow = findFirstConsecutiveRun(hand) //Get indexes of chows

        while (firstChow != null) {
            val chowTile = hand.elementAt(firstChow[0])

            score += CHOWPOINTS
            val scorerText = getString(R.string.scorer, "chow", extractSuit(chowTile))
            updateTextNewline(R.id.scorerDisplay, scorerText)

            hand.subList(firstChow[0],firstChow[2]+1).clear() //Remove chow from hand
            //ISSUE WITH MULTIPLE CHOWS?
            hand.sort() //Sort and search again
            firstChow = findFirstConsecutiveRun(hand)
        }

        //CHECK FOR FINAL PAIR (MAHJONG) ------------------------------------------------------

        if (hand.count()==2 && countUniqueElements(hand).keys.count()==1){
            score += MAHJONGPOINTS
            updateTextNewline(R.id.scorerDisplay, "MAHJONG")
        }
        updateTextNewline(R.id.scorerDisplay,"Unused tiles:"+ hand.toString())


        //CHECK FOR ALL 1s, 9s, all same suit -----------------------------------------

        //CHECK FOR MATCHING WINDS, FLOWERS, SEASONS -----------------------------------------

        //Update score
        val scoreText = getString(R.string.score, score)
        updateText(R.id.score_text,scoreText)
    }


}