package ca.fuwafuwa.kaku.Windows.Views

import android.content.Context
import android.util.AttributeSet

import java.util.ArrayList

import ca.fuwafuwa.kaku.Windows.Data.DisplayData
import ca.fuwafuwa.kaku.Windows.Interfaces.ISearchPerformer
import ca.fuwafuwa.kaku.Windows.WindowCoordinator

/**
 * Created by 0xbad1d3a5 on 5/5/2016.
 */
class KanjiGridView : SquareGridView
{
    private lateinit var mWindowCoordinator: WindowCoordinator
    private lateinit var mSearchPerformer: ISearchPerformer
    private lateinit var mDisplayData: DisplayData

    val kanjiViewList: List<KanjiCharacterView>
        get()
        {
            val count = childCount
            val kanjiViewList = ArrayList<KanjiCharacterView>()

            for (i in 0 until count)
            {
                kanjiViewList.add(getChildAt(i) as KanjiCharacterView)
            }

            return kanjiViewList
        }

    constructor(context: Context) : super(context)
    {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    {
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)
    {
    }

    fun setDependencies(windowCoordinator: WindowCoordinator, searchPerformer: ISearchPerformer)
    {
        mWindowCoordinator = windowCoordinator
        mSearchPerformer = searchPerformer
    }

    fun setText(displayData: DisplayData)
    {
        mDisplayData = displayData

        for (squareChar in displayData.squareChars)
        {
            val kanjiView = KanjiCharacterView(mContext)
            kanjiView.setDependencies(mWindowCoordinator, mSearchPerformer)
            kanjiView.setText(squareChar)

            addView(kanjiView)
        }

        setItemCount(displayData.count)
        postInvalidate()
    }

    fun clearText()
    {
        removeAllViews()
        postInvalidate()
    }

    fun correctText()
    {
        mDisplayData.recomputeChars()
        val kanjiViews = kanjiViewList
        val numChars = mDisplayData.count
        val kanjiViewSize = kanjiViews.size

        if (numChars > kanjiViewSize)
        {
            addKanjiViews(numChars - kanjiViewSize)
        } else if (numChars < kanjiViewSize)
        {
            removeKanjiViews(kanjiViewSize - numChars)
        }

        for ((index, squareChar) in mDisplayData.squareChars.withIndex())
        {
            val kanjiView = getChildAt(index) as KanjiCharacterView
            kanjiView.setText(squareChar)
        }

        setItemCount(numChars)
        postInvalidate()
    }

    fun unhighlightAll()
    {
        for (k in kanjiViewList)
        {
            k.unhighlight()
        }
    }

    private fun addKanjiViews(count: Int)
    {
        for (i in 0 until count)
        {
            val kanjiView = KanjiCharacterView(mContext)
            kanjiView.setDependencies(mWindowCoordinator, mSearchPerformer)

            addView(kanjiView)
        }
    }

    private fun removeKanjiViews(count: Int)
    {
        val childCount = childCount
        for (i in childCount downTo childCount - count + 1)
        {
            removeViewAt(i - 1)
        }
    }

    companion object
    {
        private val TAG = KanjiGridView::class.java.name
    }
}
