package io.github.mucute.qwq.kolomitm.activity

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import io.github.mucute.qwq.kolomitm.R
import io.github.mucute.qwq.kolomitm.activity.BaseActivity
import io.github.mucute.qwq.kolomitm.adapter.ViewPager2Adapter
import io.github.mucute.qwq.kolomitm.databinding.ActivityMainBinding
import io.github.mucute.qwq.kolomitm.fragment.main.AccountsFragment
import io.github.mucute.qwq.kolomitm.fragment.main.ExtensionsFragment
import io.github.mucute.qwq.kolomitm.fragment.main.HomeFragment
import io.github.mucute.qwq.kolomitm.fragment.main.SettingsFragment

class MainActivity : BaseActivity() {

    private lateinit var viewBinding: ActivityMainBinding

    private val fragments = listOf(
        HomeFragment(),
        ExtensionsFragment(),
        AccountsFragment(),
        SettingsFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        applyWindowInsets(viewBinding.root)

        val viewPager2 = viewBinding.viewPager2
        val bottomNavigationView = viewBinding.bottomNavigationView

        viewPager2.adapter = ViewPager2Adapter(this, fragments)
        bottomNavigationView.setOnItemSelectedListener {
            viewPager2.currentItem = when (it.itemId) {
                R.id.home -> 0
                R.id.extensions -> 1
                R.id.accounts -> 2
                else -> 3
            }
            setToolbarTitle(viewPager2.currentItem)
            true
        }

        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigationView.selectedItemId = when (position) {
                    0 -> R.id.home
                    1 -> R.id.extensions
                    2 -> R.id.accounts
                    else -> R.id.settings
                }
                setToolbarTitle(position)
            }
        })
    }

    private fun setToolbarTitle(position: Int) {
        viewBinding.materialToolbar.setTitle(
            when (position) {
                0 -> R.string.home
                1 -> R.string.extensions
                2 -> R.string.accounts
                else -> R.string.settings
            }
        )
    }

}