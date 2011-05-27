package org.torquebox.core.runtime;

import static org.junit.Assert.*;

import org.jboss.vfs.VFS;
import org.junit.Before;
import org.junit.Test;

public class VFSLoadServiceTest {
    
    private VFSLoadService loadService;

    @Before
    public void setUp() {
        // Force VFS url-handler init.
        VFS.getRootVirtualFile();
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
