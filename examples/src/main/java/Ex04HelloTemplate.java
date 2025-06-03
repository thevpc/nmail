
import net.thevpc.nmail.NMail;
import net.thevpc.nmail.datasource.StringsNMailDataSource;

import java.io.File;

/**
 * Created by vpc on 10/3/16.
 */
public class Ex04HelloTemplate {

    public static void main(String[] args) {
        NMail go = new NMail();
        go.from("me@gmail.com");
        go.to("${adresse}");
        go.cc("${autreAdresse}");
        go.subject("Salute from the other side");
        go.body().add(
                "<html>"
                + "<body>"
                + "<p>${if genre='male' then 'Cher' else 'Chere' end} ${prenom},</p>"
                + "<p>Comment ca va ${prenom} ${nom}?</p>"
                + "</body>"
                + "</html>",
                NMail.HTML_CONTENT_TYPE,
                true
        );
        go.footer(
                "<b>Me</b>, Your sender",
                NMail.HTML_CONTENT_TYPE
        );
        go.attachment(new File("my-attachment.xls"), null);
        go.attachment(new byte[]{1, 2, 3}, null);
        go.repeatDatasource(
                new StringsNMailDataSource(
                new String[][]{
                    {"ali", "ben mahmoud", "ali@gmail.com", "ali@yahoo.com", "male"},
                    {"alia", "bel aid", "alia@gmail.com", "alia@yahoo.com", "female"}
                },
                new String[]{"prenom", "nom", "adresse", "autreAdresse", "genre"}
        ));

        go.setCredentials("me", "1234");
        go.setDry(true);
        go.send();

    }
}
