/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.common.rest.api.service;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.syncope.common.lib.patch.StatusPatch;
import org.apache.syncope.common.lib.patch.UserPatch;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.to.ProvisioningResult;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.beans.AnyQuery;

/**
 * REST operations for users.
 */
@Tag(name = "Users")
@SecurityRequirements({
    @SecurityRequirement(name = "BasicAuthentication")
    , @SecurityRequirement(name = "Bearer") })
@Path("users")
public interface UserService extends AnyService<UserTO> {

    @ApiResponses(
            @ApiResponse(responseCode = "200", description =
                    "User matching the provided key; if value looks like a UUID then it is interpreted as key,"
                    + " otherwise as a username.", headers =
                    @Header(name = HttpHeaders.ETAG, schema =
                            @Schema(type = "string"),
                            description = "Opaque identifier for the latest modification made to the entity returned"
                            + " by this endpoint")))
    @Override
    UserTO read(String key);

    @Override
    PagedResult<UserTO> search(AnyQuery anyQuery);

    /**
     * Creates a new user.
     *
     * @param userTO user to be created
     * @param storePassword whether password shall be stored internally
     * @return Response object featuring Location header of created user as well as the user itself
     * enriched with propagation status information
     */
    @Parameter(name = RESTHeaders.PREFER, in = ParameterIn.HEADER,
            description = "Allows client to specify a preference for the result to be returned from the server",
            allowEmptyValue = true, schema =
            @Schema(defaultValue = "return-content", allowableValues = { "return-content", "return-no-content" }))
    @ApiResponses(
            @ApiResponse(responseCode = "201",
                    description = "User successfully created enriched with propagation status information, as Entity,"
                    + "or empty if 'Prefer: return-no-content' was specified",
                    content =
                    @Content(schema =
                            @Schema(implementation = ProvisioningResult.class)), headers = {
                @Header(name = RESTHeaders.RESOURCE_KEY, schema =
                        @Schema(type = "string"),
                        description = "UUID generated for the user created")
                , @Header(name = HttpHeaders.LOCATION, schema =
                        @Schema(type = "string"),
                        description = "URL of the user created")
                , @Header(name = RESTHeaders.PREFERENCE_APPLIED, schema =
                        @Schema(type = "string"),
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied") }))
    @POST
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response create(
            @NotNull UserTO userTO,
            @DefaultValue("true") @QueryParam("storePassword") boolean storePassword);

    /**
     * Updates user matching the provided key.
     *
     * @param userPatch modification to be applied to user matching the provided key
     * @return Response object featuring the updated user enriched with propagation status information
     */
    @Parameter(name = RESTHeaders.PREFER, in = ParameterIn.HEADER,
            description = "Allows client to specify a preference for the result to be returned from the server",
            allowEmptyValue = true, schema =
            @Schema(defaultValue = "return-content", allowableValues = { "return-content", "return-no-content" }))
    @Parameter(name = HttpHeaders.IF_MATCH, in = ParameterIn.HEADER,
            description = "When the provided ETag value does not match the latest modification date of the entity, "
            + "an error is reported and the requested operation is not performed.",
            allowEmptyValue = true, schema =
            @Schema(type = "string"))
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                description = "User successfully updated enriched with propagation status information, as Entity",
                content =
                @Content(schema =
                        @Schema(implementation = ProvisioningResult.class)))
        , @ApiResponse(responseCode = "204",
                description = "No content if 'Prefer: return-no-content' was specified", headers =
                @Header(name = RESTHeaders.PREFERENCE_APPLIED, schema =
                        @Schema(type = "string"),
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied"))
        , @ApiResponse(responseCode = "412",
                description = "The ETag value provided via the 'If-Match' header does not match the latest modification"
                + " date of the entity") })
    @PATCH
    @Path("{key}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response update(@NotNull UserPatch userPatch);

    /**
     * Updates user matching the provided key.
     *
     * @param userTO complete update
     * @return Response object featuring the updated user enriched with propagation status information
     */
    @Parameter(
            name = RESTHeaders.PREFER, in = ParameterIn.HEADER,
            description = "Allows client to specify a preference for the result to be returned from the server",
            allowEmptyValue = true, schema =
            @Schema(defaultValue = "return-content", allowableValues = { "return-content", "return-no-content" }))
    @Parameter(name = HttpHeaders.IF_MATCH, in = ParameterIn.HEADER,
            description = "When the provided ETag value does not match the latest modification date of the entity, "
            + "an error is reported and the requested operation is not performed.",
            allowEmptyValue = true, schema =
            @Schema(type = "string"))
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                description = "User successfully updated enriched with propagation status information, as Entity",
                content =
                @Content(schema =
                        @Schema(implementation = ProvisioningResult.class)))
        ,
        @ApiResponse(responseCode = "204",
                description = "No content if 'Prefer: return-no-content' was specified", headers =
                @Header(name = RESTHeaders.PREFERENCE_APPLIED, schema =
                        @Schema(type = "string"),
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied"))
        , @ApiResponse(responseCode = "412",
                description = "The ETag value provided via the 'If-Match' header does not match the latest modification"
                + " date of the entity") })
    @PUT
    @Path("{key}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response update(@NotNull UserTO userTO);

    /**
     * Performs a status update on given user.
     *
     * @param statusPatch status update details
     * @return Response object featuring the updated user enriched with propagation status information
     */
    @Parameter(
            name = RESTHeaders.PREFER, in = ParameterIn.HEADER,
            description = "Allows client to specify a preference for the result to be returned from the server",
            allowEmptyValue = true, schema =
            @Schema(defaultValue = "return-content", allowableValues = { "return-content", "return-no-content" }))
    @Parameter(name = HttpHeaders.IF_MATCH, in = ParameterIn.HEADER,
            description = "When the provided ETag value does not match the latest modification date of the entity, "
            + "an error is reported and the requested operation is not performed.",
            allowEmptyValue = true, schema =
            @Schema(type = "string"))
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                description = "User successfully updated enriched with propagation status information, as Entity",
                content =
                @Content(schema =
                        @Schema(implementation = ProvisioningResult.class)))
        , @ApiResponse(responseCode = "204",
                description = "No content if 'Prefer: return-no-content' was specified", headers =
                @Header(name = RESTHeaders.PREFERENCE_APPLIED, schema =
                        @Schema(type = "string"),
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied"))
        , @ApiResponse(responseCode = "412",
                description = "The ETag value provided via the 'If-Match' header does not match the latest modification"
                + " date of the entity") })
    @POST
    @Path("{key}/status")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response status(@NotNull StatusPatch statusPatch);
}
