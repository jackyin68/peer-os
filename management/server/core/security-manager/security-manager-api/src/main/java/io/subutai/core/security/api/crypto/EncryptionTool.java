package io.subutai.core.security.api.crypto;


import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;

import io.subutai.common.security.crypto.pgp.ContentAndSignatures;
import io.subutai.common.security.crypto.pgp.KeyPair;


/**
 * Tool for working with encryption
 */
public interface EncryptionTool
{

    /**
     * *****************************************
     */
    public byte[] encrypt( final byte[] message, final PGPPublicKey publicKey, boolean armored );


    /**
     * Decrypts message with Peer private key
     */
    public byte[] decrypt( final byte[] message ) throws PGPException;


    /**
     * *****************************************
     */
    public boolean verify( byte[] signedMessage, PGPPublicKey publicKey );


    /**
     * Signs message with peer private key and encrypts with the given pub key
     *
     * @param message - message
     * @param publicKey - encryption key
     * @param armored - output in armored format
     */
    public byte[] signAndEncrypt( final byte[] message, final PGPPublicKey publicKey, final boolean armored )
            throws PGPException;


    /**
     * Decrypts message with peer private key
     *
     * @param encryptedMessage - message
     *
     * @return - {@code ContentAndSignatures}
     */
    public ContentAndSignatures decryptAndReturnSignatures( final byte[] encryptedMessage ) throws PGPException;


    /**
     * Verifies the content with its signatures
     *
     * @param contentAndSignatures -  {@code ContentAndSignatures}
     * @param publicKey - public key to verify signatures
     *
     * @return - true if verified successfully, false otherwise
     */

    public boolean verifySignature( ContentAndSignatures contentAndSignatures, PGPPublicKey publicKey )
            throws PGPException;


    /**
     * Generated keypair
     *
     * @param userId
     * @param secretPwd
     * @param armored
     *
     * @return - KeyPair
     */
    public KeyPair generateKeyPair ( String userId,String secretPwd, boolean armored );


    /* **********************************************
     *
     */
    public byte[] decrypt( final byte[] message, PGPSecretKeyRing keyRing , String pwd) throws PGPException;


    /**
     * Signs a public key
     *
     * @param publicKeyRing a public key ring containing the single public key to sign
     * @param id the id we are certifying against the public key
     * @param secretKey the signing key
     * @param secretKeyPassword the signing key password
     *
     * @return a public key ring with the signed public key
     */

    public PGPPublicKeyRing signPublicKey( PGPPublicKeyRing publicKeyRing, String id, PGPSecretKey secretKey,String secretKeyPassword );


    /**
     * Verifies that a public key is signed with another public key
     *
     * @param keyToVerify the public key to verify
     * @param id the id we are verifying against the public key
     * @param keyToVerifyWith the key to verify with
     *
     * @return true if verified, false otherwise
     */
    public boolean verifyPublicKey( PGPPublicKey keyToVerify, String id, PGPPublicKey keyToVerifyWith );

}
