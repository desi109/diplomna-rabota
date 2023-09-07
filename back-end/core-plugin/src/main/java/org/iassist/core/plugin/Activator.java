package org.iassist.core.plugin;

import java.util.Hashtable;

import org.iassist.core.plugin.impl.Service;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.iassist.spring.boot.api.AppService;

public class Activator implements BundleActivator {

    public static BundleContext bundleContext;

    @Override
    public void start(BundleContext context) {
        bundleContext = context;
        registerServices();
    }

    @Override
    public void stop(BundleContext context) {
        bundleContext = null;
    }

    public static BundleContext getBundleContext() {
        return bundleContext;
    }

    private void registerServices() {
        AppService service = new Service();
        bundleContext.registerService(AppService.class.getName(), service, new Hashtable<>());
        System.out.println("Service registered: " + service.name());
    }

}
