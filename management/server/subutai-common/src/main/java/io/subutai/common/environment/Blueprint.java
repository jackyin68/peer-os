package io.subutai.common.environment;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import io.subutai.common.util.CollectionUtil;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;


/**
 * Blueprint for environment creation stores nodeGroups.
 *
 * @see NodeGroup
 */
public class Blueprint
{
    private UUID id;
    private ContainerDistributionType type = ContainerDistributionType.AUTO;
    private String name;
    private String cidr;
    private Set<NodeGroup> nodeGroups;


    public Blueprint( final String name, final String cidr, final Set<NodeGroup> nodeGroups )
    {
        this.name = name;
        this.cidr = cidr;
        this.nodeGroups = nodeGroups;
    }


    public Blueprint( final String name, final Set<NodeGroup> nodeGroups )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ), "Invalid name" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( nodeGroups ), "Invalid node group set" );

        this.id = UUID.randomUUID();
        this.name = name;
        this.nodeGroups = nodeGroups;
    }


    public UUID getId()
    {
        return id;
    }


    public void setId( final UUID id )
    {
        this.id = id;
    }


    public ContainerDistributionType getType()
    {
        return type;
    }


    public String getName()
    {
        return name;
    }


    public Set<NodeGroup> getNodeGroups()
    {
        return nodeGroups == null ? Sets.<NodeGroup>newHashSet() : Collections.unmodifiableSet( nodeGroups );
    }


    public String getCidr()
    {
        return cidr;
    }


    public void setType( final ContainerDistributionType type )
    {
        this.type = type;
    }
}
