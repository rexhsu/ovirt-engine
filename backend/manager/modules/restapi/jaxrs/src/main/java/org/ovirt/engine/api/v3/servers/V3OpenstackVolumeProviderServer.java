/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProviderResource;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3OpenStackVolumeProvider;

@Produces({"application/xml", "application/json"})
public class V3OpenstackVolumeProviderServer extends V3Server<OpenstackVolumeProviderResource> {
    public V3OpenstackVolumeProviderServer(OpenstackVolumeProviderResource delegate) {
        super(delegate);
    }

    @GET
    public V3OpenStackVolumeProvider get() {
        return adaptGet(getDelegate()::get);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3OpenStackVolumeProvider update(V3OpenStackVolumeProvider provider) {
        return adaptUpdate(getDelegate()::update, provider);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @Path("volumetypes")
    public V3OpenstackVolumeTypesServer getVolumeTypesResource() {
        return new V3OpenstackVolumeTypesServer(getDelegate().getVolumeTypesResource());
    }

    @Path("authenticationkeys")
    public V3OpenstackVolumeAuthenticationKeysServer getAuthenticationKeysResource() {
        return new V3OpenstackVolumeAuthenticationKeysServer(getDelegate().getAuthenticationKeysResource());
    }

    @Path("{action: (importcertificates|testconnectivity)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
