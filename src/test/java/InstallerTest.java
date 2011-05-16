import java.io.File;

import org.junit.Test;



public class InstallerTest {

    public void testLocal() throws Exception {
        final Installer i = new Installer();
        //i.install();
        //new File(System.getProperty("java.io.tmpdir");
    }
    
    public void testUnzip() throws Exception {
        final Installer i = new Installer();
        final File zipped = new File("lantern-osx-installer.zip");
        //i.unzip(zipped);
        
        
        Runtime.getRuntime().exec("unzip /var/folders/fV/fVQPjV6KHfO4PrfbviouKU+++TI/-Tmp-/lantern-osx-installer.zip");
        Runtime.getRuntime().exec("open lantern-osx-installer.app");
        //Runtime.getRuntime().exec("jar xvf "+zipped.getName());
    }
}
