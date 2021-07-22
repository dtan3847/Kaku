package ca.fuwafuwa.kaku.Search;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.fuwafuwa.kaku.Database.JmDictDatabase.Models.EntryOptimized;

public class EntrySearcher {
    private static final String TAG = "EntrySearcher";
    private final Map<String, List<EntryOptimized>> lookupKanji = new HashMap<>();
    private final Map<String, List<EntryOptimized>> lookupKana = new HashMap<>();

    EntrySearcher(List<EntryOptimized> entries) {
        for (EntryOptimized e : entries) {
            List<EntryOptimized> existing;
            if (!lookupKanji.containsKey(e.getKanji())) {
                existing = lookupKanji.put(e.getKanji(), new ArrayList<>());
            } else {
                existing = lookupKanji.get(e.getKanji());
            }
            existing.add(e);
            for (String reading : e.getReadings().split(", ")) {
                List<EntryOptimized> existingR;
                if (!lookupKana.containsKey(reading)) {
                    existingR = lookupKana.put(reading, new ArrayList<>());
                } else {
                    existingR = lookupKana.get(reading);
                }
                existingR.add(e);
            }
        }
        Log.d(TAG, "First 5 JmDict entries");
        for (int i = 0; i < 5; i++) {
            Log.d(TAG, entries.get(i).toString());
        }
        Log.d(TAG, String.format(
                "Built lookup tables: kanji %s, kana %s, entries %s",
                lookupKanji.size(),
                lookupKana.size(),
                entries.size()
        ));
    }

    public String search(String text) {
        String ret = null;
        ret = searchInner(text);
        if(ret != null) return ret;
        ret = searchInner(replaceHiraWithKata(text));
        if(ret != null) return ret;
        ret = searchInner(replaceKataWithHira(text));
        if(ret != null) return ret;
        ret = jsonDictAnyAsIs(text);
        // do something
        return ret;
    }

    public String searchInner(String text) {
        //todo
        return null;
    }

    public String replaceHiraWithKata(String text) {
        //todo
        return text;
    }

    public String replaceKataWithHira(String text) {
        //todo
        return text;
    }

    public String jsonDictAnyAsIs(String text) {
        //todo
        return null;
    }


}
