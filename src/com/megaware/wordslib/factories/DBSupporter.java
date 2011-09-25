/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.megaware.wordslib.factories;

import com.megaware.wordslib.beans.DictionaryEntry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author Arun.K.R
 * @verson 1.0
 */
public class DBSupporter{

     private static final long serialVersionUID = 333L;
     
    public int update(Connection conn, String sql, DictionaryEntry de, boolean change) 
            throws SQLException {
        
        PreparedStatement stmt = null;
        int rows = 0;
        
        try {
            stmt = this.prepareStatement(conn, sql);
            this.fillStatement(stmt, de, change);
            rows = stmt.executeUpdate();
        } catch (SQLException e) {
            this.rethrow(e, sql, de);

        } finally {
            DbUtils.close(stmt);
        }

        return rows;
       
    }

    
    protected void fillStatement(PreparedStatement psmt, DictionaryEntry de, boolean change) 
            throws SQLException {
        
        //InputStream in = de.getSound();
        byte[] in = de.getSound();

        if (de == null) {
            return;
        }

        if (change) {
            psmt.setInt(5, de.getId());
        } 
        
        psmt.setString(1, de.getWord().toUpperCase().trim());
        psmt.setString(2, de.getMeaning().trim());
        psmt.setString(3, de.getSynonyms().trim());
        psmt.setBytes(4, in);
        //SerialBlob a = new SerialBlob(in);
        //psmt.setBlob(colNum[4], a);
       
       
    }

    private PreparedStatement prepareStatement(Connection conn, String sql)
            throws SQLException {
        
        return conn.prepareStatement(sql);
    }

    protected void rethrow(SQLException cause, String sql, DictionaryEntry de)
        throws SQLException {
    
        StringBuffer msg = new StringBuffer(cause.getMessage());

        msg.append(" \n Query: ");
        msg.append(sql);
        msg.append(" \n Parameters: ");
        
        if (de == null) {
            msg.append("[]");
        } else {
            msg.append( "{" + "\t" +
                        "ID : " + de.getId() + "\t" +
                        "WORD : " + de.getWord() + "\t" +
                        "MEANING : " + de.getMeaning() + "\t" +
                        "SYNONYMS : " + de.getSynonyms() + "\t" +
                        "}"
                      );
        }

        SQLException e = new SQLException(msg.toString(), cause.getSQLState(),
                cause.getErrorCode());
        e.setNextException(cause);

        throw e;
    }
  
}
