
import net.thevpc.nmail.*;
import net.thevpc.nmail.agent.DummyAgent;
import net.thevpc.nmail.modules.NMailModuleProcessor;

/**
 * Created by vpc on 10/3/16.
 */
public class Ex05EventHandling {

    public static void main(String[] args) {
        NMail go = new NMail()
                .from("me@gmail.com")
                .to("you@gmail.com")
                .cc("him@gmail.com")
                .setCredentials("me", "1234")
                .setDry(false)
                .subject("Salute from the other side");
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
        NMailModuleProcessor processor = DefaultNMailFactory.INSTANCE.createProcessor();
        //define another agent if ou want to consider replacing default java smtp sending agent
        processor.setAgent(new DummyAgent());
        processor.sendMessage(
                go, null,
                new NMailListener() {
                    public void onBeforeSend(NMailMessage mail) {
                        System.out.println("log Before Sending");
                    }

                    public void onAfterSend(NMailMessage mail) {
                        System.out.println("log after successful Sending");
                    }

                    public void onSendError(NMailMessage mail, Throwable exc) {
                        System.out.println("log after failing sending");
                    }
                }
        );
    }
}
