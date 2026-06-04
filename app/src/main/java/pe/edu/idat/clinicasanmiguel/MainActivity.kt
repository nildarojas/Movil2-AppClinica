package pe.edu.idat.clinicasanmiguel

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private lateinit var imgLogo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        imgLogo = findViewById(R.id.imgLogo)

        imgLogo.scaleX = 0.75f
        imgLogo.scaleY = 0.75f
        imgLogo.alpha = 0.4f

        val scaleX = ObjectAnimator.ofFloat(imgLogo, "scaleX", 0.75f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(imgLogo, "scaleY", 0.75f, 1.0f)
        val fadeIn = ObjectAnimator.ofFloat(imgLogo, "alpha", 0.4f, 1.0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, fadeIn)
            duration = 1200
            interpolator = DecelerateInterpolator()
            start()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}