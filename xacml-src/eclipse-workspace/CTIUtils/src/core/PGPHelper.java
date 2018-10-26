package core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPOnePassSignature;
import org.bouncycastle.openpgp.PGPOnePassSignatureList;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureList;
import org.bouncycastle.openpgp.PGPSignatureSubpacketVector;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.bouncycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

/**
 * This class implements PGP encryption and decryption for either a file or a string
 * @author 
 * Justin Fleming - fleminjr@critical.com
 * Jeremy Fields - fieldsjd@critical.com
 *
 */
public class PGPHelper {
	// Here's my new line.
    /**
     * Used for PGP
     */
    private static final int   KEY_FLAGS = 27;
    /**
     * Used for PGP
     */
    private static final int[] MASTER_KEY_CERTIFICATION_TYPES = new int[]{
    	PGPSignature.POSITIVE_CERTIFICATION,
    	PGPSignature.CASUAL_CERTIFICATION,
    	PGPSignature.NO_CERTIFICATION,
    	PGPSignature.DEFAULT_CERTIFICATION
    };

    
    
    /**
    *
    * @param fileToVerify - the signed file to verify
    * @param publicKeyFile - the public key to verify against
    * @throws Exception
    */
   public static final boolean verifyFile(InputStream fileToVerify, InputStream keyIn) throws Exception {
       InputStream in = PGPUtil.getDecoderStream(fileToVerify);
        
       PGPObjectFactory pgpObjFactory = new PGPObjectFactory(in);
       PGPCompressedData compressedData = (PGPCompressedData)pgpObjFactory.nextObject();
       
       //Get the signature from the file
         
       pgpObjFactory = new PGPObjectFactory(compressedData.getDataStream());
       PGPOnePassSignatureList onePassSignatureList = (PGPOnePassSignatureList)pgpObjFactory.nextObject();
       PGPOnePassSignature onePassSignature = onePassSignatureList.get(0);
        
       //Get the literal data from the file

       PGPLiteralData pgpLiteralData = (PGPLiteralData)pgpObjFactory.nextObject();
       InputStream literalDataStream = pgpLiteralData.getInputStream();
        
       PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
       PGPPublicKey key = pgpRing.getPublicKey(onePassSignature.getKeyID());
        
       FileOutputStream literalDataOutputStream = new FileOutputStream(pgpLiteralData.getFileName());
       onePassSignature.init(new JcaPGPContentVerifierBuilderProvider().setProvider("BC"), key);

       int ch;
       while ((ch = literalDataStream.read()) >= 0) {
           onePassSignature.update((byte)ch);
           literalDataOutputStream.write(ch);
       }

       literalDataOutputStream.close();
        
       //Get the signature from the written out file

       PGPSignatureList p3 = (PGPSignatureList)pgpObjFactory.nextObject();
       PGPSignature signature = p3.get(0);
        
       //Verify the two signatures

       if (onePassSignature.verify(signature)) {
           return true;
       }
       else {
           return false;
       }
   }
    
    //http://sloanseaman.com/wordpress/2012/05/13/revisited-pgp-encryptiondecryption-in-java/
    
    /**
     * This method is used for encryption and decryption methods for reading
     * a public key in from an input stream.
     *  
     * @param in - InputStream
     * @return PGPPublicKey
     * @throws IOException
     * @throws PGPException
     */
    @SuppressWarnings("unchecked")
    private static PGPPublicKey readPublicKey(InputStream in)
    	throws IOException, PGPException
    {

        PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(in));

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //
        PGPPublicKey publicKey = null;

        //
        // iterate through the key rings.
        //
        Iterator<PGPPublicKeyRing> rIt = keyRingCollection.getKeyRings();

        while (publicKey == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (publicKey == null && kIt.hasNext()) {
                PGPPublicKey key = kIt.next();
                if (key.isEncryptionKey()) {
                    publicKey = key;
                }
            }
        }

        if (publicKey == null) {
            throw new IllegalArgumentException("Can't find public key in the key ring.");
        }
        if (!isForEncryption(publicKey)) {
            throw new IllegalArgumentException("KeyID " + publicKey.getKeyID() + " not flagged for encryption.");
        }

