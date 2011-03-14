import java.applet.Applet;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//import netscape.javascript.JSObject;

/**
 * General applet for generic installs.
 */
public class Installer extends Applet {


    private static final long serialVersionUID = -3141611634409357059L;
    
    private static final String OS_NAME = System.getProperty("os.name");
    private static final boolean IS_OS_MAC_OSX = getOSMatches("Mac OS X");
    private static final boolean IS_OS_WINDOWS = getOSMatches("Windows");

    private volatile static String status = "Installer Loaded...";

    private volatile static long downloaded = 0L;

    private volatile static long contentLength = 0L;
    
    //private final AtomicReference<String> status = 
    //    new AtomicReference<String>("Installer loaded...");
    
    public void install(final String url, final String installerName) {
        System.out.println("Install called...");
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            public Void run() {
                System.out.println("Inside privileged call...");
                //callInstaller();
                //return null;
                threadedInstall(url, installerName);
                System.out.println("Returning from threaded install call");
                return null;
            }
        });
    }
    
    protected void threadedInstall(final String url, 
        final String installerName) {
        final Runnable runner = new Runnable() {
            public void run() {
                try {
                    final File installerFile = installPrivileged(url, installerName);
                    System.out.println("Installer file at: "+installerFile.getAbsolutePath());
                    
                    final String xattr =
                        "xattr -d com.apple.quarantine "+installerFile.getAbsolutePath();

                    System.out.println("Tweaking attributes with call: "+xattr);
                    Runtime.getRuntime().exec(xattr);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    
                    System.out.println("Opening Lantern");
                    final String cmd = cmd(installerFile);
                    
                    System.out.println("Cur dir: "+new File(".").getAbsolutePath());
                    //final String cmd = "open lantern-osx-installer.app";
                    System.out.println("Calling command "+cmd);
                    
                    status = "Installing Lantern...";
                    Runtime.getRuntime().exec(cmd);
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    status = "Install complete!";
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        };

        final Thread t = new Thread(runner, "Threaded-Install-Thread");
        t.setDaemon(true);
        t.start();
    }
    
    public File unzip(final File zipped, final String installerName) 
        throws IOException {
        
        status = "Extracting installer contents...";
        
        if (IS_OS_MAC_OSX) {
            // Java unzipping for whatever reason creates app bundles OSX
            // doesn't like. Use native unzip instead.
            final File file = 
                new File(zipped.getParentFile(), installerName);
            if (file.exists()) {
                if (!deleteDirectory(file)) {
                    System.err.println("COULD NOT DELETE DIR: "+file);
                }
                else {
                    System.out.println("Successfully deleted directory!");
                }
            } else {
                System.out.println("DIRECTORY DOES NOT EXIST: "+file);
            }
            
            final String unzip = 
                "unzip "+zipped.getAbsolutePath() +" -d "+zipped.getParent();
            System.out.println("Unzipping with call: "+unzip);
            Runtime.getRuntime().exec(unzip);
            return file;
        } else {
            // Try using jar itself on other OSes. 
            Runtime.getRuntime().exec("jar xf "+zipped.getAbsolutePath());
            return new File(installerName);
        }
        /*
        final ZipFile zip = new ZipFile(zipped, ZipFile.OPEN_READ);
        final Enumeration<? extends ZipEntry> entries = zip.entries();
        File firstFile = null;
        while (entries.hasMoreElements()) {
            final ZipEntry entry = entries.nextElement();
            final String name = entry.getName();
            final File file = new File(name);
            if (firstFile == null) {
                firstFile = file;
            }
            if (entry.isDirectory()) {
                if (!file.isDirectory() && !file.mkdirs()) {
                    System.out.println("ERROR making directory: "+name);
                }
            }
            else {
                BufferedOutputStream dest = null;
                BufferedInputStream bis = null;
                try {
                    dest = new BufferedOutputStream(
                        new FileOutputStream(name), 2048);
                    bis = new BufferedInputStream(zip.getInputStream(entry));
                    status = "Extracting "+name;
                    copyLarge(bis, dest);
                    bis.close();
                    dest.flush();
                    dest.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                } finally {
                    closeQuietly(dest, bis);
                }
            }
        }
        return firstFile;
        */
    }
    
    private boolean deleteDirectory(final File path) {
        if (path.exists()) {
            System.out.println("DELETING DIR AT: "+path);
            final File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    if (!deleteDirectory(files[i])) {
                        System.out.println("ERROR DELETING "+files[i]);
                    }
                } else {
                    files[i].delete();
                }
            }
        } 
        
        return (path.delete());
    }

    public File installPrivileged(final String url, final String installerName) 
        throws IOException {
        final File installer = downloadInstaller(url);
        return unzip(installer, installerName);
    }
    

    public File downloadInstaller(final String url) throws IOException {
        final String installerName = installerName();
        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        final File tempFile = new File(tempDir, installerName);
        //final File tempFile = new File("/Users/afisk/lantern/lantern-osx-installer.zip");
        //final String fullUrl = "http://cdn.bravenewsoftware.org/"+installerName;
        
        
        HttpURLConnection connection = null;
        URL serverAddress = null;
        InputStream is = null;
        OutputStream os = null;
        try {
            serverAddress = new URL(url);
            status = "Opening connection to "+url;
            //Set up the initial connection
            connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.setReadTimeout(40000);
            connection.setInstanceFollowRedirects(true);
            connection.setConnectTimeout(40 * 1000);
            connection.setUseCaches(false);
            connection.connect();
            status = "Connected to "+url;
            System.out.println("Connected...");
            System.out.println("HEADERS: "+connection.getHeaderFields());
            final Map<String, List<String>> headers = 
                connection.getHeaderFields();
            for (final Entry<String, List<String>> entry : headers.entrySet()) {
                final String key = entry.getKey();
                if ("content-length".equalsIgnoreCase(key)) {
                    //System.out.println("Setting CONTENT LENGTH");
                    final String val = entry.getValue().get(0);
                    contentLength = Long.parseLong(val);
                    //System.out.println("CONTENT LENGTH: "+contentLength);
                }
            }
            if (contentLength == 0L) {
                System.out.println("NO CONTENT LENGTH!!!");
            }
            //connection.getHeaderField("");
            is = connection.getInputStream();
            os = new FileOutputStream(tempFile);
            //System.out.println("Copying from url: "+url);
            status = "Downloading installer of size "+contentLength;
            copyLarge(is, os);
            os.close();
            is.close();
            status = "Finished downloading installer...";
            System.out.println("Wrote file to: "+tempFile);
            return tempFile;
        } catch (final IOException e) {
            status = 
                "Error downloading installer, reported as: "+e.getMessage();
            //e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            closeQuietly(is, os);
        }
        
        throw new IOException("Did not successfully unzip the file.");
    }
    
    private void closeQuietly(final Closeable... closeables) {
        for (final Closeable c : closeables) {
            if (c == null) continue;
            try {
                c.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected String cmd(final File installerFile) throws IOException {
        if (IS_OS_MAC_OSX) {
            return "open "+installerFile.getAbsolutePath();
        } else if (IS_OS_WINDOWS) {
            return installerFile.getAbsolutePath();
        } else {
            throw new IOException("Unsupported OS");
        }
        
    }
    
    protected String installerName() {
        if (IS_OS_MAC_OSX) {
            return "lantern-osx-installer-0.3.zip";
        } else if (IS_OS_WINDOWS) {
            //return "installer.exe";
            return "lantern-win-installer.zip";
        }
        return "installer.exe";
    }
    
    private static boolean getOSMatches(final String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    } 

    private long copyLarge(final InputStream input, 
        final OutputStream output)
        throws IOException {
        final byte[] buffer = new byte[1024 * 4];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
            downloaded = count;
            System.out.println("Downloaded: "+downloaded);
        }
        return count;
    }
    
    public long getContentLength() {
        return contentLength;
    }
    
    public long getDownloaded() {
        return downloaded;
    }
    
    public int getPercentComplete() {
        if (contentLength == 0L) {
            System.out.println("Content length is zero!!");
            return 0;
        }
        
        return (int) (100 * (double)(downloaded/contentLength));
    }
    
    public String getStatus() {
        return status;
    }

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
    

    /*
    private void callInstaller() {
        final URL sourceUrl = 
            getClass().getClassLoader().getResource("lantern-osx-installer.zip");
        InputStream sourceStream = null;
        OutputStream targetStream = null;
        try {
            sourceStream = new BufferedInputStream(sourceUrl.openStream());
            final File extracted = new File("lantern-osx-installer.zip");
            targetStream = new BufferedOutputStream(
                new FileOutputStream(extracted));
            copyLarge(sourceStream, targetStream);
            closeQuietly(sourceStream, targetStream);
            final File unzipped = unzip(extracted);
            final String cmd = cmd(unzipped);
            System.out.println("Calling command "+cmd);
            Runtime.getRuntime().exec(cmd);
        } catch (final IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(sourceStream, targetStream);
        }
    }
    */
}
