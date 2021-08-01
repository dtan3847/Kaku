package ca.fuwafuwa.kaku.Search

import android.content.Context
import android.util.Log
import ca.fuwafuwa.kaku.Database.JmDictDatabase.JmDatabaseHelper
import ca.fuwafuwa.kaku.Database.JmDictDatabase.Models.EntryOptimized
import java.util.*

class EntrySearcher private constructor(context: Context) {
    private val TAG = "EntrySearcher"
    private val dict: List<EntryOptimized>
    private val lookupKanji: MutableMap<String, MutableList<Int>> = HashMap()
    private val lookupKana: MutableMap<String, MutableList<Int>> = HashMap()
    init {
        val helper = JmDatabaseHelper.instance(context)
        val dao = helper.getDbDao<EntryOptimized>(EntryOptimized::class.java)
        dict = dao.queryForAll()
        // Create maps from all entries
        for (i in dict.indices) {
            val e = dict[i]
            if (!lookupKanji.containsKey(e.kanji)) {
                lookupKanji[e.kanji] = ArrayList()
            }
            val existing: MutableList<Int> = lookupKanji[e.kanji]!!
            existing.add(i)
            for (reading in e.readings.split(", ").toTypedArray()) {
                if (!lookupKana.containsKey(reading)) {
                    lookupKana[reading] = ArrayList()
                }
                val existingR: MutableList<Int> = lookupKana[reading]!!
                existingR.add(i)
            }
        }
        Log.d(TAG, "First 5 JmDict entries")
        for (i in 0..4) {
            Log.d(TAG, dict[i].toString())
        }
        Log.d(
            TAG, String.format(
                "Built lookup tables: kanji %s, kana %s, entries %s",
                lookupKanji.size,
                lookupKana.size,
                dict.size
            )
        )
    }
    fun search(text: String): List<EntryOptimized> {
        var ret: List<EntryOptimized>? = null
        ret = searchInner(text)
        if (ret != null) return ret
        ret = searchInner(replaceHiraWithKata(text))
        if (ret != null) return ret
        ret = searchInner(replaceKataWithHira(text))
        if (ret != null) return ret
        ret = jsonDictAnyAsIs(text)
        if (ret != null) return ret
        //todo: custom dicts
        return listOf()
    }

    private fun searchInner(text: String): List<EntryOptimized>? {
        if (lookupKanji.containsKey(text)) {
            return getFromDict(lookupKanji[text]!!, text)
        } else if (lookupKana.containsKey(text)) {
            return getFromDict(lookupKana[text]!!, text)
        }
        return null
    }

    private fun getFromDict(indexes: List<Int>, text: String): List<EntryOptimized> {
        val ret: MutableList<EntryOptimized> = ArrayList()
        for (i in indexes) {
            val e = dict[i]
            // todo: are entry.from, entry.found needed?
            ret.add(e)
        }
        // pos already exists for non-primary entries
        return ret
    }

    private fun replaceHiraWithKata(text: String): String {
        val sb = StringBuilder()
        for (i in text.indices) {
            var codepoint = text.codePointAt(i)
            if (codepoint in 0x3040..0x3096) codepoint += 0x30A0 - 0x3040 else if (codepoint in 0x309D..0x309E) codepoint += 0x30A0 - 0x3040
            sb.append(codepoint)
        }
        return sb.toString()
    }

    private fun replaceKataWithHira(text: String): String {
        val sb = StringBuilder()
        for (i in text.indices) {
            var codepoint = text.codePointAt(i)
            if (codepoint in 0x30A0..0x30F6) codepoint -= 0x30A0 - 0x3040 else if (codepoint in 0x30FD..0x30FE) codepoint -= 0x30A0 - 0x3040
            sb.append(codepoint)
        }
        return sb.toString()
    }

    private fun jsonDictAnyAsIs(text: String): List<EntryOptimized>? {
        //todo: custom dicts
        return null
    }

    private fun jsonLookupArbitraryAsIs(text: String): List<EntryOptimized>? {
        //todo: custom dicts
        return null
    }

    companion object : SingletonHolder<EntrySearcher, Context>(::EntrySearcher)

}