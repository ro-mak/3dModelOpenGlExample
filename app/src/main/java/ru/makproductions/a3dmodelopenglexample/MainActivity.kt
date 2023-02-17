package ru.makproductions.a3dmodelopenglexample

import MyGlSurfaceView
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import ru.makproductions.a3dmodelopenglexample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: MyGlSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create the GLSurfaceView and set it as the content view
        glSurfaceView = MyGlSurfaceView(this)
        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        // Pause the GLSurfaceView when the activity is paused
        glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        // Resume the GLSurfaceView when the activity is resumed
        glSurfaceView.onResume()
    }
}