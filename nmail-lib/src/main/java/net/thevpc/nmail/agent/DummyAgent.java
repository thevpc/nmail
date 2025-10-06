package net.thevpc.nmail.agent;

import net.thevpc.nmail.NMailAgent;
import net.thevpc.nmail.NMailContext;
import net.thevpc.nmail.NMailMessage;
import net.thevpc.nmail.expr.ExprVars;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.core.NWorkspace;

import java.util.Properties;

public class DummyAgent implements NMailAgent {
    @Override
    public int sendMessage(NMailMessage mail, Properties properties, NMailContext mailContext, ExprVars vars)  {
        if (NWorkspace.get().isPresent()) {
            NOut.println("Dummy agent will not send message " + mail.sourceId() + " ... ");
        } else {
            System.out.println("Dummy agent will not send message " + mail.sourceId() + " ... ");
        }
        return 1;
    }
}
