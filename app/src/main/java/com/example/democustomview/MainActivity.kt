package com.example.democustomview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import com.example.democustomview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnHideChart.setOnClickListener {
            binding.lineChartView.apply {
                this.pivotY = this.height.toFloat()
                this.animate().scaleY(0f).start()
            }
//            binding.lineChartView.animate().setDuration(2000).setInterpolator(DecelerateInterpolator())
//                .scaleY(1f).setStartDelay(2000L).start()
        }
        binding.btnShowChart.setOnClickListener {
            binding.lineChartView.apply {
                this.pivotY = this.height.toFloat()
                this.animate().scaleY(1f).setInterpolator(OvershootInterpolator(0.5f)).start()
            }
        }
    }


}