package com.comp655.distributedgradebook;

import com.comp655.distributedgradebook.resources.GradeBookResource;
import com.comp655.distributedgradebook.resources.SecondaryResource;
import com.comp655.distributedgradebook.resources.SystemNetworkResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * Configures Jakarta RESTful Web Services for the application.
 * @author Juneau
 */
@ApplicationPath("/")
public class GradeBookApplication extends Application {

    private Set<Object> singletons = new HashSet<Object>();

    public GradeBookApplication() {
        singletons.add(new GradeBookResource());
        singletons.add(new SecondaryResource());
        singletons.add(new SystemNetworkResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}
