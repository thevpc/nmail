
import java.io.File;
import java.io.StringReader;
import net.thevpc.gomail.GoMail;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author vpc
 */
public class Ex06LoadFile {

    public static void main(String[] args) {
        GoMail go = GoMail.load(new File("/data/private/git/work-documents/gomail/aid-idha.gomail"));
        go.send();
        
        go = GoMail.load(new StringReader(
                "#mimetype=application/gomail\n" +
"from : me@nowhere.com\n" +
"to : ${Email}\n" +
"#to : ${from}\n" +
"cc : ${from}\n" +
"user : mylogin\n" +
"password: canard77\n" +
"property: mail.smtp.host=ssl0.ssl2.net\n" +
"property: mail.smtp.port=123\n" +
"property: mail.smtp.auth=true\n" +
"property: mail.smtp.starttls.enable=true\n" +
"\n" +
"datasource : ds ; type=csv ; readlines\n" +
"Sex; Prenom      ; Email                                    ;Ignore\n" +
"M  ; Si Taha     ; taha.bensalah@me.there; x\n" +
"\n" +
"repeat : ds where Email<>null and Ignore<>'x' \n" +
"simulate : false\n" +
"subject : AID Moubarek\n" +
"object : text/html expandable\n" +
"<html> \n" +
"	<body>\n" +
"	<p>${if Sex='F' then 'Chere' else 'Cher' end} ${Prenom},</p>\n" +
"	<p>\n" +
"        Je tiens a vous formuler, a l'occasion de l'Aid, mes voeux sinceres de bonheur, de sante et de prosperite a vous et votre famille.\n" +
"	</p>\n" +
"	<p>Amities</p>\n" +
"	</body>\n" +
"</html>\n" +
"<<end>>\n" +
"footer : text/html expandable\n" +
"<p><strong>Taha Ben Salah</strong></p>\n" +
"<p>Core Techs Solutions</p>\n" +
"\n" +
"<<end>>\n" +
" "));
        go.send();
    }
}
