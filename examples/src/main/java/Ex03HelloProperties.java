
import net.thevpc.nmail.NMail;


/**
 * Created by vpc on 10/3/16.
 */
public class Ex03HelloProperties {

    public static void main(String[] args) {
        NMail go = new NMail();
        go.from("me@gmail.com");
        go.to("you@gmail.com");
        go.cc("him@gmail.com");
        go.subject("Salute from the other side");
        go.body().add(
                "<html>"
                + "<body>"
                + "<p>Hi you,</p>"
                + "<p>How Are you</p>"
                + "</body>"
                + "</html>",
                NMail.HTML_CONTENT_TYPE,
                false
        );
        //if no properties are defined, nmail will handle automatically
        //smtp properties, you still could define them by yourself
        go.setProperty("mail.smtp.auth", "true");
        go.setProperty("mail.smtp.starttls.enable", "true");
        go.setProperty("mail.smtp.host", "smtp.gmail.com");
        go.setProperty("mail.smtp.port", "587");
        go.setProperty("mail.smtp.socketFactory.class", "SSL_FACTORY");
        go.setProperty("mail.smtp.socketFactory.fallback", "false");
        go.setProperty("mail.smtp.socketFactory.port", "465");

        go.setCredentials("me", "1234");
        //when setSimulate is true, no mail will be sent, but a log is written to stdout
        go.setDry(true);
        go.send();

    }
}
