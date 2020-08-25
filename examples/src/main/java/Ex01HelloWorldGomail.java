
import net.vpc.common.gomail.GoMail;

import java.io.IOException;

/**
 * Created by vpc on 10/3/16.
 */
public class Ex01HelloWorldGomail {

    public static void main(String[] args) {
        GoMail go = new GoMail()
                .setCredentials("me", "1234")
                .from("me@gmail.com")
                .to("you@gmail.com")
                .cc("him@gmail.com")
                .subject("Hello");
        go.body().add(
                "Hi you,\n"
                + "How Are you",
                GoMail.TEXT_CONTENT_TYPE,
                false
        );
        go.send();
    }
}
