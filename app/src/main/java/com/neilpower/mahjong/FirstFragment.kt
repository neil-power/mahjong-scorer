package com.neilpower.mahjong

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.neilpower.mahjong.databinding.FragmentFirstBinding

//To do
//Update selection to 11
//Add text to tiles
//set loop to fill table with tiles (last)

//Issues
//Clear doesn't clear selected flowers or seasons
//Issue with multiple chows of same suit - sorting issue (eg bamboo_1, bamboo_1, bamboo_2 ...)
//Issue when clicking on empty tile
//Issue when calculating score if no winds are clicked
//Doesn't add 1 tile if kong present


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
private const val WINDPLAYERMULTIPLIER = 2
private const val WINDROUNDMULTIPLIER = 2
private const val SAMESEASONMULTIPLIER = 2
private const val SAMEFLOWERMULTIPLIER = 2

private var selectedTileNumber = 0
private var selectedTileList: MutableList<String> = ArrayList()
private var flowerSeasonList: MutableList<String> = ArrayList()

private val windNames = listOf("East","South","West","North")
private val dragonNames = listOf("Red","Green","White")
private val flowerNames = listOf("Spring","Summer","Autumn","Winter")
private val seasonNames = listOf("Plum","Orchid","Chrysanthemum","Bamboo")

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
        updateText(R.id.score_text, scoreText)

        //Calculate score
        binding.calculateScoreButton.setOnClickListener {
            if (selectedTileList.count() == 11) {
                calculateScore()
            } else {
                updateText(R.id.score_text, "Select 11 tiles")
            }
        }

        // Reset everything
        binding.clearButton.setOnClickListener {
            clearAll()
        }

        //Loop through all buttons in tile selection table and set event listeners
        val tileTable: TableLayout = view.findViewById(R.id.tile_selection)

        for (i in 0 until tileTable.childCount) {
            val tileRow = tileTable.getChildAt(i) as TableRow
            if (i==0){
                for (j in 0 until tileRow.childCount) { //Ignore season and flower row
                    val imageView = tileRow.getChildAt(j) as ImageView
                    setFlowerSeasonClickListener(imageView)
                }
            }else{
                for (j in 0 until tileRow.childCount) {
                    val imageView = tileRow.getChildAt(j) as ImageView
                    setAddClickListener(imageView)
                }
            }

        }


        //Loop through all buttons in selected tile selection table and set event listeners
        val selectedTileRow: TableRow = view.findViewById(R.id.selected_row)
        for (i in 0 until selectedTileRow.childCount) {
            val imageView = selectedTileRow.getChildAt(i) as ImageView
            setRemoveClickListener(imageView)
        }

    }

    override fun onDestroyView() {
        clearAll()
        super.onDestroyView()
        _binding = null
    }

    //EVENT FUNCTIONS-------------------------------------------------------------------------------
    private fun selectTile(tileClicked: ImageView) {
        //Update tileToUpdate with image of tile clicked

        val selectedTileRow: TableRow = requireView().findViewById(R.id.selected_row)

        if (selectedTileNumber < selectedTileRow.childCount) {
            val clickedTileId = tileClicked.id
            val clickedTileName = resources.getResourceEntryName(clickedTileId)

            //Check selectedTileList for counts of tile clicked
            val numAlreadySelected = selectedTileList.count{it == clickedTileName}

            if ( (extractSuit(clickedTileName)=="flower" && numAlreadySelected ==0) || //Only same flower
                (extractSuit(clickedTileName)=="season" && numAlreadySelected ==0) || //Only same season
                ( (extractSuit(clickedTileName)!="flower" && (extractSuit(clickedTileName)!="season") && (numAlreadySelected <= 3) ) )){ //Max 4 of others



                val tileToUpdate: ImageView = selectedTileRow.getChildAt(selectedTileNumber) as ImageView
                tileToUpdate.setImageDrawable(tileClicked.drawable)
                tileToUpdate.setBackgroundResource(R.drawable.tile_front)

                selectedTileNumber += 1
                selectedTileList.add(clickedTileName)

                //Add name of clicked tile to selection display
                updateText(R.id.selectionDisplay,"Selected tiles:" + selectedTileList.toString())
            }
        }
    }

    private fun removeTile(tileClicked: ImageView) {
        //Remove tile from selection when clicked
        val clickedTileId = tileClicked.id
        val clickedTileName = resources.getResourceEntryName(clickedTileId)
        val clickedTileNumber = extractNumber(clickedTileName)
        val selectedTileRow: TableRow = requireView().findViewById(R.id.selected_row)

        //Remove tile from selectedList
        selectedTileList.removeAt(clickedTileNumber-1)
        selectedTileNumber -= 1

        //Shuffle all imageviews to right of clicked tile back
        for (i in clickedTileNumber - 1 until  selectedTileRow.childCount - 1) {
            val imageView: ImageView = selectedTileRow.getChildAt(i) as ImageView
            val nextImageView: ImageView = selectedTileRow.getChildAt(i + 1) as ImageView

            val drawable: Drawable? = nextImageView.drawable
            imageView.setImageDrawable(drawable)
        }

        val finalTile = selectedTileRow.getChildAt(selectedTileRow.childCount-1) as ImageView
        finalTile.setImageResource(R.drawable.tile_front)
        finalTile.setBackgroundResource(R.drawable.tile_front)

        //Add name of clicked tile to selection display
        updateText(R.id.selectionDisplay,"Selected tiles:" + selectedTileList.toString())
    }

    private fun selectFlowerSeason(tileClicked: ImageView){
        val clickedTileId = tileClicked.id
        val clickedTileName = resources.getResourceEntryName(clickedTileId)

        val numAlreadySelected = flowerSeasonList.count{it == clickedTileName}

        if (numAlreadySelected == 0){ //if tile is already selected
            //highlight tile
            tileClicked.setBackgroundResource(R.drawable.tile_front_border)
            flowerSeasonList.add(clickedTileName) //add to list
        }else{
            //deselect tile
            tileClicked.setBackgroundResource(R.drawable.tile_front)
            flowerSeasonList.remove(clickedTileName)
            //remove from list
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

        //Reset all flower/season tiles
        val tileTable: TableLayout = requireView().findViewById(R.id.tile_selection)
        val flowerSeasonRow = tileTable.getChildAt(0) as TableRow
        for (i in 0 until flowerSeasonRow.childCount) {
            val tileToClear = selectedTileRow.getChildAt(i) as ImageView
            tileToClear.setBackgroundResource(R.drawable.tile_front)
        }
        flowerSeasonList.clear()

        //Reset selected tiles list
        updateText(R.id.selectionDisplay,"Selected Tiles:" )

        //Set score text to 0
        val scoreText = getString(R.string.score, 0)
        updateText(R.id.score_text,scoreText)

        updateText(R.id.scorerDisplay, "Points scored by:")

        //Reset winds - necessary?
        val roundCheck: RadioGroup = requireView().findViewById(R.id.wind_round)
        for (i in 1 until roundCheck.childCount) { //Note 1 to avoid textview
            val radioButton = roundCheck.getChildAt(i) as RadioButton
            radioButton.isChecked = false
        }

        val playerCheck: RadioGroup = requireView().findViewById(R.id.wind_player)
        for (i in 1 until playerCheck.childCount) { //Note 1 to avoid textview
            val radioButton = playerCheck.getChildAt(i) as RadioButton
            radioButton.isChecked = false
        }
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

    private fun setAddClickListener(imageView: ImageView) {

        //Set listener for a view
        imageView.setOnClickListener {
            selectTile(imageView)
        }
    }

    private fun setFlowerSeasonClickListener(imageView: ImageView) {

        //Set listener for a view
        imageView.setOnClickListener {
            selectFlowerSeason(imageView)
        }
    }

    private fun setRemoveClickListener(imageView: ImageView) {
        //Set listener for a view
        imageView.setOnClickListener {
            removeTile(imageView)
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
            if ( (endIndex + 2 < list.size) &&
                (extractNumber(list[endIndex]) + 1 == extractNumber(list[endIndex + 1])) &&
                (extractNumber(list[endIndex]) + 2 == extractNumber(list[endIndex + 2])) &&
                (extractSuit(list[endIndex]) != "dragon") &&
                (extractSuit(list[endIndex]) != "wind")) { //If chow is not of dragon or wind
                return (startIndex..endIndex + 2).toList()

            } else {
                // Move to the next index
                endIndex++
                startIndex = endIndex
            }
        }

        return null // No consecutive run of three found
    }

    private fun updateScore(scoringType: String, tileName: String){
        val tileSuit = extractSuit(tileName)
        val tileNumber = extractNumber(tileName)
        val tile: String = if (tileSuit == "wind"){
            windNames[tileNumber-1]
        } else if (tileSuit == "flower"){
            flowerNames[tileNumber-1]
        } else if (tileSuit == "season"){
            seasonNames[tileNumber-1]
        } else if (tileSuit == "dragon"){
            dragonNames[tileNumber-1]
        }else{
            tileNumber.toString()
        }
        val scorerText = getString(R.string.scorer, scoringType, tile,tileSuit)
        updateTextNewline(R.id.scorerDisplay, scorerText)
    }

    //CALCULATING SCORE FUNCTIONS-------------------------------------------------------------------
    private fun calculateScore() {
        //Calculates score from array of selected tiles
        var score = 0
        var multiplier = 1
        //Reset score breakdown
        updateText(R.id.scorerDisplay, "Points scored by:")

        val hand = selectedTileList.toMutableList() //Create copy of selected tile list for scoring
        var uniqueElementsCount = countUniqueElements(hand) //Count unique elements

        val roundCheck: RadioGroup = requireView().findViewById(R.id.wind_round)
        val windOfRound = extractNumber(resources.getResourceEntryName(roundCheck.checkedRadioButtonId))

        val playerCheck: RadioGroup = requireView().findViewById(R.id.wind_player)
        val windOfPlayer = extractNumber(resources.getResourceEntryName(playerCheck.checkedRadioButtonId))

        //CHECK FOR KONGS ------------------------------------------------------
        //While there are 4 or more of the same tiles
        while (uniqueElementsCount.values.max()==4) {
            for (uniqueTile in uniqueElementsCount) { //Run through all tiles
                if (uniqueTile.value == 4) { //If count is greater than 4

                    score += if (extractSuit(uniqueTile.key)=="dragon"){
                        KONGPOINTS* DRAGONMULTIPLIER //KONG OF DRAGON

                    }else if(extractSuit(uniqueTile.key)=="wind"){
                        if ( (extractNumber(uniqueTile.key) == windOfRound) && (extractNumber(uniqueTile.key) == windOfPlayer)) {
                                KONGPOINTS * WINDMULTIPLIER * WINDROUNDMULTIPLIER * WINDPLAYERMULTIPLIER //KONG OF PLAYER'S WIND OF ROUND
                            }else if(extractNumber(uniqueTile.key) == windOfRound){
                                KONGPOINTS * WINDMULTIPLIER * WINDROUNDMULTIPLIER //KONG OF WIND OF ROUND
                            }else if(extractNumber(uniqueTile.key) == windOfPlayer){
                                KONGPOINTS * WINDMULTIPLIER * WINDPLAYERMULTIPLIER //KONG OF PLAYER'S WIND
                            }else{
                                KONGPOINTS* WINDMULTIPLIER //KONG OF WIND
                        }

                    }else if(extractNumber(uniqueTile.key)== 1 || extractNumber(uniqueTile.key)==9){
                        KONGPOINTS* TERMINALMULTIPLIER //TERMINAL KONG

                    }else{
                        KONGPOINTS //STANDARD KONG
                    }
                    updateScore("kong",uniqueTile.key)

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
                        if ( (extractNumber(uniqueTile.key) == windOfRound) && (extractNumber(uniqueTile.key) == windOfPlayer)) {
                            PUNGPOINTS * WINDMULTIPLIER * WINDROUNDMULTIPLIER * WINDPLAYERMULTIPLIER //PUNG OF PLAYER'S WIND OF ROUND
                        }else if(extractNumber(uniqueTile.key) == windOfRound){
                            PUNGPOINTS * WINDMULTIPLIER * WINDROUNDMULTIPLIER //PUNG OF WIND OF ROUND
                        }else if(extractNumber(uniqueTile.key) == windOfPlayer){
                            PUNGPOINTS * WINDMULTIPLIER * WINDPLAYERMULTIPLIER //PUNG OF PLAYER'S WIND
                        }else{
                            PUNGPOINTS* WINDMULTIPLIER //PUNG OF WIND
                        }
                    }else if(extractNumber(uniqueTile.key)== 1 || extractNumber(uniqueTile.key)==9){
                        PUNGPOINTS* TERMINALMULTIPLIER //TERMINAL PUNG
                    }else{
                        PUNGPOINTS //STANDARD PUNG
                    }

                    updateScore("pung",uniqueTile.key)

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
            updateScore("chow",chowTile)

            hand.subList(firstChow[0],firstChow[2]+1).clear() //Remove chow from hand
            //ISSUE WITH MULTIPLE CHOWS?
            hand.sort() //Sort and search again
            firstChow = findFirstConsecutiveRun(hand)
        }

        //CHECK FOR FINAL PAIR (MAHJONG) ------------------------------------------------------

        if (hand.count()==2 && countUniqueElements(hand).keys.count()==1){
            val pairTile = hand[0]
            updateScore("pair", pairTile)

            score += MAHJONGPOINTS
            updateTextNewline(R.id.scorerDisplay, "MAHJONG")

            hand.clear()

        }

        //CHECK FOR ALL 1s, 9s, all same suit -----------------------------------------

        //CHECK FOR MATCHING WINDS, FLOWERS, SEASONS -----------------------------------------
        for (i in 0 until flowerSeasonList.count()-1) {
            val flowerSeasonType = extractSuit(flowerSeasonList[i])
            val flowerSeasonNumber = extractNumber(flowerSeasonList[i])
            if (flowerSeasonType=="flower"){
                score += FLOWERPOINTS
                updateScore("flower",flowerSeasonList[i])
                if (flowerSeasonNumber==windOfPlayer){
                    multiplier *= SAMEFLOWERMULTIPLIER
                    updateScore("player's flower",flowerSeasonList[i])
                }
            }else if(flowerSeasonType=="season"){
                score += SEASONPOINTS
                updateScore("season",flowerSeasonList[i])
                if (flowerSeasonNumber==windOfPlayer){
                    multiplier *= SAMESEASONMULTIPLIER
                    updateScore("player's season",flowerSeasonList[i])
                }
            }

        }

        updateTextNewline(R.id.scorerDisplay,"Unused tiles:"+ hand.toString())

        score *= multiplier
        //Update score

        val scoreText = getString(R.string.score, score)
        updateText(R.id.score_text,scoreText)
    }


}