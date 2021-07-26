package ca.fuwafuwa.kaku.Search;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.fuwafuwa.kaku.Database.JmDictDatabase.Models.EntryOptimized;

public class EntrySearcher {
    private static final String TAG = "EntrySearcher";
    private final List<EntryOptimized> dict;
    private final Map<String, List<Integer>> lookupKanji = new HashMap<>();
    private final Map<String, List<Integer>> lookupKana = new HashMap<>();

    EntrySearcher(List<EntryOptimized> dict) {
        this.dict = dict;
        // Create maps from all entries
        for (int i = 0; i < dict.size(); i++) {
            EntryOptimized e = dict.get(i);
            List<Integer> existing;
            if (!lookupKanji.containsKey(e.getKanji())) {
                existing = lookupKanji.put(e.getKanji(), new ArrayList<>());
            } else {
                existing = lookupKanji.get(e.getKanji());
            }
            existing.add(i);
            for (String reading : e.getReadings().split(", ")) {
                List<Integer> existingR;
                if (!lookupKana.containsKey(reading)) {
                    existingR = lookupKana.put(reading, new ArrayList<>());
                } else {
                    existingR = lookupKana.get(reading);
                }
                existingR.add(i);
            }
        }
        Log.d(TAG, "First 5 JmDict entries");
        for (int i = 0; i < 5; i++) {
            Log.d(TAG, dict.get(i).toString());
        }
        Log.d(TAG, String.format(
                "Built lookup tables: kanji %s, kana %s, entries %s",
                lookupKanji.size(),
                lookupKana.size(),
                dict.size()
        ));
    }

    public List<EntryOptimized> search(String text) {
        List<EntryOptimized> ret = null;
        ret = searchInner(text);
        if(ret != null) return ret;
        ret = searchInner(replaceHiraWithKata(text));
        if(ret != null) return ret;
        ret = searchInner(replaceKataWithHira(text));
        if(ret != null) return ret;
        ret = jsonDictAnyAsIs(text);
        //todo: custom dicts
        return ret;
    }

    private List<EntryOptimized> searchInner(String text) {
        if (lookupKanji.containsKey(text)) {
            return getFromDict(lookupKanji.get(text), text);
        } else if (lookupKana.containsKey(text)) {
            return getFromDict(lookupKana.get(text), text);
        }
        return null;
    }

    private List<EntryOptimized> getFromDict(List<Integer> indexes, String text) {
        List<EntryOptimized> ret = new ArrayList<>();
        for (int i : indexes) {
            EntryOptimized e = dict.get(i);
            // todo: are entry.from, entry.found needed?
            ret.add(e);
        }
        // pos already exists for non-primary entries
        return ret;
    }

    private String replaceHiraWithKata(String text) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < text.length(); i++)
        {
            int codepoint = text.codePointAt(i);
            if(codepoint >= 0x3040 && codepoint <= 0x3096)
                codepoint += (0x30A0 - 0x3040);
            else if(codepoint >= 0x309D && codepoint <= 0x309E)
                codepoint += (0x30A0 - 0x3040);
            sb.append(codepoint);
        }
        return sb.toString();
    }

    private String replaceKataWithHira(String text) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < text.length(); i++)
        {
            int codepoint = text.codePointAt(i);
            if(codepoint >= 0x30A0 && codepoint <= 0x30F6)
                codepoint -= (0x30A0 - 0x3040);
            else if(codepoint >= 0x30FD && codepoint <= 0x30FE)
                codepoint -= (0x30A0 - 0x3040);
            sb.append(codepoint);
        }
        return sb.toString();
    }

    private List<EntryOptimized> jsonDictAnyAsIs(String text) {
        //todo: custom dicts
        return null;
    }

    private List<EntryOptimized> jsonLookupArbitraryAsIs(String text) {
        //todo: custom dicts
        return null;
    }


}
