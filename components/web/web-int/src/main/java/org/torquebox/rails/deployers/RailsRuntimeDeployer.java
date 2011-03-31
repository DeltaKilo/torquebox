/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.torquebox.rails.deployers;

import java.net.MalformedURLException;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.spi.deployer.DeploymentStages;
import org.jboss.deployers.spi.deployer.helpers.AbstractDeployer;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;

import org.torquebox.base.metadata.RubyApplicationMetaData;
import org.torquebox.interp.metadata.RubyLoadPathMetaData;
import org.torquebox.interp.metadata.RubyRuntimeMetaData;
import org.torquebox.interp.spi.RuntimeInitializer;
import org.torquebox.rack.metadata.RackApplicationMetaData;
import org.torquebox.rails.core.RailsRuntimeInitializer;
import org.torquebox.rails.metadata.RailsApplicationMetaData;

/**
 * <pre>
 * Stage: PRE_DESCRIBE
 *    In: RackApplicationMetaData
 *   Out: RubyRuntimeMetaData
 * </pre>
 * 
 * Create the ruby runtime metadata from the rack metadata
 */
public class RailsRuntimeDeployer extends AbstractDeployer {

    public RailsRuntimeDeployer() {
        setStage( DeploymentStages.PRE_DESCRIBE );
        setInput( RailsApplicationMetaData.class );
        addRequiredInput( RackApplicationMetaData.class );
        addRequiredInput( RubyApplicationMetaData.class );
        addOutput( RubyRuntimeMetaData.class );

        setRelativeOrder( 500 );
    }

    public void deploy(DeploymentUnit unit) throws DeploymentException {
        if (unit instanceof VFSDeploymentUnit) {
            deploy( (VFSDeploymentUnit) unit );
        }
    }

    public void deploy(VFSDeploymentUnit unit) throws DeploymentException {
        RubyRuntimeMetaData runtimeMetaData = unit.getAttachment(  RubyRuntimeMetaData.class );
        
        if ( runtimeMetaData != null && runtimeMetaData.getRuntimeType() != null ) {
            log.warn( "Ruby runtime already configured as " + runtimeMetaData.getRuntimeType() + ": " + unit );
            return;
        }

        log.debug( "Deploying rails ruby runtime: " + unit );

        if (runtimeMetaData == null) {
            runtimeMetaData = new RubyRuntimeMetaData();
            unit.addAttachment(  RubyRuntimeMetaData.class, runtimeMetaData );
        }

        RubyApplicationMetaData rubyAppMetaData = unit.getAttachment( RubyApplicationMetaData.class );
        RackApplicationMetaData rackAppMetaData = unit.getAttachment( RackApplicationMetaData.class );
        RailsApplicationMetaData railsAppMetaData = unit.getAttachment( RailsApplicationMetaData.class );

        runtimeMetaData.setBaseDir( rubyAppMetaData.getRoot() );
        runtimeMetaData.setEnvironment( rubyAppMetaData.getEnvironmentVariables() );
        runtimeMetaData.setRuntimeType( RubyRuntimeMetaData.RuntimeType.RAILS );

        try {
            runtimeMetaData.appendLoadPath( new RubyLoadPathMetaData( rubyAppMetaData.getRoot().toURL() ) );
        } catch (MalformedURLException e) {
            throw new DeploymentException( e );
        }

        RuntimeInitializer initializer = new RailsRuntimeInitializer( rubyAppMetaData, rackAppMetaData, railsAppMetaData );
        runtimeMetaData.setRuntimeInitializer( initializer );
    }

}
