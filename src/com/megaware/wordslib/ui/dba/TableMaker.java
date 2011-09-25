package com.megaware.wordslib.ui.dba;

import com.megaware.wordslib.beans.DictionaryEntry;
import com.megaware.wordslib.factories.DBManager;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Arun.K.R
 * @version 1.0
 */
public class TableMaker extends AbstractTableModel {

    private static final long serialVersionUID = 333L;
    Object[][] contents;
    String[] columnNames;  
        
    public TableMaker() {
        super();
        
        // creating Column Headers...
        columnNames = new String[3];
        columnNames[0] = "WORD";
        columnNames[1] = "MEANING";
        columnNames[2] = "SYNONYMS";
        // Column Headers created.
       
        // creating rows
        getTableContents ();
        //Rows created.
 
    }

    @Override
    public int getRowCount() {
        return contents.length;
    }

    @Override
    public int getColumnCount() {
        
        if (contents.length == 0) {
            return 0;
        } else {
            return contents[0].length;
        } 

    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return contents [rowIndex][columnIndex]; 
    }

    private void getTableContents(){
        DBManager dba = DBManager.getInstance();
        List<DictionaryEntry> lstOfAllRec =  dba.getAllRecords();
        DictionaryEntry de = null;
        contents = new String[lstOfAllRec.size()][3];
        for(int i = 0; i < lstOfAllRec.size(); ++i){
            de = lstOfAllRec.get(i);
            contents[i][0] = de.getWord();
            contents[i][1] = de.getMeaning();
            contents[i][2] = de.getSynonyms();
        }
        lstOfAllRec.clear();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

}
