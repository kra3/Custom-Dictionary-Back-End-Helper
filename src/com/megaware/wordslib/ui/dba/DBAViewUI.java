/*
 * DBAViewUI.java
 *
 * Created on 31 October 2008, 04:50
 */

package com.megaware.wordslib.ui.dba;

import com.megaware.wordslib.beans.DictionaryEntry;
import com.megaware.wordslib.factories.DBManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.jlap;
import org.apache.derby.iapi.services.io.ArrayInputStream;
import org.javadev.AnimatingCardLayout;
import org.javadev.effects.FadeAnimation;

/**
 *
 * @author Arun.K.R
 * @version 1.0
 */
public class DBAViewUI extends javax.swing.JFrame{
    
    private static final long serialVersionUID = 333L;
    private AnimatingCardLayout acl = new AnimatingCardLayout();
    private TableMaker model = new TableMaker();
    private TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model) ;
    private DictionaryEntry de = new DictionaryEntry();
    private DBManager dba = DBManager.getInstance();   
        
    /** Creates new form DBAViewUI */
    public DBAViewUI() {
        initComponents();
        refreshTableView();
        tblDBWords.getTableHeader().setReorderingAllowed(false);
        acl.setAnimationDuration(500);
        pnlMainFunctions.setVisible(false);
        this.validate();
        acl.setAnimation(new FadeAnimation());
    } 
    
    private void refreshTableView(){
        model = new TableMaker();
        tblDBWords.setModel(model);
        sorter = new TableRowSorter<TableModel>(model);
        tblDBWords.setRowSorter(sorter);
        sorter.setComparator(1, null); //Sets Meaning column unsearchable
        sorter.setComparator(2, null); //Sets Synonyms coluumn unsearchable
    }
        
    private void attachSound() {
        soundFileChooser.setAcceptAllFileFilterUsed(false);
        soundFileChooser.addChoosableFileFilter(
                new FileNameExtensionFilter("*.mp3", "mp3"));
        int returnVal = soundFileChooser.showDialog(DBAViewUI.this, "Attach");
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            FileInputStream is;
            byte[] sound;
            File mp3file;
            try {
                mp3file = soundFileChooser.getSelectedFile();
                if(pnlSelRecordView.isShowing()){
                    txtSelSoundPath.setText(mp3file.getPath());
                }else{
                    txtNewSoundPath.setText(mp3file.getPath());
                }
                is = new FileInputStream(mp3file);
                sound = new byte[(int) mp3file.length()];
                is.read(sound);
                de.setSound(sound);
            } catch (IOException ex) {
                String tmp = "IO Error : mp3 attachment";
                System.err.println(tmp);
                Logger.getLogger(DBAViewUI.class.getName()).log(Level.SEVERE,
                                                                tmp, ex);
            }
        }
    }

    private void filterTable() {
        String filter = txtWordsFilter.getText().toUpperCase();
        if (filter.length() == 0) {
            sorter.setRowFilter(null);
        } else {
            try {
                sorter.setRowFilter(RowFilter.regexFilter(filter));
            } catch (PatternSyntaxException pse) {
                System.err.println("Bad regex pattern");
            }
        }
    }
    
    private void clearRecordView(){
        txtNewWord.setText("");
        txtNewMeaning.setText("");
        txtNewSynonym.setText("");
        txtNewSoundPath.setText("");
        txtSelWord.setText("");
        txtSelMeaning.setText("");
        txtSelSynonym.setText("");
        txtSelSoundPath.setText("");
    }

    private void invertSelectedBtnVisibility() {
        btnSelectedSave.setVisible(!btnSelectedSave.isVisible());
        //btnSelectedCancel must always visible
        btnSelectedCancel.setVisible(true);
        btnSelectedRemove.setVisible(!btnSelectedRemove.isVisible());
        btnSelectedEdit.setVisible(!btnSelectedEdit.isVisible());
        pnlSelectedCommandsS.revalidate();
    }
    
    private void invertSelRecordEditability(){
        //if word is changed it's identical to new record
        txtSelWord.setEditable(false);
        txtSelMeaning.setEditable(!txtSelMeaning.isEditable());
        txtSelSynonym.setEditable(!txtSelSynonym.isEditable());
        txtSelSoundPath.setEditable(!txtSelSoundPath.isEditable());
        btnSelBrowse.setEnabled(!btnSelBrowse.isEnabled());
        //btnSelPlay must always editable
        btnSelPlay.setEnabled(true);
        
        btnSelectedEdit.setEnabled(!btnSelectedEdit.isEnabled());
        btnSelectedRemove.setEnabled(!btnSelectedRemove.isEnabled());
        btnSelectedSave.setEnabled(!btnSelectedSave.isEnabled());
        //btnSelectedCancel must always editable
        btnSelectedCancel.setEnabled(true);
    }
    
    private void playClip(byte[] mp3){
        try {
            jlap.playMp3(new ArrayInputStream(mp3), 0, mp3.length, null);
        } catch (JavaLayerException ex) {
            Logger.getLogger(DBAViewUI.class.getName()).log(Level.SEVERE, null,
                                                            ex);
        }
    }
    
    private void displaySelRecord(){
        recordClear();
        int row = tblDBWords.getSelectedRow();
        de = dba.getSingleRecord((String)tblDBWords.getValueAt(row, 0));
        txtSelWord.setText(de.getWord());
        txtSelMeaning.setText(de.getMeaning());
        txtSelSynonym.setText(de.getSynonyms());
        if(de.getSound() == null){
            txtSelSoundPath.setText("No Attached Sound");
        }else{
            txtSelSoundPath.setText("From DataBase");
        }
    }
    
    private void recordClear(){
        //It's guaranteed that Id is used only for updation
        de.setWord("");
        de.setMeaning("");
        de.setSynonyms("");
        de.setSound(null);
    }
    
    private void makeChanges(){
        //word is not editable so it'll never change
        de.setMeaning(txtSelMeaning.getText());
        de.setSynonyms(txtSelSynonym.getText());
        //if sound is changed it automatically added via de.setSound() in attachSound()
    }
    
    private void createRecord(){
        //id will automatically created
        de.setWord(txtNewWord.getText());
        de.setMeaning(txtNewMeaning.getText());
        de.setSynonyms(txtNewSynonym.getText());
        //sound is automatically added via de.setSound() in attachSound()
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlNewRecord = new javax.swing.JPanel();
        pnlNewCommandsS = new javax.swing.JPanel();
        btnNewSave = new javax.swing.JButton();
        btnNewCancel = new javax.swing.JButton();
        pnlNewRecordView = new javax.swing.JPanel();
        lblNewWord = new javax.swing.JLabel();
        lblNewMeaning = new javax.swing.JLabel();
        lblNewSynonym = new javax.swing.JLabel();
        lblNewSound = new javax.swing.JLabel();
        txtNewWord = new javax.swing.JTextField();
        scrlPnlNewMeaning = new javax.swing.JScrollPane();
        txtNewMeaning = new javax.swing.JTextArea();
        scrlPnlNewSynonym = new javax.swing.JScrollPane();
        txtNewSynonym = new javax.swing.JTextArea();
        txtNewSoundPath = new javax.swing.JTextField();
        btnNewBrowse = new javax.swing.JButton();
        btnNewPlay = new javax.swing.JButton();
        pnlSelectedRecord = new javax.swing.JPanel();
        pnlSelectedCommandsS = new javax.swing.JPanel();
        btnSelectedSave = new javax.swing.JButton();
        btnSelectedEdit = new javax.swing.JButton();
        btnSelectedRemove = new javax.swing.JButton();
        btnSelectedCancel = new javax.swing.JButton();
        pnlSelRecordView = new javax.swing.JPanel();
        lblSelWord = new javax.swing.JLabel();
        lblSelMeaning = new javax.swing.JLabel();
        lblSelSynonym = new javax.swing.JLabel();
        lblSelSound = new javax.swing.JLabel();
        txtSelWord = new javax.swing.JTextField();
        scrlPnlSelMeaning = new javax.swing.JScrollPane();
        txtSelMeaning = new javax.swing.JTextArea();
        scrlPnlSelSynonym = new javax.swing.JScrollPane();
        txtSelSynonym = new javax.swing.JTextArea();
        txtSelSoundPath = new javax.swing.JTextField();
        btnSelBrowse = new javax.swing.JButton();
        btnSelPlay = new javax.swing.JButton();
        pnlEmpty = new javax.swing.JPanel();
        soundFileChooser = new javax.swing.JFileChooser();
        pnlMinFunctions = new javax.swing.JPanel();
        lblHeading = new javax.swing.JLabel();
        btnNewRecord = new javax.swing.JButton();
        txtWordsFilter = new javax.swing.JTextField();
        pnlMainFunctions = new javax.swing.JPanel();
        pnlTableView = new javax.swing.JPanel();
        scrlPnlDBContainer = new javax.swing.JScrollPane();
        tblDBWords = new javax.swing.JTable();

        pnlNewRecord.setLayout(new java.awt.BorderLayout());

        pnlNewCommandsS.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/megaware/wordslib/ui/dba/Bundle"); // NOI18N
        btnNewSave.setText(bundle.getString("DBAViewUI.btnNewSave.text")); // NOI18N
        btnNewSave.setDoubleBuffered(true);
        btnNewSave.setPreferredSize(new java.awt.Dimension(90, 35));
        btnNewSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewSaveActionPerformed(evt);
            }
        });
        pnlNewCommandsS.add(btnNewSave);

        btnNewCancel.setText(bundle.getString("DBAViewUI.btnNewCancel.text")); // NOI18N
        btnNewCancel.setPreferredSize(new java.awt.Dimension(90, 35));
        btnNewCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewCancelActionPerformed(evt);
            }
        });
        pnlNewCommandsS.add(btnNewCancel);

        pnlNewRecord.add(pnlNewCommandsS, java.awt.BorderLayout.SOUTH);

        lblNewWord.setText(bundle.getString("DBAViewUI.lblNewWord.text")); // NOI18N

        lblNewMeaning.setText(bundle.getString("DBAViewUI.lblNewMeaning.text")); // NOI18N

        lblNewSynonym.setText(bundle.getString("DBAViewUI.lblNewSynonym.text")); // NOI18N

        lblNewSound.setText(bundle.getString("DBAViewUI.lblNewSound.text")); // NOI18N

        txtNewWord.setText(bundle.getString("DBAViewUI.txtNewWord.text")); // NOI18N
        txtNewWord.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                txtNewWordCaretUpdate(evt);
            }
        });

        txtNewMeaning.setColumns(20);
        txtNewMeaning.setLineWrap(true);
        txtNewMeaning.setRows(5);
        txtNewMeaning.setTabSize(4);
        txtNewMeaning.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                txtNewMeaningCaretUpdate(evt);
            }
        });
        scrlPnlNewMeaning.setViewportView(txtNewMeaning);

        txtNewSynonym.setColumns(20);
        txtNewSynonym.setLineWrap(true);
        txtNewSynonym.setRows(2);
        txtNewSynonym.setTabSize(4);
        scrlPnlNewSynonym.setViewportView(txtNewSynonym);

        txtNewSoundPath.setText(bundle.getString("DBAViewUI.txtNewSoundPath.text")); // NOI18N

        btnNewBrowse.setText(bundle.getString("DBAViewUI.btnNewBrowse.text")); // NOI18N
        btnNewBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewBrowseActionPerformed(evt);
            }
        });

        btnNewPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/megaware/wordslib/resources/icons/24audio-x-generic.png"))); // NOI18N
        btnNewPlay.setText(bundle.getString("DBAViewUI.btnNewPlay.text")); // NOI18N
        btnNewPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewPlayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlNewRecordViewLayout = new javax.swing.GroupLayout(pnlNewRecordView);
        pnlNewRecordView.setLayout(pnlNewRecordViewLayout);
        pnlNewRecordViewLayout.setHorizontalGroup(
            pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNewRecordViewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlNewRecordViewLayout.createSequentialGroup()
                        .addComponent(lblNewSound)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtNewSoundPath, javax.swing.GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewBrowse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNewPlay))
                    .addGroup(pnlNewRecordViewLayout.createSequentialGroup()
                        .addGroup(pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblNewMeaning)
                            .addComponent(lblNewWord)
                            .addComponent(lblNewSynonym))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrlPnlNewSynonym, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                            .addComponent(scrlPnlNewMeaning, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                            .addComponent(txtNewWord, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE))))
                .addContainerGap())
        );

        pnlNewRecordViewLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnNewBrowse, btnNewPlay});

        pnlNewRecordViewLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblNewMeaning, lblNewSound, lblNewSynonym, lblNewWord});

        pnlNewRecordViewLayout.setVerticalGroup(
            pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNewRecordViewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblNewWord, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNewWord, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNewMeaning)
                    .addComponent(scrlPnlNewMeaning))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblNewSynonym)
                    .addComponent(scrlPnlNewSynonym, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlNewRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lblNewSound)
                        .addComponent(btnNewPlay)
                        .addComponent(btnNewBrowse))
                    .addGroup(pnlNewRecordViewLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(txtNewSoundPath, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pnlNewRecordViewLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblNewMeaning, lblNewSound, lblNewSynonym, lblNewWord});

        pnlNewRecordViewLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnNewBrowse, btnNewPlay});

        pnlNewRecord.add(pnlNewRecordView, java.awt.BorderLayout.CENTER);

        pnlSelectedCommandsS.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnSelectedSave.setText(bundle.getString("DBAViewUI.btnSelectedSave.text")); // NOI18N
        btnSelectedSave.setEnabled(false);
        btnSelectedSave.setPreferredSize(new java.awt.Dimension(90, 35));
        btnSelectedSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectedSaveActionPerformed(evt);
            }
        });
        pnlSelectedCommandsS.add(btnSelectedSave);

        btnSelectedEdit.setText(bundle.getString("DBAViewUI.btnSelectedEdit.text")); // NOI18N
        btnSelectedEdit.setPreferredSize(new java.awt.Dimension(90, 35));
        btnSelectedEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectedEditActionPerformed(evt);
            }
        });
        pnlSelectedCommandsS.add(btnSelectedEdit);

        btnSelectedRemove.setText(bundle.getString("DBAViewUI.btnSelectedRemove.text")); // NOI18N
        btnSelectedRemove.setPreferredSize(new java.awt.Dimension(90, 35));
        btnSelectedRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectedRemoveActionPerformed(evt);
            }
        });
        pnlSelectedCommandsS.add(btnSelectedRemove);

        btnSelectedCancel.setText(bundle.getString("DBAViewUI.btnSelectedCancel.text")); // NOI18N
        btnSelectedCancel.setPreferredSize(new java.awt.Dimension(90, 35));
        btnSelectedCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectedCancelActionPerformed(evt);
            }
        });
        pnlSelectedCommandsS.add(btnSelectedCancel);

        lblSelWord.setText(bundle.getString("DBAViewUI.lblSelWord.text")); // NOI18N

        lblSelMeaning.setText(bundle.getString("DBAViewUI.lblSelMeaning.text")); // NOI18N

        lblSelSynonym.setText(bundle.getString("DBAViewUI.lblSelSynonym.text")); // NOI18N

        lblSelSound.setText(bundle.getString("DBAViewUI.lblSelSound.text")); // NOI18N

        txtSelWord.setEditable(false);
        txtSelWord.setText(bundle.getString("DBAViewUI.txtSelWord.text")); // NOI18N
        txtSelWord.setDoubleBuffered(true);

        txtSelMeaning.setColumns(20);
        txtSelMeaning.setEditable(false);
        txtSelMeaning.setLineWrap(true);
        txtSelMeaning.setRows(5);
        txtSelMeaning.setTabSize(4);
        txtSelMeaning.setDoubleBuffered(true);
        txtSelMeaning.addCaretListener(new javax.swing.event.CaretListener() {
            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                txtSelMeaningCaretUpdate(evt);
            }
        });
        scrlPnlSelMeaning.setViewportView(txtSelMeaning);

        txtSelSynonym.setColumns(20);
        txtSelSynonym.setEditable(false);
        txtSelSynonym.setLineWrap(true);
        txtSelSynonym.setRows(2);
        txtSelSynonym.setTabSize(4);
        txtSelSynonym.setDoubleBuffered(true);
        scrlPnlSelSynonym.setViewportView(txtSelSynonym);

        txtSelSoundPath.setEditable(false);
        txtSelSoundPath.setText(bundle.getString("DBAViewUI.txtSelSoundPath.text")); // NOI18N
        txtSelSoundPath.setDoubleBuffered(true);

        btnSelBrowse.setText(bundle.getString("DBAViewUI.btnSelBrowse.text")); // NOI18N
        btnSelBrowse.setDoubleBuffered(true);
        btnSelBrowse.setEnabled(false);
        btnSelBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelBrowseActionPerformed(evt);
            }
        });

        btnSelPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/megaware/wordslib/resources/icons/24audio-x-generic.png"))); // NOI18N
        btnSelPlay.setText(bundle.getString("DBAViewUI.btnSelPlay.text")); // NOI18N
        btnSelPlay.setDoubleBuffered(true);
        btnSelPlay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelPlayActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlSelRecordViewLayout = new javax.swing.GroupLayout(pnlSelRecordView);
        pnlSelRecordView.setLayout(pnlSelRecordViewLayout);
        pnlSelRecordViewLayout.setHorizontalGroup(
            pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSelRecordViewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSelRecordViewLayout.createSequentialGroup()
                            .addComponent(lblSelWord, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(6, 6, 6))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlSelRecordViewLayout.createSequentialGroup()
                            .addComponent(lblSelMeaning, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(6, 6, 6)))
                    .addComponent(lblSelSound, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblSelSynonym, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlSelRecordViewLayout.createSequentialGroup()
                        .addComponent(txtSelSoundPath, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSelBrowse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSelPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrlPnlSelMeaning, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                    .addComponent(txtSelWord, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE)
                    .addComponent(scrlPnlSelSynonym, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );

        pnlSelRecordViewLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btnSelBrowse, btnSelPlay});

        pnlSelRecordViewLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lblSelMeaning, lblSelSound, lblSelSynonym, lblSelWord});

        pnlSelRecordViewLayout.setVerticalGroup(
            pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSelRecordViewLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSelWord, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSelWord))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblSelMeaning)
                    .addComponent(scrlPnlSelMeaning))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrlPnlSelSynonym)
                    .addComponent(lblSelSynonym))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlSelRecordViewLayout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addGroup(pnlSelRecordViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnSelBrowse)
                            .addComponent(lblSelSound)
                            .addComponent(btnSelPlay)))
                    .addGroup(pnlSelRecordViewLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSelSoundPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pnlSelRecordViewLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {btnSelBrowse, btnSelPlay, txtSelSoundPath});

        pnlSelRecordViewLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblSelMeaning, lblSelSound, lblSelSynonym, lblSelWord});

        javax.swing.GroupLayout pnlSelectedRecordLayout = new javax.swing.GroupLayout(pnlSelectedRecord);
        pnlSelectedRecord.setLayout(pnlSelectedRecordLayout);
        pnlSelectedRecordLayout.setHorizontalGroup(
            pnlSelectedRecordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlSelectedCommandsS, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 547, Short.MAX_VALUE)
            .addComponent(pnlSelRecordView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlSelectedRecordLayout.setVerticalGroup(
            pnlSelectedRecordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSelectedRecordLayout.createSequentialGroup()
                .addComponent(pnlSelRecordView, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSelectedCommandsS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlEmptyLayout = new javax.swing.GroupLayout(pnlEmpty);
        pnlEmpty.setLayout(pnlEmptyLayout);
        pnlEmptyLayout.setHorizontalGroup(
            pnlEmptyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        pnlEmptyLayout.setVerticalGroup(
            pnlEmptyLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        soundFileChooser.setDialogTitle(bundle.getString("DBAViewUI.soundFileChooser.dialogTitle")); // NOI18N
        soundFileChooser.setDoubleBuffered(true);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(bundle.getString("DBAViewUI.title")); // NOI18N
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        pnlMinFunctions.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        lblHeading.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblHeading.setText(bundle.getString("DBAViewUI.lblHeading.text")); // NOI18N

        btnNewRecord.setText(bundle.getString("DBAViewUI.btnNewRecord.text")); // NOI18N
        btnNewRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewRecordActionPerformed(evt);
            }
        });

        txtWordsFilter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtWordsFilterKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtWordsFilterKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout pnlMinFunctionsLayout = new javax.swing.GroupLayout(pnlMinFunctions);
        pnlMinFunctions.setLayout(pnlMinFunctionsLayout);
        pnlMinFunctionsLayout.setHorizontalGroup(
            pnlMinFunctionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMinFunctionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMinFunctionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMinFunctionsLayout.createSequentialGroup()
                        .addComponent(txtWordsFilter, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnNewRecord, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addComponent(lblHeading, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 768, Short.MAX_VALUE)))
        );
        pnlMinFunctionsLayout.setVerticalGroup(
            pnlMinFunctionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlMinFunctionsLayout.createSequentialGroup()
                .addComponent(lblHeading, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlMinFunctionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtWordsFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnNewRecord))
                .addContainerGap())
        );

        getContentPane().add(pnlMinFunctions, java.awt.BorderLayout.NORTH);

        pnlMainFunctions.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout pnlMainFunctionsLayout = new javax.swing.GroupLayout(pnlMainFunctions);
        pnlMainFunctions.setLayout(pnlMainFunctionsLayout);
        pnlMainFunctionsLayout.setHorizontalGroup(
            pnlMainFunctionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 772, Short.MAX_VALUE)
        );
        pnlMainFunctionsLayout.setVerticalGroup(
            pnlMainFunctionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 169, Short.MAX_VALUE)
        );

        getContentPane().add(pnlMainFunctions, java.awt.BorderLayout.SOUTH);
        //acl.setAnimationDuration(2500);
        pnlMainFunctions.setLayout(acl);
        pnlMainFunctions.add(pnlEmpty, "EMPTY");
        pnlMainFunctions.add(pnlNewRecord, "NEW");
        pnlMainFunctions.add(pnlSelectedRecord, "SELECTED");

        tblDBWords.setAutoCreateRowSorter(true);
        tblDBWords.setModel(model);
        tblDBWords.setDoubleBuffered(true);
        tblDBWords.setFillsViewportHeight(true);
        tblDBWords.setRowSorter(sorter );
        tblDBWords.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblDBWordsMouseClicked(evt);
            }
        });
        scrlPnlDBContainer.setViewportView(tblDBWords);

        javax.swing.GroupLayout pnlTableViewLayout = new javax.swing.GroupLayout(pnlTableView);
        pnlTableView.setLayout(pnlTableViewLayout);
        pnlTableViewLayout.setHorizontalGroup(
            pnlTableViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 776, Short.MAX_VALUE)
            .addGroup(pnlTableViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlTableViewLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrlPnlDBContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 764, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        pnlTableViewLayout.setVerticalGroup(
            pnlTableViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 534, Short.MAX_VALUE)
            .addGroup(pnlTableViewLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlTableViewLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(scrlPnlDBContainer, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        getContentPane().add(pnlTableView, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewCancelActionPerformed
        btnNewSave.setVisible(false);
        btnNewCancel.setVisible(false);
        pnlNewCommandsS.revalidate();
        clearRecordView();
        pnlMainFunctions.setVisible(false);
        this.validate();
}//GEN-LAST:event_btnNewCancelActionPerformed

    private void btnSelectedSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectedSaveActionPerformed
        btnSelectedCancel.setVisible(false);
        btnSelectedSave.setVisible(false);
        pnlSelectedCommandsS.revalidate();
        makeChanges();
        dba.update(de);
        refreshTableView();
        invertSelRecordEditability();
        clearRecordView();
        pnlMainFunctions.setVisible(false);
        this.validate();
}//GEN-LAST:event_btnSelectedSaveActionPerformed

    private void txtWordsFilterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtWordsFilterKeyPressed
        filterTable();
    }//GEN-LAST:event_txtWordsFilterKeyPressed

    private void txtWordsFilterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtWordsFilterKeyReleased
        filterTable();
    }//GEN-LAST:event_txtWordsFilterKeyReleased

    private void btnNewSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewSaveActionPerformed
        btnNewCancel.setVisible(false);
        btnNewSave.setVisible(false);
        pnlNewCommandsS.revalidate();
        createRecord();
        dba.insert(de);
        refreshTableView();
        clearRecordView();
        pnlMainFunctions.setVisible(false);
        this.validate();
}//GEN-LAST:event_btnNewSaveActionPerformed

    private void btnNewRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewRecordActionPerformed
        pnlMainFunctions.setVisible(true);
        this.validate();
        clearRecordView();
        recordClear();
        acl.show(pnlMainFunctions, "NEW");
        btnNewSave.setVisible(true);
        btnNewCancel.setVisible(true);
        pnlNewCommandsS.revalidate();
        btnNewSave.setEnabled(false);
    }//GEN-LAST:event_btnNewRecordActionPerformed

    private void tblDBWordsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblDBWordsMouseClicked
        pnlMainFunctions.setVisible(true);
        this.validate();
        clearRecordView();
        int selRow = tblDBWords.getSelectedRow();
        if(selRow != -1){
            acl.show(pnlMainFunctions, "SELECTED");
            btnSelectedEdit.setVisible(true);
            btnSelectedRemove.setVisible(true);//GEN-LAST:event_tblDBWordsMouseClicked
            btnSelectedCancel.setVisible(true);
            btnSelectedSave.setVisible(false);
            pnlSelectedCommandsS.revalidate();
            displaySelRecord();
        }
    }                                       

    private void btnSelectedCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectedCancelActionPerformed
        if(btnSelectedRemove.isVisible()){
            clearRecordView();
            pnlMainFunctions.setVisible(false);
            this.validate();
        }else{
            invertSelectedBtnVisibility();
            invertSelRecordEditability();
        }
    }//GEN-LAST:event_btnSelectedCancelActionPerformed

    private void btnSelectedEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectedEditActionPerformed
        invertSelectedBtnVisibility();
        invertSelRecordEditability();  
}//GEN-LAST:event_btnSelectedEditActionPerformed

    private void btnSelBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelBrowseActionPerformed
        attachSound();
    }//GEN-LAST:event_btnSelBrowseActionPerformed

    private void btnSelPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelPlayActionPerformed
        byte[] phonetics = de.getSound();
        if(phonetics == null){
            JOptionPane.showMessageDialog(this,
                    "There is no added sound. Please add a sound to play it",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        } else{
            playClip(phonetics);
        }
    }//GEN-LAST:event_btnSelPlayActionPerformed

    private void btnNewBrowseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewBrowseActionPerformed
        attachSound();
    }//GEN-LAST:event_btnNewBrowseActionPerformed

    private void btnNewPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewPlayActionPerformed
        byte[] phonetics = de.getSound();
        if(phonetics == null){
            JOptionPane.showMessageDialog(this,
                    "There is no added sound. Please add a sound to play it",
                    "Information", JOptionPane.INFORMATION_MESSAGE);
        } else{
            playClip(phonetics);
        }
    }//GEN-LAST:event_btnNewPlayActionPerformed

    private void btnSelectedRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectedRemoveActionPerformed
        btnSelectedRemove.setVisible(false);
        btnSelectedEdit.setVisible(false);
        pnlSelectedCommandsS.revalidate();
        dba.remove(de.getWord());
        refreshTableView();
        clearRecordView();
        pnlMainFunctions.setVisible(false);
        this.validate();
    }//GEN-LAST:event_btnSelectedRemoveActionPerformed

    private void txtSelMeaningCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtSelMeaningCaretUpdate
        if(txtSelMeaning.getText().trim().isEmpty()){
            //since meaning can't be null 
            btnSelectedSave.setEnabled(false);
        }else{
            //we need to change Save button to enabled state only if it's visible
            if(btnSelectedSave.isVisible() && !btnSelectedSave.isEnabled()){
                btnSelectedSave.setEnabled(true);
            }
        }
    }//GEN-LAST:event_txtSelMeaningCaretUpdate

    private void txtNewWordCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtNewWordCaretUpdate
        if(txtNewWord.getText().trim().isEmpty()){
            //since word can't be null
            btnNewSave.setEnabled(false);
        }else {
            if(!txtNewMeaning.getText().trim().isEmpty()){
                btnNewSave.setEnabled(true);
            }
        }
        
    }//GEN-LAST:event_txtNewWordCaretUpdate

    private void txtNewMeaningCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtNewMeaningCaretUpdate
        if(txtNewMeaning.getText().trim().isEmpty()){
            //since meaning can't be null
            btnNewSave.setEnabled(false);
        }else{
            if(!txtNewWord.getText().trim().isEmpty()){
                btnNewSave.setEnabled(true);
            }
        }
    }//GEN-LAST:event_txtNewMeaningCaretUpdate
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
                UIManager.setLookAndFeel("org.jvnet.substance.SubstanceDefaultLookAndFeel");
                java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    new DBAViewUI().setVisible(true);
                }
            });
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DBAViewUI.class.getName()).log(Level.SEVERE, null,
                                                            ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(DBAViewUI.class.getName()).log(Level.SEVERE, null,
                                                            ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(DBAViewUI.class.getName()).log(Level.SEVERE, null,
                                                            ex);
        } catch (UnsupportedLookAndFeelException ex) {
            Logger.getLogger(DBAViewUI.class.getName()).log(Level.SEVERE, null,
                                                            ex);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnNewBrowse;
    private javax.swing.JButton btnNewCancel;
    private javax.swing.JButton btnNewPlay;
    private javax.swing.JButton btnNewRecord;
    private javax.swing.JButton btnNewSave;
    private javax.swing.JButton btnSelBrowse;
    private javax.swing.JButton btnSelPlay;
    private javax.swing.JButton btnSelectedCancel;
    private javax.swing.JButton btnSelectedEdit;
    private javax.swing.JButton btnSelectedRemove;
    private javax.swing.JButton btnSelectedSave;
    private javax.swing.JLabel lblHeading;
    private javax.swing.JLabel lblNewMeaning;
    private javax.swing.JLabel lblNewSound;
    private javax.swing.JLabel lblNewSynonym;
    private javax.swing.JLabel lblNewWord;
    private javax.swing.JLabel lblSelMeaning;
    private javax.swing.JLabel lblSelSound;
    private javax.swing.JLabel lblSelSynonym;
    private javax.swing.JLabel lblSelWord;
    private javax.swing.JPanel pnlEmpty;
    private javax.swing.JPanel pnlMainFunctions;
    private javax.swing.JPanel pnlMinFunctions;
    private javax.swing.JPanel pnlNewCommandsS;
    private javax.swing.JPanel pnlNewRecord;
    private javax.swing.JPanel pnlNewRecordView;
    private javax.swing.JPanel pnlSelRecordView;
    private javax.swing.JPanel pnlSelectedCommandsS;
    private javax.swing.JPanel pnlSelectedRecord;
    private javax.swing.JPanel pnlTableView;
    private javax.swing.JScrollPane scrlPnlDBContainer;
    private javax.swing.JScrollPane scrlPnlNewMeaning;
    private javax.swing.JScrollPane scrlPnlNewSynonym;
    private javax.swing.JScrollPane scrlPnlSelMeaning;
    private javax.swing.JScrollPane scrlPnlSelSynonym;
    private javax.swing.JFileChooser soundFileChooser;
    private javax.swing.JTable tblDBWords;
    private javax.swing.JTextArea txtNewMeaning;
    private javax.swing.JTextField txtNewSoundPath;
    private javax.swing.JTextArea txtNewSynonym;
    private javax.swing.JTextField txtNewWord;
    private javax.swing.JTextArea txtSelMeaning;
    private javax.swing.JTextField txtSelSoundPath;
    private javax.swing.JTextArea txtSelSynonym;
    private javax.swing.JTextField txtSelWord;
    private javax.swing.JTextField txtWordsFilter;
    // End of variables declaration//GEN-END:variables
    
}
