package ca.fuwafuwa.kaku.Search

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import ca.fuwafuwa.kaku.DB_JMDICT_NAME
import ca.fuwafuwa.kaku.DB_KANJIDICT_NAME
import ca.fuwafuwa.kaku.Database.JmDictDatabase.JmDatabaseHelper
import ca.fuwafuwa.kaku.Database.JmDictDatabase.Models.EntryOptimized
import ca.fuwafuwa.kaku.Deinflictor.DeinflectionInfo
import ca.fuwafuwa.kaku.Deinflictor.Deinflector
import java.sql.SQLException
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by 0xbad1d3a5 on 12/16/2016.
 */

class JmTask @Throws(SQLException::class)
constructor(private val mSearchInfo: SearchInfo,
            private val mSearchJmTaskDone: SearchJmTaskDone,
            context: Context) : AsyncTask<Void, Void, List<JmSearchResult>>()
{
    companion object
    {
        private val TAG = JmTask::class.java.name
    }

    private val mJmDbHelper: JmDatabaseHelper = JmDatabaseHelper.instance(context)
    private val mDeinflector: Deinflector = Deinflector(context)
    private val mEntrySearcher: EntrySearcher = EntrySearcher.getInstance(context)

    interface SearchJmTaskDone
    {
        fun jmTaskCallback(results: List<JmSearchResult>, searchInfo: SearchInfo)
    }

    override fun doInBackground(vararg params: Void): List<JmSearchResult>
    {
        val text = mSearchInfo.text
        val textOffset = mSearchInfo.textOffset
        val entryOptimizedDao = mJmDbHelper.getDbDao<EntryOptimized>(EntryOptimized::class.java)

        val startDictTime = System.currentTimeMillis()
//        var character = String(intArrayOf(text.codePointAt(textOffset)), 0, 1)
//
//        // What the flying fuck? Wasn't the entire point of using an ORM is so shit would be escaped for me?
//        character = character.replace("%", "\\%")
//        character = character.replace("_", "\\_")
//        character = character.replace("'", "''")
//
//        val entries: List<EntryOptimized> = entryOptimizedDao.queryBuilder().where().like("kanji", "$character%").query()
        val matchedEntries = rankResults(getMatchedEntries(text, textOffset, listOf()))
        Log.d(TAG, "Dict lookup time: ${System.currentTimeMillis() - startDictTime}")

        return matchedEntries
    }

    override fun onPostExecute(result: List<JmSearchResult>)
    {
        mSearchJmTaskDone.jmTaskCallback(result, mSearchInfo)
    }

    @Throws(SQLException::class)
    private fun getMatchedEntries(text: String, textOffset: Int, entries: List<EntryOptimized>): List<JmSearchResult>
    {
        val end = if (textOffset + 25 >= text.length) text.length else textOffset + 25
        var word = text.substring(textOffset, end)
        val seenEntries = HashSet<EntryOptimized>()
        val results = ArrayList<JmSearchResult>()
        var first = true

        while (word.isNotEmpty())
        {
            // Find deinflections and add them
            val deinfResultsList: List<DeinflectionInfo> = mDeinflector.getPotentialDeinflections(word)
            for (deinfInfo in deinfResultsList)
            {
//                val filteredEntry: List<EntryOptimized> = entries.filter { entry -> entry.kanji == deinfInfo.word }
                val filteredEntry: List<EntryOptimized> = mEntrySearcher.search(deinfInfo.word)
                // todo: sort filtered
                if (filteredEntry.isEmpty())
                {
                    continue
                }

                for (entry in filteredEntry){

                    if (seenEntries.contains(entry)){
                        continue
                    }

                    var valid = true

                    if (!first)
                    {
                        valid = (deinfInfo.type and 1 != 0) && (entry.pos.contains("v1")) ||
                                (deinfInfo.type and 2 != 0) && (entry.pos.contains("v5")) ||
                                (deinfInfo.type and 4 != 0) && (entry.pos.contains("adj-i")) ||
                                (deinfInfo.type and 8 != 0) && (entry.pos.contains("vk")) ||
                                (deinfInfo.type and 16 != 0) && (entry.pos.contains("vs-"))
                    }

                    if (valid){
                        results.add(JmSearchResult(entry, deinfInfo, word))
                        seenEntries.add(entry)
                    }
                }
                if (results.isNotEmpty()) first = false
            }

            // Add all exact matches as well
            val filteredEntry: List<EntryOptimized> = entries.filter { entry -> entry.kanji == word }
            for (entry in filteredEntry)
            {
                if (seenEntries.contains(entry))
                {
                    continue
                }

                results.add(JmSearchResult(entry, DeinflectionInfo(word, 0, ""), word))
                seenEntries.add(entry)
            }

            word = word.substring(0, word.length - 1)
        }

        return results
    }

    private fun rankResults(results: List<JmSearchResult>) : List<JmSearchResult>
    {
        return results.sortedWith(compareBy(
                { getDictPriority(it) },
                { 0 - it.entry.kanji.length },
                { getEntryPriority(it) },
                { getPriority(it) }))
    }

    private fun getDictPriority(result: JmSearchResult) : Int
    {
        return when
        {
            result.entry.dictionary == DB_JMDICT_NAME -> Int.MAX_VALUE - 2
            result.entry.dictionary == DB_KANJIDICT_NAME -> Int.MAX_VALUE - 1
            else -> Int.MAX_VALUE
        }
    }

    private fun getEntryPriority(result: JmSearchResult) : Int
    {
        return if (result.entry.isPrimaryEntry) 0 else 1
    }

    private fun getPriority(result: JmSearchResult) : Int
    {
        val priorities = result.entry.priorities.split(",")
        var lowestPriority = Int.MAX_VALUE

        for (priority in priorities){

            var pri = Int.MAX_VALUE

            if (priority.contains("nf")){ // looks like the range is nf01-nf48
                pri = priority.substring(2).toInt()
            }
            else if (priority == "news1"){
                pri = 60
            }
            else if (priority == "news2"){
                pri = 70
            }
            else if (priority == "ichi1"){
                pri = 80
            }
            else if (priority == "ichi2"){
                pri = 90
            }
            else if (priority == "spec1"){
                pri = 100
            }
            else if (priority == "spec2"){
                pri = 110
            }
            else if (priority == "gai1"){
                pri = 120
            }
            else if (priority == "gai2"){
                pri = 130
            }

            lowestPriority = if (pri < lowestPriority) pri else lowestPriority
        }

        return lowestPriority
    }
}
