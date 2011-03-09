import java.applet.Applet;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

//import netscape.javascript.JSObject;

/**
 * General applet for generic installs.
 */
public class Installer extends Applet {


    private static final long serialVersionUID = -3141611634409357059L;
    
    public void install() {
        System.out.println("Install called...");
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        final String installerName = installerName();
        final File tempFile = new File(tempDir, installerName);
        OutputStream os = null;
        try {
            os = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }        
        HttpURLConnection connection = null;
        URL serverAddress = null;
        InputStream is = null;
        InputStream gis = null;
        try {
            serverAddress = new URL("http://cdn.bravenewsoftware.org/"+installerName);
          
            //Set up the initial connection
            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(30000);
                      
            connection.connect();
            is = connection.getInputStream();
            gis = new GZIPInputStream(is);
            copyLarge(gis, os);
            
            System.out.println("Opening Lantern");
            final String cmd = "open "+tempFile.getAbsolutePath();
            System.out.println("Calling command "+cmd);
            Runtime.getRuntime().exec(cmd);
            //Thread.sleep(10 * 1000);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (gis != null) {
                try {
                    gis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        /*
        try {
            Runtime.getRuntime().exec("open /Applications/Calculator.app");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */
    }
    
    private String installerName() {
        if (getOSMatches("Windows")) {
            return "installer.exe";
        }
        else if (getOSMatches("Mac OS X")) {
            return "lantern.app";
        }
        return "installer.exe";
    }
    
    private static final String OS_NAME = System.getProperty("os.name");
    
    private static boolean getOSMatches(final String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    } 

    private static long copyLarge(final InputStream input, 
        final OutputStream output)
        throws IOException {
        final byte[] buffer = new byte[1024 * 4];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    /*
    private void callJavaScript(final String func, final Object... args) {
        final JSObject window = JSObject.getWindow(Installer.this);
        if (window == null) {
            System.out.println("Could not get window from JSObject!!!");
            return;
        }
        System.out.println("Calling func through window");
        try {
            window.call(func, args);
        } catch (final Exception e) {
            System.out.println("Got error!!" + e.getMessage());
            e.printStackTrace();
            showError(e);
        }
        System.out.println("Finished JavaScript call...");
    }

    private void showError(final Exception e) {
        final String[] args = new String[] { e.getMessage() };
        final JSObject window = JSObject.getWindow(this);
        try {
            window.call("alert", args);
        } catch (final Exception ex) {
            System.out.println("Error showing error! " + ex);
        }
    }
    */

    @Override
    public void init() {
        System.out.println("Initing applet with polling...");
        super.init();
    }

    @Override
    public void start() {
        System.out.println("Starting applet with polling...");
        super.start();
    }

    @Override
    public void stop() {
        System.out.println("Stopping applet...");
        super.stop();
    }

    @Override
    public void destroy() {
        System.out.println("Destroying applet...");
        super.destroy();
    }
}
