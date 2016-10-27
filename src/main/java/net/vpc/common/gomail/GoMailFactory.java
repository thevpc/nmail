/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import java.io.IOException;

import net.vpc.common.gomail.modules.GoMailModuleProcessor;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public interface GoMailFactory {
    /**
     * create mail processor with default agent and config.
     * equivalent to <pre>createProcessor(null,null)</pre>
     * @return
     * @throws IOException
     */
    public GoMailModuleProcessor createProcessor() throws IOException;

    /**
     * create mail processor.
     * @param agent mail agent, if null createAgent() is called
     * @param config mail config, if null GoMailConfig.getDefaultInstance() is called
     * @return
     * @throws IOException
     */
    public GoMailModuleProcessor createProcessor(GoMailAgent agent,GoMailConfig config) throws IOException;

    /**
     * create mail config
     * @return mail agent instance, default is SplitRecipientsGoMailAgent.INSTANCE
     * @throws IOException
     */
    public GoMailAgent createAgent() throws IOException;
}
