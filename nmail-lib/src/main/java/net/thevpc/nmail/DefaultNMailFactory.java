/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nmail.agent.DefaultNMailAgent;
import net.thevpc.nmail.modules.NMailModuleProcessor;

/**
 * @author taha.bensalah@gmail.com
 */
public class DefaultNMailFactory implements NMailFactory {
    public static final NMailFactory INSTANCE = new DefaultNMailFactory();

    public NMailModuleProcessor createProcessor() {
        return createProcessor(null, null);
    }

    public NMailModuleProcessor createProcessor(NMailAgent agent, NMailConfig config) {
        if (config == null) {
            config = NMailConfig.getDefaultInstance(); //configure with context class loader
        }
        if (agent == null) {
            agent = createAgent();
        }
        return new NMailModuleProcessor(agent, config);
    }

    @Override
    public NMailAgent createAgent() {
        return DefaultNMailAgent.INSTANCE;
    }
}
