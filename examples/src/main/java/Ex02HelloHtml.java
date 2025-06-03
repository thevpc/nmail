
import net.thevpc.nmail.NMail;


/**
 * Created by vpc on 10/3/16.
 */
public class Ex02HelloHtml {

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
        go.footer(
                "<b>Me</b>, Your sender",
                NMail.HTML_CONTENT_TYPE
        );
        go.setCredentials("me", "1234");
        go.setDry(true);
        go.send();
    }
}
