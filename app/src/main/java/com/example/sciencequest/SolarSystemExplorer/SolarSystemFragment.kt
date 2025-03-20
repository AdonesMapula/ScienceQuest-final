package com.example.sciencequest.SolarSystemExplorer

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sciencequest.R
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.database.*

class SolarSystemFragment : Fragment(R.layout.fragment_solar_system) {

    private lateinit var arFragment: ArFragment
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var exoplanetAdapter: ExoplanetAdapter
    private lateinit var database: DatabaseReference

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize AR Fragment, RecyclerView, Firebase Database
        //arFragment = childFragmentManager.findFragmentById(R.id.sceneform_fragment) as ArFragment
        searchView = view.findViewById(R.id.searchView)
        recyclerView = view.findViewById(R.id.exoplanetRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)
        exoplanetAdapter = ExoplanetAdapter()
        recyclerView.adapter = exoplanetAdapter

        // Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("exoplanets")

        // Set up AR interaction
//        arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane?, motionEvent ->
//            place3DPlanet(hitResult)
//        }

        // Set up search functionality
        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchExoplanets(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun place3DPlanet(hitResult: HitResult) {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        //anchorNode.setParent(arFragment.arSceneView.scene)

        // Load the planet model (Make sure it's in the assets folder)
        val planetModel = Uri.parse("model.sfb") // Your 3D model file in the assets folder
        ModelRenderable.builder()
            .setSource(requireContext(), planetModel)
            .build()
            .thenAccept { renderable ->
                //val planetNode = TransformableNode(arFragment.transformationSystem)
//                planetNode.renderable = renderable
//                planetNode.setParent(anchorNode)
//                planetNode.select()
            }
            .exceptionally {
                Toast.makeText(requireContext(), "Error loading 3D model", Toast.LENGTH_SHORT).show()
                null
            }
    }

    private fun searchExoplanets(query: String) {
        // Query the Firebase database for exoplanets by name
        database.orderByChild("name").equalTo(query).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val exoplanets = mutableListOf<Exoplanet>()
                for (data in snapshot.children) {
                    val exoplanet = data.getValue(Exoplanet::class.java)
                    exoplanet?.let { exoplanets.add(it) }
                }
                exoplanetAdapter.submitList(exoplanets) // Update the adapter's data
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error searching exoplanets", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
