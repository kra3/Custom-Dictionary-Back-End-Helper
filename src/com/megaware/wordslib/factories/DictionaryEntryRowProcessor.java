package com.megaware.wordslib.factories;

import com.megaware.wordslib.beans.DictionaryEntry;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.RowProcessor;

/**
 *
 * @author Arun.K.R
 * @version 1.0
 */
public class DictionaryEntryRowProcessor implements RowProcessor {

    private static final long serialVersionUID = 333L;

    @Override
    public Object toBean(ResultSet rs, Class type) throws SQLException {
        DictionaryEntry de = (DictionaryEntry) this.newInstance(type);
        de.setId(rs.getInt("ID"));
        de.setWord(rs.getString("WORD"));
        de.setMeaning(rs.getString("MEANING"));
        de.setSynonyms(rs.getString("SYNONYMS"));

        try {
            Blob sound = rs.getBlob("SOUND");
            byte[] is = sound.getBytes((long) 1, (int) sound.length());
            de.setSound(is);
        } catch (Exception ex) {
            //driver couldn't handle this as Blob
            //fallback to default & slower method
            byte[] is = rs.getBytes("SOUND");
            de.setSound(is);
        }

        return de;
    }

    @Override
    @SuppressWarnings ( "unchecked" )
    public List toBeanList(ResultSet rs, Class type) throws SQLException {
        List<DictionaryEntry> results = new ArrayList<DictionaryEntry>();

        if (!rs.next()) {
            return results;
        }

        do {
            results.add((DictionaryEntry) this.toBean(rs, type));
        } while (rs.next());

        return results;
    }

    private Object newInstance(Class c) throws SQLException {
        try {
            return c.newInstance();
        } catch (InstantiationException e) {
            throw new SQLException(
                    "Cannot create " + c.getName() + ": " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new SQLException(
                    "Cannot create " + c.getName() + ": " + e.getMessage());
        }
    }

    @Override
    public Object[] toArray(ResultSet arg0) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map toMap(ResultSet arg0) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
