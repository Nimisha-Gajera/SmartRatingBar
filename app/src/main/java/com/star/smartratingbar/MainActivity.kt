package com.star.smartratingbar

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.slider.Slider
import com.library.smart_rating_bar.SmartRatingBar
import com.star.smartratingbar.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private  val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.starRatingBar.ratingChangeListener =
            object : SmartRatingBar.OnRatingChangeListener {
                override fun onRatingChanged(rating: Int) {
                    binding.mTxtStarRatingValue.text = "$rating"
                }
            }
        binding.heartRatingBar.ratingChangeListener = object :
            SmartRatingBar.OnRatingChangeListener {
            override fun onRatingChanged(rating: Int) {
                binding.mTxtHeartRatingValue.text = "$rating"
            }
        }
        binding.diamondRatingBar.ratingChangeListener = object :
            SmartRatingBar.OnRatingChangeListener {
            override fun onRatingChanged(rating: Int) {
                binding.mTxtDiamondRatingValue.text = "$rating"
            }
        }
        binding.crownRatingBar.ratingChangeListener = object :
            SmartRatingBar.OnRatingChangeListener {
            override fun onRatingChanged(rating: Int) {
                binding.mTxtCrownRatingValue.text = "$rating"
            }
        }

        binding.mSliderSize.addOnChangeListener(object : Slider.OnChangeListener {
            override fun onValueChange(p0: Slider, p1: Float, p2: Boolean) {
                binding.starRatingBar.setStarSize(p1)
                binding.heartRatingBar.setStarSize(p1)
                binding.diamondRatingBar.setStarSize(p1)
                binding.crownRatingBar.setStarSize(p1)
            }
        })
        binding.mSliderSpace.addOnChangeListener(object : Slider.OnChangeListener {
            override fun onValueChange(p0: Slider, p1: Float, p2: Boolean) {
                binding.starRatingBar.setStarPadding(p1)
                binding.heartRatingBar.setStarPadding(p1)
                binding.diamondRatingBar.setStarPadding(p1)
                binding.crownRatingBar.setStarPadding(p1)
            }
        })

    }
}