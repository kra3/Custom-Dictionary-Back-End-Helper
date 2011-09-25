package com.megaware.wordslib.factories;

import com.megaware.wordslib.methods.GeneralMethods;
import com.megaware.wordslib.beans.DictionaryEntry;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryLoader;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

/**
 * Class that manages all database related tasks for this appplication
 * Assumes Derby database is used in embedded mode.
 * Only a singleton instance is available.
 * @author Arun.K.R
 * @version 1.0
 */
public class DBManager {

    private static final String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static String url = null;
    private static final String dbName = "EngDic";
    private static final String username = "engdic";
    private static final String password = "engdic";
    private Connection conn;
    private Statement smt;
    private ResultSetHandler rsh;
    private QueryRunner qr;
    private DBSupporter updateQR;
    private QueryLoader ql;
    private Map queries;
    private static DBManager singletoneInstance = new DBManager();

    private DBManager() {

        if (!DbUtils.loadDriver(driver)) {
            System.err.println("Database connection failed");
        }

        System.out.print("Application Path : " + GeneralMethods.getApplicationPath());
        System.setProperty("derby.system.home",
                GeneralMethods.getApplicationPath());
        qr = new QueryRunner();
        ql = QueryLoader.instance();
        updateQR = new DBSupporter();
        try {
            //care should be taken: if pakage name changes load()'s parameter should also be changed.
            queries = ql.load("/com/megaware/wordslib/factories/Queries.properties");
            printLoaded();
        } catch (IOException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static DBManager getInstance() {
        return singletoneInstance;
    }

    public boolean insert(DictionaryEntry de) {

        url = "jdbc:derby:" + dbName;
        createConnection(url);

        try {
            updateQR.update(conn, (String) queries.get("insert"), de, false);
        } catch (SQLException ex) {

            do {
                System.out.println(ex.getErrorCode() + ex.getSQLState());
            } while (ex.getNextException() != null);

            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);

            return false;

        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }

        return true;
    }

    public boolean remove(String word) {

        url = "jdbc:derby:" + dbName;
        createConnection(url);

        try {
            qr.update(conn, (String) queries.get("remove"), word.toUpperCase().trim());
        } catch (SQLException ex) {

            do {
                System.out.println(ex.getErrorCode() + ex.getSQLState());
            } while (ex.getNextException() != null);

            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);

            return false;

        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }

        return true;
    }

    public boolean update(DictionaryEntry de) {

        url = "jdbc:derby:" + dbName;
        createConnection(url);

        try {
            updateQR.update(conn, (String) queries.get("update"),
                    de, true);
        } catch (SQLException ex) {

            do {
                System.out.println(ex.getErrorCode() + ex.getSQLState());
            } while (ex.getNextException() != null);

            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);

            return false;

        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }

