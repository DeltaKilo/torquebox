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

package org.torquebox.web.rack;

import org.jboss.logging.Logger;
import org.jboss.vfs.VirtualFile;
import org.jruby.Ruby;
import org.torquebox.core.app.RubyApplicationMetaData;
import org.torquebox.core.runtime.RuntimeInitializer;

/**
 * {@link RuntimeInitializer} for Ruby Rack applications.
 * 
 * @author Bob McWhirter <bmcwhirt@redhat.com>
 */
public class RackRuntimeInitializer implements RuntimeInitializer {


    public RackRuntimeInitializer(RubyApplicationMetaData rubyAppMetaData, RackApplicationMetaData rackMetaData) {
        this.rubyAppMetaData = rubyAppMetaData;
        this.rackAppMetaData = rackMetaData;
    }

    public VirtualFile getRackRoot() {
        return this.rubyAppMetaData.getRoot();
    }

    public String getRackEnv() {
        return this.rubyAppMetaData.getEnvironmentName();
    }

    @Override
    public void initialize(Ruby ruby) throws Exception {
        ruby.evalScriptlet( "require %(torquebox-web)" );
        ruby.evalScriptlet( getInitializerScript() );
        ruby.setCurrentDirectory( this.rubyAppMetaData.getRoot().getPhysicalFile().getCanonicalPath() );
    }

    /**
     * Create the initializer script.
     * 
     * @return The initializer script.
     */
    protected String getInitializerScript() {
        StringBuilder script = new StringBuilder();
        String appName = this.rubyAppMetaData.getApplicationName();
        String rackRootPath = this.rubyAppMetaData.getRootPath();
        String rackEnv = this.rubyAppMetaData.getEnvironmentName();
        String contextPath = this.rackAppMetaData.getContextPath();

        if (rackRootPath.endsWith( "/" )) {
            rackRootPath = rackRootPath.substring( 0, rackRootPath.length() - 1 );
        }

        if (!rackRootPath.startsWith( "vfs:/" )) {
            if (!rackRootPath.startsWith( "/" )) {
                rackRootPath = "/" + rackRootPath;
            }
        }

        script.append( "RACK_ROOT=%q(" + rackRootPath + ")\n" );
        script.append( "RACK_ENV=%q(" + rackEnv + ")\n" );
        script.append( "TORQUEBOX_APP_NAME=%q(" + appName + ")\n" );
        script.append( "TORQUEBOX_RACKUP_CONTEXT=%q(" + contextPath + ")\n" );
        script.append( "ENV['RACK_ROOT']=%q(" + rackRootPath + ")\n" );
        script.append( "ENV['RACK_ENV']=%q(" + rackEnv + ")\n" );
        script.append( "ENV['TORQUEBOX_APP_NAME']=%q(" + appName + ")\n" );

        // only set if not root context
        if (contextPath != null && contextPath.length() > 1) { 
            // context path should always start with a "/"
            if (!contextPath.startsWith( "/" )) {
                contextPath = "/" + contextPath;
            }
            script.append( "ENV['RAILS_RELATIVE_URL_ROOT']=%q(" + contextPath + ")\n" );
            script.append( "ENV['RACK_BASE_URI']=%q(" + contextPath + ")\n" );
        }

        return script.toString();
    }

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger( RackRuntimeInitializer.class );
    
    protected RubyApplicationMetaData rubyAppMetaData;
    protected RackApplicationMetaData rackAppMetaData;

}
