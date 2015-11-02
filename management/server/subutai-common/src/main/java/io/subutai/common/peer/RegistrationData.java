package io.subutai.common.peer;


import com.fasterxml.jackson.annotation.JsonIgnore;

import io.subutai.common.serialize.Serializable;


/**
 * Registration DTO
 */
public class RegistrationData
{
    private PeerInfo peerInfo;
    private String keyPhrase;
    private String cert;
    private RegistrationStatus status;
    private Encrypted data;


    public RegistrationData()
    {
    }


    public RegistrationData( final PeerInfo peerInfo, final String keyPhrase, final RegistrationStatus status )
    {
        this.peerInfo = peerInfo;
        this.keyPhrase = keyPhrase;
        this.status = status;
    }


    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    public void setPeerInfo( final PeerInfo peerInfo )
    {
        this.peerInfo = peerInfo;
    }


    public String getKeyPhrase()
    {
        return keyPhrase;
    }


    public void setKeyPhrase( final String keyPhrase )
    {
        this.keyPhrase = keyPhrase;
    }


    public void setCert( final String cert )
    {
        this.cert = cert;
    }


    public String getCert()
    {
        return cert;
    }


    public RegistrationStatus getStatus()
    {
        return status;
    }


    public void setStatus( final RegistrationStatus status )
    {
        this.status = status;
    }


    public Encrypted getData()
    {
        return data;
    }


    public void setData( final Encrypted data )
    {
        this.data = data;
    }
}
