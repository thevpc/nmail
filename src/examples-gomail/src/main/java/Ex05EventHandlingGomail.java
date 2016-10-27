import net.vpc.common.gomail.*;
import net.vpc.common.gomail.modules.GoMailModuleProcessor;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by vpc on 10/3/16.
 */
public class Ex05EventHandlingGomail {
    public static void main(String[] args) {
        GoMail go=new GoMail();
        go.from("me@gmail.com");
        go.to("you@gmail.com");
        go.cc("him@gmail.com");
        go.subject("Salute from the other side");
        go.body().add(
                "<html>" +
                "<body>" +
                "<p>Hi you,</p>" +
                "<p>How Are you</p>"+
                "</body>"+
                "</html>",
                GoMail.HTML_CONTENT_TYPE,
                false
        );
        try {
            go.setCredentials("me","1234");
            go.setSimulate(false);
            GoMailModuleProcessor processor = DefaultGoMailFactory.INSTANCE.createProcessor();
            //define another agent if ou want to consider replacing default java smtp sending agent
            processor.setAgent(new GoMailAgent() {
                public int sendMessage(GoMailMessage mail, Properties properties, GoMailContext expr) throws IOException {
                    System.out.println("Je ne vais pas vraiment envoyer un mail mais je vais faire cet affichage");
                    //return 1 qui est le nombre de messages supposes envoyes
                    return 1;
                }
            });
            processor.sendMessage(
                    go, null,
                    new GoMailListener() {
                        public void onBeforeSend(GoMailMessage mail) {
                            System.out.println("log Before Sending");
                        }

                        public void onAfterSend(GoMailMessage mail) {
                            System.out.println("log after successful Sending");
                        }

                        public void onSendError(GoMailMessage mail, Throwable exc) {
                            System.out.println("log after failing sending");
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
