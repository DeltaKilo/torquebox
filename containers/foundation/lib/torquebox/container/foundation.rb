# Copyright 2008-2011 Red Hat, Inc, and individual contributors.
# 
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
# 
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
# 
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

require 'ostruct'
require 'torquebox/kernel'
require 'torquebox/container/foundation_enabler'
require 'jruby'

module TorqueBox
  module Container
    class Foundation

      MC_MAIN_DEPLOYER_NAME = "MainDeployer"

      def initialize()
        @logger = org.jboss.logging::Logger.getLogger( 'org.torquebox.containers.Foundation' )
        @classloader = JRuby.runtime.jruby_class_loader
        @server = Java::org.jboss.bootstrap.api.mc.server::MCServerFactory.createServer( @classloader )

        descriptors = @server.configuration.bootstrap_descriptors
        descriptors << Java::org.jboss.reloaded.api::ReloadedDescriptors.class_loading_descriptor
        descriptors << Java::org.jboss.reloaded.api::ReloadedDescriptors.vdf_descriptor
        descriptors << org.jboss.bootstrap.api.descriptor::FileBootstrapDescriptor.new( java.io::File.new( File.join( File.dirname(__FILE__), 'foundation-bootstrap-jboss-beans.xml' ) ) )
        @enablers = []
        enable( FoundationEnabler )
      end

      def enable(enabler_or_class,&block)
        enabler = nil
        if ( enabler_or_class.is_a?( Class ) ) 
          enabler = enabler_or_class.new( &block )
        else
          enabler = enabler_or_class
        end
        
        wrapper = OpenStruct.new( :enabler=>enabler, :deployments=>[] )
        @enablers << wrapper
      end
  
      def start
        @server.start
        TorqueBox::Kernel.kernel = kernel

        beans_xml = File.join( File.dirname(__FILE__), 'foundation-jboss-beans.xml' )

        @enablers.each do |wrapper|
          if ( wrapper.enabler.respond_to?( :before_start ) ) 
            wrapper.enabler.send( :before_start, self )
          end
          wrapper.enabler.fundamental_deployment_paths.each do |path|
            wrapper.deployments << deploy(path)
          end
        end

        process_deployments( true )

        @enablers.each do |wrapper|
          if ( wrapper.enabler.respond_to?( :after_start ) )  
            wrapper.enabler.send( :after_start, self )
          end
        end
      end
  
      def stop
        @enablers.reverse.each do |wrapper|
          wrapper.deployments.each do |deployment|
            name = Java::java.lang::String.new( deployment.name )
            undeploy( name )
          end
        end
     
        process_deployments( true )
        @server.stop
      end
  
      def deploy(path)
        virtual_file = Java::org.jboss.vfs::VFS.getChild( path )
        deployment_factory = Java::org.jboss.deployers.vfs.spi.client::VFSDeploymentFactory.instance
        deployment = deployment_factory.createVFSDeployment(virtual_file)
        main_deployer.addDeployment(deployment)
        deployment
      end

      def deploy_as(path, name)
        virtual_file = Java::org.jboss.vfs::VFS.getChild( path )
        deployment_factory = Java::org.jboss.deployers.vfs.spi.client::VFSDeploymentFactory.instance
        deployment = deployment_factory.createVFSDeployment(name, virtual_file)
        main_deployer.addDeployment(deployment)
        deployment
      end

      def undeploy(deployment_name)
        main_deployer.undeploy( deployment_name )
      end

      def process_deployments(check_complete=false)
        main_deployer.process
        if ( check_complete )
          main_deployer.checkComplete
        end
      end

      def deployment_unit(name) 
        main_deployer.getDeploymentUnit( name )
      end

      def kernel()
        kernel_controller.kernel
      end

      def kernel_controller() 
        @server.kernel.controller
      end

      def main_deployer()
        kernel_controller.getInstalledContext(MC_MAIN_DEPLOYER_NAME).target
      end
     
      def [](bean_name)
        entry = kernel_controller.getInstalledContext( bean_name )
        return nil if entry.nil?
        entry.target
      end
  
    end
  end
end
