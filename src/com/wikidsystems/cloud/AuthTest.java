package com.wikidsystems.cloud;

import java.io.File;

/**
 * Created by
 * User: els
 * Date: 5/29/12
 * Time: 9:04 AM
 */
public class AuthTest {

    public static void main(String[] args){
        try{
            if(args.length!=2){
                System.err.println("Usage: java -jar WikidUserAthenticator.jar [username] [passcode]");
                System.exit(0);
            }

            if(!((new File(WikidUserAuthenticator.CONFIGURATION_PROPERTIES)).canRead())){
                System.err.println("Can't read from "+ WikidUserAuthenticator.CONFIGURATION_PROPERTIES);
                System.exit(0);
            }

            WikidUserAuthenticator wua = new WikidUserAuthenticator();
            wua.authenticate(args[0],args[1],0l,null);

        } catch(Throwable t){
            t.printStackTrace();
        }
    }
}
