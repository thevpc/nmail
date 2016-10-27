import net.vpc.common.gomail.GoMail;

import java.io.IOException;

/**
 * Created by vpc on 10/3/16.
 */
public class Ex01HelloWorldGomail {
    public static void main(String[] args) {
        GoMail go=new GoMail();
        go.from("me@gmail.com");
        go.to("you@gmail.com");
        go.cc("him@gmail.com");
        go.subject("Hello");
        go.body().add(
                "Hi you,\n" +
                        "How Areyou",
                GoMail.TEXT_CONTENT_TYPE,
                false
        );
        go.setCredentials("me","1234");
        try {
            go.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
