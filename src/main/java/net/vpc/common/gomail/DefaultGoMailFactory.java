/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.common.gomail;

import net.vpc.common.gomail.modules.GoMailModuleProcessor;
import net.vpc.common.gomail.modules.SplitRecipientsGoMailAgent;

import java.io.IOException;

/**
 *
 * @author taha.bensalah@gmail.com
 */
public class DefaultGoMailFactory implements  GoMailFactory{
    public static final GoMailFactory INSTANCE=new DefaultGoMailFactory();

    public GoMailModuleProcessor createProcessor() throws IOException{
        return createProcessor(null,null);
    }

    public GoMailModuleProcessor createProcessor(GoMailAgent agent,GoMailConfig config) throws IOException{
        if(config==null){
            config=GoMailConfig.getDefaultInstance(); //configure with context class loader
        }
        if(agent==null){
            agent=createAgent();
        }
        return new GoMailModuleProcessor(agent, config);
    }

    @Override
    public GoMailAgent createAgent() throws IOException {
        return SplitRecipientsGoMailAgent.INSTANCE;
    }
}
