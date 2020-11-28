/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import net.thevpc.gomail.modules.GoMailModuleProcessor;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailFactory {
    /**
     * create mail processor with default agent and config.
     * equivalent to <pre>createProcessor(null,null)</pre>
     * @return
     */
    public GoMailModuleProcessor createProcessor() ;

    /**
     * create mail processor.
     * @param agent mail agent, if null createAgent() is called
     * @param config mail config, if null GoMailConfig.getDefaultInstance() is called
     * @return
     */
    public GoMailModuleProcessor createProcessor(GoMailAgent agent,GoMailConfig config) ;

    /**
     * create mail config
     * @return mail agent instance, default is SplitRecipientsGoMailAgent.INSTANCE
     */
    public GoMailAgent createAgent();
}
