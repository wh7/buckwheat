package com.danilkinkin.buckwheat

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.*
import com.danilkinkin.buckwheat.adapters.*
import com.danilkinkin.buckwheat.decorators.SpendsDividerItemDecoration
import com.danilkinkin.buckwheat.utils.getThemeColor
import com.danilkinkin.buckwheat.viewmodels.SpentViewModel
import com.danilkinkin.buckwheat.widgets.topsheet.TopSheetBehavior
import com.google.android.material.color.DynamicColors
import com.google.android.material.floatingactionbutton.FloatingActionButton


var instance: MainActivity? = null

class MainActivity : AppCompatActivity() {
    lateinit var model: SpentViewModel

    lateinit var parentView: View

    val recyclerView: RecyclerView by lazy {
        findViewById(R.id.recycle_view)
    }

    private val fabHome: FloatingActionButton by lazy {
        findViewById(R.id.fab_home_btn)
    }

    companion object {
        val TAG: String = MainActivity::class.java.simpleName

        fun getInstance(): MainActivity {
            return instance!!
        }
    }

    init {
        instance = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            DynamicColors.applyToActivitiesIfAvailable(application)
        }

        val windowInsetsController =  WindowCompat.getInsetsController(window, window.decorView)

        windowInsetsController.isAppearanceLightNavigationBars = true

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root)) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            val mlp = view.layoutParams as MarginLayoutParams
            mlp.topMargin = 0
            mlp.leftMargin = insets.left
            mlp.bottomMargin = 0
            mlp.rightMargin = insets.right
            view.layoutParams = mlp

            WindowInsetsCompat.CONSUMED
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }

        window.statusBarColor = ColorUtils.setAlphaComponent(
            getThemeColor(this, com.google.android.material.R.attr.colorPrimaryContainer),
            255,
        )

        val model: SpentViewModel by viewModels()

        this.model = model

        build()
        observe()
    }

    override fun onCreateView(
        parent: View?,
        name: String,
        context: Context,
        attrs: AttributeSet
    ): View? {
        if (parent != null) {
            parentView = parent
        }

        return super.onCreateView(parent, name, context, attrs)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun observe() {
        model.requireReCalcBudget.observeForever {
            if (it) {
                val newDayBottomSheet = NewDayBottomSheet()
                newDayBottomSheet.show(supportFragmentManager, NewDayBottomSheet.TAG)
            }
        }

        model.requireSetBudget.observeForever {
            if (it) {
                val settingsBottomSheet = SettingsBottomSheet()
                settingsBottomSheet.show(supportFragmentManager, SettingsBottomSheet.TAG)
            }
        }
    }

    private fun build() {
        var isChangeBecauseRemove = false

        val layoutManager = object : LinearLayoutManager(this) {
            private var isScrollEnabled = true

            fun setScrollEnabled(flag: Boolean) {
                this.isScrollEnabled = flag
            }

            override fun canScrollVertically(): Boolean {
                return isScrollEnabled && super.canScrollVertically();
            }
        }

        val spendsDividerItemDecoration = SpendsDividerItemDecoration(recyclerView.context)
        recyclerView.addItemDecoration(spendsDividerItemDecoration)

        layoutManager.stackFromEnd = true

        val topAdapter = TopAdapter(model)
        val spendsAdapter = SpendsAdapter()
        val contactAdapter = ConcatAdapter(topAdapter, spendsAdapter /*, editorAdapter, keyboardAdapter */ )

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = contactAdapter
        recyclerView.scrollToPosition(spendsAdapter.itemCount - 1)

        val swipeToDeleteCallback = SwipeToDeleteCallback(applicationContext, spendsAdapter) {
            isChangeBecauseRemove = true
            model.removeSpent(it)
        }

        val itemTouchhelper = ItemTouchHelper(swipeToDeleteCallback)

        itemTouchhelper.attachToRecyclerView(recyclerView)

        spendsAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                if (isChangeBecauseRemove) {
                    isChangeBecauseRemove = false
                } else {
                    layoutManager.scrollToPosition(spendsAdapter.itemCount)
                }
            }
        });

        model.getSpends().observeForever { spents ->
            spendsAdapter.submitList(spents)
        }

        model.budget.observeForever {
            topAdapter.notifyDataSetChanged()
        }

        fabHome.setOnClickListener {
            recyclerView.smoothScrollToPosition(contactAdapter.itemCount - 1)

            val topSheetBehavior = try {
                ((recyclerView.layoutParams as CoordinatorLayout.LayoutParams).behavior as TopSheetBehavior)
            } catch (e: Exception) {
                null
            }

            topSheetBehavior?.setSmartState(TopSheetBehavior.Companion.State.STATE_HIDDEN)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }
}