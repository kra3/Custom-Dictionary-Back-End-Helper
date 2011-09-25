package com.megaware.wordslib.beans;

/**
 * This is a java bean for the Record of "ENGDIC".WORDS table
 * Do not alter variable names and methods unless you know what are you doing
 * NB : change varibale & method names if filed name/names of the table changes.
 * @author Arun.K.R
 * @version 1.0
 */
public class DictionaryEntry {
    private int id;
    private String word;
    private String meaning;
    private String synonyms;
    private byte[] sound;
    
    
    public DictionaryEntry() {
    }
   
    public int getId() {
        return id;
    }

    public void setId(int id){
        this.id = id;
    }
    
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
    
    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public byte[] getSound() {
        return sound;
    }

    public void setSound(byte[] soundClip) {
        this.sound = soundClip;
    }

}
