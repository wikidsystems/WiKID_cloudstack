/**
 * Created by: els
 * Date: 5/21/12
 * Time: 8:31 AM
 */
package com.wikidsystems.cloud;

import com.cloud.server.auth.UserAuthenticator;
import com.wikidsystems.client.wClient;

import javax.ejb.Local;
import javax.naming.ConfigurationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

@Local(value = {com.cloud.server.auth.UserAuthenticator.class})

public class WikidUserAuthenticator implements UserAuthenticator {
    private final Logger log = Logger.getLogger(this.getClass().getName());
    private final Properties p = new Properties();
    public static final String CONFIGURATION_PROPERTIES = "/etc/WiKID/cloudstack.properties";

    private boolean configured = false;
    private String auth_domaincode;
    private wClient wc;

    public boolean authenticate(String username, String password, Long domainId, Map<String, Object[]> requestParameters) {

        if(p.getProperty("debug").equalsIgnoreCase("true")){
            l("WikidUserAuthenticator.authenticate() called with the following parameters:\n" +
                    "\tusername: "+username+"\n"+
                    "\tpassword: "+password+"\n"+
                    "\tdomainId: "+domainId+"\n"+
                    "\trequestParameters: "+"\n"+
                        "\t\t"+"response:"+requestParameters.get("response")[0]+"\n"+
                        "\t\t"+"username:"+requestParameters.get("username")[0]+"\n"+
                        "\t\t"+"domain:"+requestParameters.get("domain")[0]+"\n"+
                        "\t\t"+"command:"+requestParameters.get("command")[0]+"\n"+
                        "\t\t"+"password:"+requestParameters.get("password")[0]+"\n\n");
        }


        // TODO: Validate the user really exists (_userAccountDao)

        boolean result = wc.CheckCredentials(username, password, auth_domaincode);

        if(result){
            l("User "+ username+ " successfully authenticated in the "+ auth_domaincode + " WiKID domain.");
        } else {
            l("User "+ username+ " FAILED authentication in the "+ auth_domaincode + " WiKID domain.");
        }
        return result;
    }

    private boolean configure()  {
        try {
            l("Loading properties from: "+ CONFIGURATION_PROPERTIES);
            p.load(new FileReader(CONFIGURATION_PROPERTIES));
            l("Loaded properties: " + p);
            l("Opening wClient connection...");
//            wc = new wClient(CONFIGURATION_PROPERTIES);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            l(e.getMessage());
            return false;
        }
    }

    private void l(String logMessage){
        if(!configured || p.getProperty("debug").equalsIgnoreCase("true")){
            System.out.println(logMessage);
            log.info(logMessage);
        }
    }

    public boolean configure(String s, Map<String, Object> stringObjectMap) throws ConfigurationException {
        if(!configured) {
            l("Initializing WiKID Authenticator");
            if (!configure()) {
                l("Initialization of WiKID Authenticator failed.");
                return false;
            }
            auth_domaincode = p.getProperty("auth_domaincode");
            l("Initialization of WiKID Authenticator complete.");
        }

        if(p.getProperty("debug").equalsIgnoreCase("true")){
            l("WikidUserAuthenticator.configure() called with the following parameters:\n" +
                    "\ts: "+s+"\n"+
                    "\tstringObjectMap: "+stringObjectMap+"\n\n");
        }

        return true;
    }

    public String getName() {
        return "WiKID User Authenticator";
    }

    public boolean start() {
        l("WikidUserAuthenticator.start() called");
        wc = new wClient(p.getProperty("host"),Integer.parseInt(p.getProperty("port")),
                p.getProperty("keyfile"), p.getProperty("pass"),
                p.getProperty("caStore"), p.getProperty("caStorePass"));
        if(!wc.isConnected()){
            l("The WiKID client failed to connect the the WiKID server.  Verify the configuration parameters in "+CONFIGURATION_PROPERTIES);
            return false;
        }
        return true;
    }

    public boolean stop() {
        l("WikidUserAuthenticator.stop() called");
        wc.close();
        return true;
    }
}
