/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.nmail;

import net.thevpc.nmail.modules.NMailModuleProcessor;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface NMailFactory {
    /**
     * create mail processor with default agent and config.
     * equivalent to <pre>createProcessor(null,null)</pre>
     * @return
     */
    public NMailModuleProcessor createProcessor() ;

    /**
     * create mail processor.
     * @param agent mail agent, if null createAgent() is called
     * @param config mail config, if null NMailConfig.getDefaultInstance() is called
     * @return
     */
    public NMailModuleProcessor createProcessor(NMailAgent agent, NMailConfig config) ;

    /**
     * create mail config
     * @return mail agent instance, default is NMailDefaultAgent.INSTANCE
     */
    public NMailAgent createAgent();
}
