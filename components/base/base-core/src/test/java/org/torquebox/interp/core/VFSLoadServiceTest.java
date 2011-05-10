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

package org.torquebox.interp.core;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.torquebox.core.runtime.VFSLoadService;
import org.torquebox.test.mc.vfs.AbstractVFSTestCase;

public class VFSLoadServiceTest extends AbstractVFSTestCase {

    private VFSLoadService loadService;

    @Before
    public void setUp() {
        this.loadService = new VFSLoadService( null );
    }

    @Test
    public void testMakeUrlNonVfsBaseWithoutSlash() throws Exception {
        String base = "/Users/bob/myapp";
        String path = "app/controllers/foo_controller.rb";

        String url = this.loadService.makeUrl( base, path ).toString();

        assertEquals( "file:/Users/bob/myapp/app/controllers/foo_controller.rb", url );
    }

    @Test
    public void testMakeUrlNonVfsBaseWithSlash() throws Exception {
        String base = "/Users/bob/myapp/";
        String path = "app/controllers/foo_controller.rb";

        String url = this.loadService.makeUrl( base, path ).toString();

        assertEquals( "file:/Users/bob/myapp/app/controllers/foo_controller.rb", url );
    }

    @Test
    public void testMakeUrlVfsBaseWithoutSlash() throws Exception {
        String base = "vfs:/Users/bob/myapp";
        String path = "app/controllers/foo_controller.rb";

        String url = this.loadService.makeUrl( base, path ).toString();

        assertEquals( "vfs:/Users/bob/myapp/app/controllers/foo_controller.rb", url );
    }

    @Test
    public void testMakeUrlVfsBaseWithSlash() throws Exception {
        String base = "vfs:/Users/bob/myapp/";
        String path = "app/controllers/foo_controller.rb";

        String url = this.loadService.makeUrl( base, path ).toString();

        assertEquals( "vfs:/Users/bob/myapp/app/controllers/foo_controller.rb", url );
    }
}
