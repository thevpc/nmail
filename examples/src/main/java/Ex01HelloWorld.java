
import net.thevpc.nmail.NMail;


/**
 * Created by vpc on 10/3/16.
 */
public class Ex01HelloWorld {

    public static void main(String[] args) {
        NMail go = new NMail()
                .setCredentials("me", "1234")
                .from("me@gmail.com")
                .to("you@gmail.com")
                .cc("him@gmail.com")
                .subject("Hello");
        go.body().add(
                "Hi you,\n"
                + "How Are you",
                NMail.TEXT_CONTENT_TYPE,
                false
        );
        go.send();
    }
}
