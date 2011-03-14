CommonUtils = {

    fileDialogApplet : null,
        
	APPLET_NAME : "Installer",
	    
	appletLoadCalled : false,
	    
    showFileDialog : function () {
	    if (CommonUtils.fileDialogApplet === null) {
	        //CommonUtils.showSpinner();
	        CommonUtils.loadLittleShootApplet();
	        
	        // Give it a second to load.
	        setTimeout(function () {
	            try {
	                CommonUtils.fileDialogApplet = $("#InstallerId")[0];
	                CommonUtils.fileDialogApplet.install(
                        "http://cdn.bravenewsoftware.org/lantern-osx-installer-0.31.zip", 
                        "lantern-osx-installer.app");
	            } catch (error) {
	                // This will make it load again next time.
	                CommonUtils.fileDialogApplet = null;
	                CommonUtils.showMessage("File Dialog Error", 
	                  "We're sorry, but there was an error loading the LittleShoot publishing dialog. "+
	                  "It should be work in a second if you try again. If you keep getting errors, "+
	                  "please e-mail us at bugs@littleshoot.org with your browser details. The reported error "+
	                  "is "+error+". Thanks!");
	            }
	            //CommonUtils.hideSpinner();
	        }, 2000);
	    }
	    else {
	        CommonUtils.fileDialogApplet.install(
                "http://cdn.bravenewsoftware.org/lantern-osx-installer-0.31.zip", 
                "lantern-osx-installer.app");
	    }
    },
    
    loadLittleShootApplet : function () {
        if (!CommonUtils.appletLoadCalled) {
            console.info("Loading applet: "+CommonUtils.APPLET_NAME);
            CommonUtils.appletLoadCalled = true;
            CommonUtils.loadApplet(CommonUtils.APPLET_NAME);
        }
        else {
            console.info("Not loading applet");
        }
    },

    loadApplet : function (appletName) {
        // console.info("Creating applet in JavaScript!!!");
        var applet = CommonUtils.newAppletElement(appletName);
        console.info("Appending applet!!");
        document.getElementsByTagName("body")[0].appendChild(applet);
        console.info("Appended applet to body");
    },

    newAppletElement : function (appletName) {
        var applet = document.createElement("applet");
        applet.setAttribute("name", appletName);
        applet.id = appletName + "Id";
        applet.setAttribute("jnlp_href", "littleShootInstaller.jnlp");
        //applet.setAttribute("code", appletName + ".class");
        applet.setAttribute("code", appletName);
        applet.setAttribute("mayscript", true);
        applet.setAttribute("width", 1);
        applet.setAttribute("height", 1);
        applet.setAttribute("archive", "littleShootInstaller.jar")

        // Offscreen.
        /*
        var style = {
            position : "absolute",
            top: "-300px"
        };
        dojo.style(applet, style);
        */

        var appletDiv = document.createElement("div");
        appletDiv.appendChild(applet);
        return appletDiv;
    },
}
