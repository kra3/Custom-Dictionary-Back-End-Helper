package com.megaware.wordslib.methods;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * class that contains methods that find utilization all over but not fit into an existing class
 * //sandu asumes you have plenty of them to include here.
 * @author Arun.K.R
 * @auther Sandeep.G.R 
 * @version 1.0
 */
public class GeneralMethods {
     private static final long serialVersionUID = 333L;
    
    public static String getApplicationPath() {
        String path = "." + java.io.File.separator;
        try {
            path = new File(path).getCanonicalPath() + java.io.File.separator;
        } catch (IOException ex) {
            Logger.getLogger(GeneralMethods.class.getName()).log(Level.SEVERE, null, ex);
        }
        return path;
    }

    public static File createTmpMP3File(byte[] b) {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File tmpdirectory = new File(tmpdir);
        File tmpSoundFile = null;
        DataOutputStream dos = null;
        
        try{
            tmpSoundFile = File.createTempFile("phonetic", ".mp3", tmpdirectory);
            tmpSoundFile.deleteOnExit();
        }catch (IOException ioe){
            Logger.getLogger(GeneralMethods.class.getName()).log(Level.SEVERE, null, ioe);
        }
       
        try {
            dos = new DataOutputStream(new FileOutputStream(tmpSoundFile));
            dos.write(b);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GeneralMethods.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GeneralMethods.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                dos.close();
            } catch (IOException ex) {
                Logger.getLogger(GeneralMethods.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return tmpSoundFile;
    }
    
    public static InputStream covertToInputStream(byte[] b){
        return new ByteArrayInputStream(b);
    }
    
    private GeneralMethods() {
    }
}
