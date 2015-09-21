package secure;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.crypto.Cipher;

import database.DBUtil;
import util.Constantes;

public class DecryptOTASS {
    private DBUtil db = new DBUtil();
    
    public String[] getUserandPass() {
        ResultSet rs = db.doSelect(Constantes.STRING_OTASS_COLS, Constantes.DB_OTASS_Table, Constantes.STRING_OTASS_CONDITION);
        String[] userAndPass = new String[] {retrieveUserFromDB(rs), decrypt(retrievePassFromDB(rs))};
        return userAndPass;
    }
    
    private String retrieveUserFromDB(ResultSet rs) {
        String userName = null;
      //Retrieve username from database.
        try {
            if (rs.next()) {
                //With substring, retrieves content from column "user" 
                userName = rs.getString(Constantes.STRING_OTASS_COLS.substring(0, 4));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userName;
    }
    
    private byte[] retrievePassFromDB(ResultSet rs) {
        byte[] encryptedPass = null;
        
        //Retrieve encrypted password from database.
        try {
            //With substring, retrieves content from column "pass" 
            encryptedPass = rs.getBytes(Constantes.STRING_OTASS_COLS.substring(6));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return encryptedPass;
    }
       
    private String decrypt(byte[] text) {
        final String PATH_PRIVATE_KEY = "D:\\IAS_Monitoring\\APP_Dev\\SCHEDScripts\\privateOTASS.key";
        byte[] decryptedPass = null;
        PrivateKey privateKey = null;
        ObjectInputStream inputStream = null;
        
        //Retrieve private key file
        try {
            inputStream = new ObjectInputStream(new FileInputStream(PATH_PRIVATE_KEY));
        } catch (FileNotFoundException e) {
            System.out.println("OTASS private key file not found.");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error while opening OTASS private key file.");
            e.printStackTrace();
        }
        
        
        //Read the private key file
        try {
            privateKey = (PrivateKey) inputStream.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error while reading OTASS private key file.");
            e.printStackTrace();
        } finally {
        	try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        
        //Decrypt the password
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            decryptedPass = cipher.doFinal(text);
        } catch (Exception e) {
            System.out.println("Error while decrypting OTASS password.");
            e.printStackTrace();
        }
        
        return new String(decryptedPass);
    }
}
