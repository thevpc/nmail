/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import net.thevpc.gomail.modules.GoMailModuleProcessor;
import net.thevpc.gomail.modules.SplitRecipientsGoMailAgent;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class DefaultGoMailFactory implements  GoMailFactory{
    public static final GoMailFactory INSTANCE=new DefaultGoMailFactory();

    public GoMailModuleProcessor createProcessor() {
        return createProcessor(null,null);
    }

    public GoMailModuleProcessor createProcessor(GoMailAgent agent,GoMailConfig config) {
        if(config==null){
            config=GoMailConfig.getDefaultInstance(); //configure with context class loader
        }
        if(agent==null){
            agent=createAgent();
        }
        return new GoMailModuleProcessor(agent, config);
    }

    @Override
    public GoMailAgent createAgent() {
        return SplitRecipientsGoMailAgent.INSTANCE;
    }
}
