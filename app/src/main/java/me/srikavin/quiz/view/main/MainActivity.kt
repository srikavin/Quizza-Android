package me.srikavin.quiz.view.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomnavigation.BottomNavigationView
import me.srikavin.quiz.R
import me.srikavin.quiz.SlidingPagerAdapter
import me.srikavin.quiz.appModule
import me.srikavin.quiz.repository.AuthRepository
import me.srikavin.quiz.view.LoginActivity
import org.koin.android.ext.koin.with
import org.koin.standalone.StandAloneContext
import java.io.*
import java.util.*

const val TAG = "QUIZ"

private var init = false

class MainActivity : AppCompatActivity() {
    private var profileFragment = ProfileFragment()
    private var battleFragment = BattleFragment()
    private var createFragment = CreateFragment()
    private var authRepository = AuthRepository

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!init) {
            StandAloneContext.startKoin(listOf(appModule)).with(applicationContext)
            init = true
        }

        if (authRepository.getAuthToken(this) == null) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            return
        }

        setContentView(R.layout.activity_main)

        val possibleStates = ArrayList<Fragment>()
        possibleStates.add(profileFragment)
        possibleStates.add(battleFragment)
        possibleStates.add(createFragment)

        val viewPager = findViewById<ViewPager>(R.id.main_viewpager)
        viewPager.adapter = SlidingPagerAdapter(supportFragmentManager, possibleStates)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_bar)

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottombaritem_profile -> {
                    updateTitle(0)
                    viewPager.currentItem = 0
                }
                R.id.bottombaritem_battle -> {
                    updateTitle(1)
                    viewPager.currentItem = 1
                }
                R.id.bottombaritem_create -> {
                    updateTitle(2)
                    viewPager.currentItem = 2
                }
            }
            true
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                updateTitle(position)
                when (position) {
                    0 -> bottomNavigationView.selectedItemId = R.id.bottombaritem_profile
                    1 -> bottomNavigationView.selectedItemId = R.id.bottombaritem_battle
                    2 -> bottomNavigationView.selectedItemId = R.id.bottombaritem_create
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        updateTitle(1)
        bottomNavigationView.selectedItemId = R.id.bottombaritem_battle
        viewPager.currentItem = 1
    }

    private fun updateTitle(page: Int) {
        val actionBar = supportActionBar!!
        when (page) {
            0 -> actionBar.setTitle(R.string.bottombar_profile)
            1 -> actionBar.setTitle(R.string.bottombar_battle)
            2 -> actionBar.setTitle(R.string.bottombar_create)
        }
    }

    private fun reportBug() {
        var logCatException: String? = null
        var logCatTemp: File? = null
        try {
            val process = Runtime.getRuntime().exec("logcat -d")
            val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))

            val log = StringBuilder()
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                log.append(line)
                log.append('\n')
                line = bufferedReader.readLine()
            }
            val logcat = log.toString()
            val cacheDir = applicationContext.externalCacheDir
            logCatTemp = File.createTempFile("bugreport", "logcat.txt", cacheDir)
            val writer = FileWriter(logCatTemp)
            writer.write(logcat)
            writer.close()

        } catch (e: Exception) {
            // Exception occurred when saving log cat
            logCatException = "Exception occurred while trying to save logcat:\n"
            val sw = StringWriter()
            e.printStackTrace(PrintWriter(sw))
            logCatException += sw.toString()
        }

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.setData(Uri.parse("mailto:"))
                .putExtra(Intent.EXTRA_EMAIL, arrayOf("quizza.bug.reports@mail.srikavin.me"))
                .putExtra(Intent.EXTRA_SUBJECT, "Quizza Bug Report")
        if (logCatTemp != null) {
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logCatTemp))
        }
        intent.putExtra(Intent.EXTRA_TEXT,
                "Steps to Reproduce: \n" +
                        "Details about the bug: \n" +
                        "Would you like to be contacted if more details are necessary? \n" +
                        "\n" +
                        "------------------------------------------------------------------\n" +
                        "LOGCAT:\n" +
                        (logCatException ?: "Saved successfully")
        )
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_main_actionbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.report_bug) {
            reportBug()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