        return publicKey;
    }

     /**
     * Load a secret key ring collection from keyIn and find the private key corresponding to
     * keyID if it exists.
     *
     * @param keyIn input stream representing a key ring collection.
     * @param keyID keyID we want.
     * @param pass passphrase to decrypt secret key with.
     * @return PGPPrivateKey
     * @throws IOException
     * @throws PGPException
     * @throws NoSuchProviderException
     */
    private  static PGPPrivateKey findPrivateKey(InputStream keyIn, long keyID, char[] pass)
    	throws IOException, PGPException, NoSuchProviderException
    {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
        return findPrivateKey(pgpSec.getSecretKey(keyID), pass);

    }

    /**
     * Load a secret key and find the private key in it
     * @param pgpSecKey The secret key
     * @param pass passphrase to decrypt secret key with
     * @return
     * @throws PGPException
     */
    private static PGPPrivateKey findPrivateKey(PGPSecretKey pgpSecKey, char[] pass)
    	throws PGPException
    {
    	if (pgpSecKey == null) return null;

        PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pass);
        return pgpSecKey.extractPrivateKey(decryptor);
    }


    /**
     * This method decrypts a file, and writes the decrypted contents
     * to a new file.
     * 
     * @param inputFileName
     * @param outputFileName
     * @param secretKeyFileName
     * @param passwd
     * @throws Exception
     */
    public static void DecryptFile(String inputFileName, String outputFileName, String secretKeyFileName, String passwd)
    	throws Exception
    {
    	
        InputStream inStrm = new FileInputStream(inputFileName);
        InputStream keyIn = new FileInputStream(secretKeyFileName);
        OutputStream out = new FileOutputStream(outputFileName);
        

    	Security.addProvider(new BouncyCastleProvider());

        InputStream in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(inStrm);
        
        PGPObjectFactory pgpF = new PGPObjectFactory(in);
        PGPEncryptedDataList enc;

        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof  PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        //
        // find the secret key
        //
        @SuppressWarnings("unchecked")
		Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;

        while (sKey == null && it.hasNext()) {
            pbe = it.next();

            sKey = findPrivateKey(keyIn, pbe.getKeyID(), passwd.toCharArray());
        }

        if (sKey == null) {
        	out.close();
            throw new IllegalArgumentException("Secret key for message not found.");
        }

        InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));

        PGPObjectFactory plainFact = new PGPObjectFactory(clear);

        Object message = plainFact.nextObject();

        if (message instanceof  PGPCompressedData) {
            PGPCompressedData cData = (PGPCompressedData) message;
            PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());

            message = pgpFact.nextObject();
        }

        if (message instanceof  PGPLiteralData) {
            PGPLiteralData ld = (PGPLiteralData) message;

            InputStream unc = ld.getInputStream();
            int ch;

            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
        } else if (message instanceof  PGPOnePassSignatureList) {
        	out.close();
            throw new PGPException("Encrypted message contains a signed message - not literal data.");
        } else {
        	out.close();
            throw new PGPException("Message is not a simple encrypted file - type unknown.");
        }

        if (pbe.isIntegrityProtected()) {
            if (!pbe.verify()) {
            	out.close();
            	throw new PGPException("Message failed integrity check");
            }
        }
        in.close();
        out.close();
        keyIn.close();
    }
    
    /**
     * This method decrypts a string, and returns the decrypted string.
     * 
     * @param encryptedString
     * @param secretKeyFileName
     * @param passwd
     * @return decrypted string
     * @throws Exception
     */
    public static String DecryptString(String encryptedString, String secretKeyFileName, String passwd)
    throws Exception
    {
    	
    	String decString;
    	InputStream in = IOUtils.toInputStream(encryptedString, "UTF-8");
        InputStream keyIn = new FileInputStream(secretKeyFileName);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        

    	Security.addProvider(new BouncyCastleProvider());

        in = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(in);

        PGPObjectFactory pgpF = new PGPObjectFactory(in);
        PGPEncryptedDataList enc;

        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof  PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        //
        // find the secret key
        //
        @SuppressWarnings("unchecked")
		Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;

        while (sKey == null && it.hasNext()) {
            pbe = it.next();

            sKey = findPrivateKey(keyIn, pbe.getKeyID(), passwd.toCharArray());
        }

        if (sKey == null) {
        	out.close();
            throw new IllegalArgumentException("Secret key for message not found.");
        }

        InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));

        PGPObjectFactory plainFact = new PGPObjectFactory(clear);

        Object message = plainFact.nextObject();

        if (message instanceof  PGPCompressedData) {
            PGPCompressedData cData = (PGPCompressedData) message;
            PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());

            message = pgpFact.nextObject();
        }

        if (message instanceof  PGPLiteralData) {
            PGPLiteralData ld = (PGPLiteralData) message;

            InputStream unc = ld.getInputStream();
            int ch;

            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
            decString = out.toString();
        } else if (message instanceof  PGPOnePassSignatureList) {
        	out.close();
            throw new PGPException("Encrypted message contains a signed message - not literal data.");
        } else {
        	out.close();
            throw new PGPException("Message is not a simple encrypted file - type unknown.");
        }

        if (pbe.isIntegrityProtected()) {
            if (!pbe.verify()) {
            	out.close();
            	throw new PGPException("Message failed integrity check");
            }
        }
        
        in.close();
        out.close();
        keyIn.close();
        return decString;
    }

    /**
     * This function reads a file, encrypts it with PGP and writes it to a new file
     * 
     * @param outputFileName
     * @param fileName
     * @param publicKeyFileName
     * @param armor
     * @param withIntegrityCheck
     * @throws IOException
     * @throws NoSuchProviderException
     * @throws PGPException
     */
    public static void EncryptFile(String outputFileName, String fileName, String publicKeyFileName, boolean armor, boolean withIntegrityCheck)
        throws IOException, NoSuchProviderException, PGPException
    {
    	
    	FileInputStream keyIn = new FileInputStream(publicKeyFileName);
    	PGPPublicKey encKey = readPublicKey(keyIn);
    	OutputStream out = new FileOutputStream(outputFileName);
    	
    	Security.addProvider(new BouncyCastleProvider());

        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

        PGPUtil.writeFileToLiteralData(
                comData.open(bOut),
                PGPLiteralData.BINARY,
                new File(fileName) );

        comData.close();

        BcPGPDataEncryptorBuilder dataEncryptor = new BcPGPDataEncryptorBuilder(PGPEncryptedData.TRIPLE_DES);
        dataEncryptor.setWithIntegrityPacket(withIntegrityCheck);
        dataEncryptor.setSecureRandom(new SecureRandom());

        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(dataEncryptor);
        encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(encKey));

        byte[] bytes = bOut.toByteArray();
        OutputStream cOut = encryptedDataGenerator.open(out, bytes.length);
        cOut.write(bytes);
        cOut.close();
        out.close();
        out.close();
        keyIn.close();
    }
    
    /**
     * This method verifies a PGP signature of a file
     * 
     * @param in
     * @param keyIn
     * @param extractContentFile
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unused")
	private static boolean verifyFile(
    	InputStream in,
        InputStream keyIn,
        String extractContentFile)
        throws Exception
    {
        in = PGPUtil.getDecoderStream(in);

        PGPObjectFactory pgpFact = new PGPObjectFactory(in);
        PGPCompressedData c1 = (PGPCompressedData)pgpFact.nextObject();

        pgpFact = new PGPObjectFactory(c1.getDataStream());

        PGPOnePassSignatureList p1 = (PGPOnePassSignatureList)pgpFact.nextObject();

        PGPOnePassSignature ops = p1.get(0);

        PGPLiteralData p2 = (PGPLiteralData)pgpFact.nextObject();

        InputStream dIn = p2.getInputStream();

        IOUtils.copy(dIn, new FileOutputStream(extractContentFile));

        int ch;
        PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn));

        PGPPublicKey key = pgpRing.getPublicKey(ops.getKeyID());

        FileOutputStream out = new FileOutputStream(p2.getFileName());

        ops.init(new BcPGPContentVerifierBuilderProvider(), key);

        while ((ch = dIn.read()) >= 0)
        {
            ops.update((byte)ch);
            out.write(ch);
        }

        out.close();

        PGPSignatureList p3 = (PGPSignatureList)pgpFact.nextObject();
        return ops.verify(p3.get(0));
    }

    /**
     * From LockBox Lobs PGP Encryption tools.
     * http://www.lockboxlabs.org/content/downloads
     *
     * I didn't think it was worth having to import a 4meg lib for three methods
     * @param key
     * @return
     */
    private static boolean isForEncryption(PGPPublicKey key)
    {
        if (key.getAlgorithm() == PublicKeyAlgorithmTags.RSA_SIGN
            || key.getAlgorithm() == PublicKeyAlgorithmTags.DSA
            || key.getAlgorithm() == PublicKeyAlgorithmTags.EC
            || key.getAlgorithm() == PublicKeyAlgorithmTags.ECDSA)
        {
            return false;
        }

        return hasKeyFlags(key, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
    }

    /**
     * From LockBox Lobs PGP Encryption tools.
     * http://www.lockboxlabs.org/content/downloads
     *
     * I didn't think it was worth having to import a 4meg lib for three methods
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
	private static boolean hasKeyFlags(PGPPublicKey encKey, int keyUsage) {
        if (encKey.isMasterKey()) {
            for (int i = 0; i != MASTER_KEY_CERTIFICATION_TYPES.length; i++) {
                for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(MASTER_KEY_CERTIFICATION_TYPES[i]); eIt.hasNext();) {
                    PGPSignature sig = eIt.next();
                    if (!isMatchingUsage(sig, keyUsage)) {
                        return false;
                    }
                }
            }
        }
        else {
            for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(PGPSignature.SUBKEY_BINDING); eIt.hasNext();) {
                PGPSignature sig = eIt.next();
                if (!isMatchingUsage(sig, keyUsage)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * From LockBox Lobs PGP Encryption tools.
     * http://www.lockboxlabs.org/content/downloads
     *
     * I didn't think it was worth having to import a 4meg lib for three methods
     * @param key
     * @return
     */
    private static boolean isMatchingUsage(PGPSignature sig, int keyUsage) {
        if (sig.hasSubpackets()) {
            PGPSignatureSubpacketVector sv = sig.getHashedSubPackets();
            if (sv.hasSubpacket(KEY_FLAGS)) {
                // code fix suggested by kzt (see comments)
            	
                if ((sv.getKeyFlags() == 0 && keyUsage == 0)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // BELOW: http://stackoverflow.com/questions/3939447/how-to-encrypt-a-string-stream-with-bouncycastle-pgp-without-starting-with-a-fil
    
    /**
     * Simple PGP encryptor between byte[].
     * 
     * @param clearData
     *            The text to be encrypted
     * @param pubKeyPath
     * 			  String representing the path to the public key ring
     * 				or public key file.
     *            
     * @param passPhrase
     *            The pass phrase (key). This method assumes that the key is a
     *            simple pass phrase, and does not yet support RSA or more
     *            sophisiticated keying.
     * @param fileName
     *            File name. This is used in the Literal Data Packet (tag 11)
     *            which is really inly important if the data is to be related to
     *            a file to be recovered later. Because this routine does not
     *            know the source of the information, the caller can set
     *            something here for file name use that will be carried. If this
     *            routine is being used to encrypt SOAP MIME bodies, for
     *            example, use the file name from the MIME type, if applicable.
     *            Or anything else appropriate.
     * 
     * @param armor
     * 			Boolean switch for ASCII armor or not
     * 
     * @return encrypted data as a String.
     * @exception IOException
     * @exception PGPException
     * @exception NoSuchProviderException
     */
    public static String encryptString(byte[] clearData, String pubKeyPath,
            String fileName, boolean armor)
            throws IOException, PGPException, NoSuchProviderException {
    	
    	FileInputStream pubKey = new FileInputStream(pubKeyPath);
    	
    	PGPPublicKey encKey = readPublicKey(pubKey);
    	
        if (fileName == null) {
            fileName = PGPLiteralData.CONSOLE;
        }

        ByteArrayOutputStream encOut = new ByteArrayOutputStream();

        OutputStream out = encOut;
        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();

        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(
                PGPCompressedDataGenerator.ZIP);
        OutputStream cos = comData.open(bOut); // open it with the final
        // destination
        PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();

        // we want to generate compressed data. This might be a user option
        // later,
        // in which case we would pass in bOut.
        OutputStream pOut = lData.open(cos, // the compressed output stream
                PGPLiteralData.BINARY, fileName, // "filename" to store
                clearData.length, // length of clear data
                new Date() // current time
                );
        pOut.write(clearData);

        lData.close();
        comData.close();
        
        /* For below:
         * 
         * 	AES_128         7
		 *	AES_192         8
		 *	AES_256         9
		 *	BLOWFISH        4
		 *	CAMELLIA_128	11
		 *	CAMELLIA_192	12
		 *	CAMELLIA_256	13
		 *	CAST5           3
		 *	DES             6
		 *	IDEA            1
		 *	NULL            0
		 *	SAFER           5
		 *	TRIPLE_DES      2
		 *	TWOFISH         10
         * 
         */
        
        
        BcPGPDataEncryptorBuilder bdeb = new  BcPGPDataEncryptorBuilder(9); // not sure if 9 is the best, but it works.
        PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(bdeb);
        
 
        BcPublicKeyKeyEncryptionMethodGenerator encKeyGen = new BcPublicKeyKeyEncryptionMethodGenerator(encKey);
        cPk.addMethod(encKeyGen);

        byte[] bytes = bOut.toByteArray();

        OutputStream cOut = cPk.open(out, bytes.length);

        cOut.write(bytes); // obtain the actual bytes from the compressed stream

        cOut.close();

        out.close();
        
        byte[] temp = encOut.toByteArray();
        
        String encryptedString = new String(temp,"UTF-8");

        return encryptedString;
    }
   
}