        return true;
    }

    /*
     * from now on url will change. 
     * Becuase Database will be jarred and supplay as read only.
     * so url will change to read from jar.
     * It's jdbc:derby:jar:(path/data.jar)EngDic OR If jar is in class path
     * jdbc:derby:/EngDic
     */
    /**
     * WARNING : Use of this method strongly discouraged
     * @param sql the sql statement you need to execute
     * @return rs <code>ResultSet</code> for passed in sql statement
     * @deprecated you may end up in trouble if your query includes Sound field
     *              which is Blob Type and cause you pain.
     */
    @Deprecated
    public ResultSet executeQuery(String sql) {
        url = "jdbc:derby:" + dbName;
        //change url before deployement
        createConnection(url);
        ResultSet rs = null;
        try {
            smt = conn.createStatement();
            rs = smt.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            DbUtils.closeQuietly(smt);
            DbUtils.closeQuietly(conn);
        }

        return rs;
    }

    /**
     * WARNING : Use of this method strongly discouraged
     * @param psmt the prepared statement ready to run.
     * @return
     * @deprecated
     */
    @Deprecated
    public ResultSet executeQuery(PreparedStatement psmt) {
        url = "jdbc:derby:" + dbName;
        //change url before deployement
        createConnection(url);
        ResultSet rs = null;
        try {
            rs = psmt.executeQuery();
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            DbUtils.closeQuietly(smt);
            DbUtils.closeQuietly(conn);
        }

        return rs;
    }

    /**
     * WARNING : Use of this method strongly discouraged
     * You cann't use it to retrive Blob entry
     * @param id primarykey of table to use in where clause
     * @param columnName field need to be returned
     * @return field the field you requested 
     * @deprecated there is no need for this method.
     */
    @Deprecated
    public Object getSingleField(int id,
            String columnName) {
        url = "jdbc:derby:" + dbName;
        createConnection(url);
        rsh = new ScalarHandler(columnName.toUpperCase().trim());
        Object field = null;

        try {
            field = qr.query(conn, (String) queries.get("singleField"), id, rsh);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            DbUtils.closeQuietly(conn);
        }

        return field;
    }

    /**
     * Bind this method with main app's search box and word list
     * @param SearchFilter search string to use as LIKE parameter. If null returns all words
     * @param numOfReqWords how many words needed to display in the list. if 0 return all 
     * @return String[] of words column
     */
    @SuppressWarnings({"unchecked", "deprecation"})
    public List<String> getWords(String SearchFilter, int numOfReqWords) {
        url = "jdbc:derby:" + dbName;
        createConnection(url);
        //column to retrive is WORD
        rsh = new ColumnListHandler("WORD");
        List<String> words = new ArrayList<String>();
        if (SearchFilter != null) {
            SearchFilter = SearchFilter.toUpperCase();
        }

        try {
            if (SearchFilter != null) {
                words = (ArrayList<String>) qr.query(conn,
                        (String) queries.get("searchFiltered"),
                        SearchFilter.trim(), rsh);
            } else {
                words = (ArrayList<String>) qr.query(conn,
                        (String) queries.get("allwords"),
                        rsh);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            DbUtils.closeQuietly(conn);
        }

        if (numOfReqWords != 0) {
            List<String> reqWords = new ArrayList<String>();
            for (int i = 0; i < numOfReqWords; ++i) {
                reqWords.add(i, words.get(i));
            }
            return reqWords;
        }

        return words;
    }

    /**
     * use: A word in the list is clicked so call this method and the corresponding record will get 
     * @param word The filed word to search with
     * @return
     */
    @SuppressWarnings("deprecation")
    public DictionaryEntry getSingleRecord(String word) {
        url = "jdbc:derby:" + dbName;
        createConnection(url);
        rsh = new BeanHandler(DictionaryEntry.class,
                new DictionaryEntryRowProcessor());
        DictionaryEntry tmp = new DictionaryEntry();
        try {
            tmp = (DictionaryEntry) qr.query(conn,
                    (String) queries.get("singleRecord"),
                    word.toUpperCase().trim(), rsh);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            DbUtils.closeQuietly(conn);
        }

        return tmp;
    }

    @SuppressWarnings("unchecked")
    public List<DictionaryEntry> getAllRecords() {
        url = "jdbc:derby:" + dbName;
        createConnection(url);
        rsh = new BeanListHandler(DictionaryEntry.class,
                new DictionaryEntryRowProcessor());
        List<DictionaryEntry> tmp = new ArrayList<DictionaryEntry>();
        try {
            tmp = (List<DictionaryEntry>) qr.query(conn,
                    (String) queries.get("allRecords"),
                    rsh);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null,
                    ex);
        } finally {
            DbUtils.closeQuietly(conn);
        }

        return tmp;
    }

    private void createConnection(String url) {
        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            Logger.getLogger(DBManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // A method to print loaded queries. Just for test purpose.
    private void printLoaded() {
        System.out.println("Queries Loaded...");
        System.out.println(queries.get("insert"));
        System.out.println(queries.get("remove"));
        System.out.println(queries.get("update"));
        System.out.println(queries.get("singleField"));
        System.out.println(queries.get("allwords"));
        System.out.println(queries.get("searchFiltered"));
        System.out.println(queries.get("singleRecord"));
        System.out.println(queries.get("allRecords"));
    }
}
