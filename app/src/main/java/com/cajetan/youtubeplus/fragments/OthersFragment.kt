package com.cajetan.youtubeplus.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController

import com.cajetan.youtubeplus.R

class OthersFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_others, container, false)

        view.findViewById<Button>(R.id.whereDoWeButton).setOnClickListener {
            findNavController().navigate(R.id.action_others_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to "Bcqb7kzekoc"))
        }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.others_options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_settings -> {
            findNavController().navigate(R.id.action_global_settings)
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

}
