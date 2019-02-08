package com.cajetan.youtubeplus.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController

import com.cajetan.youtubeplus.R

class OthersFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_others, container, false)

        view.findViewById<Button>(R.id.whereDoWeButton).setOnClickListener {
            findNavController().navigate(R.id.action_others_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to "Bcqb7kzekoc"))
        }

        view.findViewById<Button>(R.id.settingsButton).setOnClickListener {
            findNavController().navigate(R.id.action_others_to_settingsFragment)
        }

        return view
    }
}
