package com.neilpower.mahjong

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.neilpower.mahjong.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */

private var selectedTileNumber = 0
private var selectedTileList: MutableList<String> = ArrayList()

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Calculate score
        binding.calculateScoreButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
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
        super.onDestroyView()
        _binding = null
    }

//    private fun getTileById(tileId: Int): ImageView {
//        val selectionTable: TableLayout? = view?.findViewById(R.id.selected_tiles)
//        val row: TableRow = selectionTable?.getChildAt(0) as TableRow
//        return row.getChildAt(tileId) as ImageView
//    }

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

            val selectedDisplay: TextView = requireView().findViewById(R.id.selectionDisplay)

            //<string name="welcome_messages">Hello, %1$s! You have %2$d new messages.</string>
            val selectedTileText = getString(R.string.concatenated_newline, selectedDisplay.text, clickedTileName)
            selectedDisplay.text = selectedTileText
        }
    }

    private fun setClickListenerForImage(imageView: ImageView) {
        imageView.setOnClickListener {
            updateSelected(imageView)
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

        //Reset selected tile count to 0
        selectedTileNumber = 0

        val selectedDisplay: TextView = requireView().findViewById(R.id.selectionDisplay)
        selectedDisplay.text = "Selected Tiles:"

    }


}