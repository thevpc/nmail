import net.vpc.common.gomail.GoMail;

import java.io.IOException;

/**
 * Created by vpc on 10/3/16.
 */
public class Ex02HelloHtmlGomail {
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
        go.footer(
                "<b>Me</b>, Your sender",
                GoMail.HTML_CONTENT_TYPE
        );
        try {
            go.setCredentials("me","1234");
            go.setSimulate(true);
            go.send();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
