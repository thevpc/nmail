/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.gomail;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

/**
 *
 * @author vpc
 */
public interface GoMailDataSourceFactory {

    int getSupportLevel(String type);

    GoMailDataSource create(String type, URL url);
    
    GoMailDataSource create(String type, Reader reader);

    GoMailDataSource create(String type, InputStream stream);

    GoMailDataSource create(String type, File file);
}
