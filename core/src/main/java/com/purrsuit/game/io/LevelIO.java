package com.purrsuit.game.io;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.purrsuit.game.ecs.Level;
import java.util.List;
import java.util.ArrayList;

public class LevelIO {
    private LevelIO(){}

    public static Level load(String internalPath){
        FileHandle fh = Gdx.files.internal(internalPath);
        if (!fh.exists()){
            throw new IllegalArgumentException("Level file not found: " + internalPath);
        }

        String raw = fh.readString("UTF-8");
        if(raw.length() > 0 && raw.charAt(0) == '\uFEFF'){
            raw = raw.substring(1);
        }
        String[] lines = raw.split("\\r?\\n");

        List<String> rows = new ArrayList<>();
        for (String ln : lines) {
            String s = ln;
            int semi = s.indexOf(';');
            int dsl = s.indexOf("//");
            int cut = -1;
            if (semi >= 0) cut = semi;
            if (dsl >= 0 && (cut == -1 || dsl < cut)) cut = dsl;
            if (cut >= 0) s = s.substring(0, cut);

            if (s.length() > 0) {
                rows.add(s);
            }
        }
        if (rows.isEmpty()){
            throw new IllegalArgumentException("Level file is empty: " + internalPath);
        }
        int width = 0;
        for (String r : rows) {
            width = Math.max(width, r.length());
        }
        List<String> normalized = new ArrayList<String>(rows.size());
        for (String r : rows) {
            if (r.length() > width){
                StringBuilder sb = new StringBuilder(width);
                sb.append(r);
                for (int i = r.length(); i < width; i++){
                    sb.append('.');
                }
                normalized.add(sb.toString());
            } else {
                normalized.add(r);
            }
        }
        return Level.ASCIIToLevel(normalized.toArray(new String[0]));
    }
}
