package com.example.userscountry

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.userscountry.Constant.PERMISSION_ID
import com.example.userscountry.databinding.FragmentMainBinding
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import java.util.*


class MainFragment : Fragment(R.layout.fragment_main) {
    private var _binding : FragmentMainBinding? = null
    private val binding get() = _binding!!

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest : LocationRequest


    override fun onResume() {
        super.onResume()
        getUsersLocation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMainBinding.bind(view)

        //initiate fuse location provider client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())



    }

    //Check users permission
    private fun checkPermissions() : Boolean {
        if(
            context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_FINE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED || context?.let {
                ActivityCompat.checkSelfPermission(
                    it, Manifest.permission.ACCESS_COARSE_LOCATION
                )
            } == PackageManager.PERMISSION_GRANTED ){
            return true
        }
        return false
    }

    //Request permission from users
    private fun requestPermission(){
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSION_ID
        )
    }

    //Check if location is enabled
    private fun isLocationEnabled() : Boolean{
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(
            LocationManager.GPS_PROVIDER
        ) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_ID){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Snackbar.make(
                    binding.root,
                    "Permissions granted successfully",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    //function to get users location

    @SuppressLint("MissingPermission")
    private fun getUsersLocation(){
        if(checkPermissions()){
            //check if location is enabled
            if(isLocationEnabled()){
                //Get users location
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {
                    val location = it.result
                    if (location == null){
                        //Got last known location. In some rare situations this can be null.
                        getNewLocation()
                    }else{
                        binding.latitude.text = location.latitude.toString()
                        binding.longitude.text = location.longitude.toString()
                        binding.country.text = getCountryName(location.latitude, location.longitude)

                        when(binding.country.text){
                            "Nigeria" -> Snackbar.make(binding.root, "Nigerian code will run", Snackbar.LENGTH_LONG).show()
                            "Ghana" -> Snackbar.make(binding.root, "Nigerian code will run", Snackbar.LENGTH_LONG).show()
                            else -> Snackbar.make(binding.root, "Default country code runs", Snackbar.LENGTH_LONG).show()
                        }
                    }
                }

            }else{
                Snackbar.make(
                    binding.root,
                    "Kindly enable location service",
                    Snackbar.LENGTH_LONG
                ).show()
            }

        }else{
            requestPermission()
        }
    }

    //try to get users location again in case location is null
    @SuppressLint("MissingPermission")
    private fun getNewLocation(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 2
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,locationCallback, Looper.myLooper()
        )
    }

    //create the locationCallBack variable

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            val lastLocation = p0.lastLocation
            binding.latitude.text = lastLocation.latitude.toString()
            binding.longitude.text = lastLocation.longitude.toString()
            binding.country.text = getCountryName(lastLocation.latitude, lastLocation.longitude)

        }
    }

    private fun getCountryName(lat: Double, long: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geocoder.getFromLocation(lat, long, 1)

        return address[0].countryName

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}