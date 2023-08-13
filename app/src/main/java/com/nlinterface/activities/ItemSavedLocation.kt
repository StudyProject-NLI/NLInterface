package com.nlinterface.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nlinterface.R

class SavedLocationsActivity : AppCompatActivity() {

    private lateinit var savedLocationsRecyclerView: RecyclerView
    private lateinit var savedLocationsAdapter: SavedLocationsAdapter
    private lateinit var emptyTextView: TextView

    private var savedLocations: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_locations)

        savedLocationsRecyclerView = findViewById(R.id.savedLocationsRecyclerView)
        emptyTextView = findViewById(R.id.emptyTextView)

        // Retrieve the saved locations from the intent
        val intentLocations = intent.getStringArrayListExtra("savedLocations")
        savedLocations = intentLocations?.toMutableList() ?: mutableListOf()

        savedLocationsAdapter = SavedLocationsAdapter(savedLocations,
            onItemClick = { selectedLocation ->
                openMaps(selectedLocation)
            },
            onDeleteClick = { locationToDelete ->
                deleteLocation(locationToDelete)
            }
        )

        savedLocationsRecyclerView.adapter = savedLocationsAdapter
        savedLocationsRecyclerView.layoutManager = LinearLayoutManager(this)

        updateEmptyTextViewVisibility()
    }

    private fun openMaps(address: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            Toast.makeText(
                this,
                "Google Maps is not installed on your device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun deleteLocation(location: String) {
        savedLocations.remove(location)
        savedLocationsAdapter.notifyDataSetChanged()

        // Update the saved locations in SharedPreferences after deleting
        SharedPreferencesHelper.saveLocations(this, savedLocations)

        updateEmptyTextViewVisibility()
    }

    private fun updateEmptyTextViewVisibility() {
        if (savedLocations.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
        } else {
            emptyTextView.visibility = View.GONE
        }
    }

    inner class SavedLocationsAdapter(
        private val locations: MutableList<String>,
        private val onItemClick: (String) -> Unit,
        private val onDeleteClick: (String) -> Unit
    ) : RecyclerView.Adapter<SavedLocationsAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val locationButton: Button = itemView.findViewById(R.id.locationButton)
            val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val itemView = layoutInflater.inflate(R.layout.item_saved_location, parent, false)
            return ViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val location = locations[position]
            holder.locationButton.text = location
            holder.locationButton.setOnClickListener {
                onItemClick(location)
            }

            holder.deleteButton.setOnClickListener {
                onDeleteClick(location)
            }
        }

        override fun getItemCount(): Int {
            return locations.size
        }
    }
}
