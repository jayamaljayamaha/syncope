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
import java.util.Set;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.syncope.common.lib.patch.AssociationPatch;
import org.apache.syncope.common.lib.patch.DeassociationPatch;
import org.apache.syncope.common.lib.to.AnyTO;
import org.apache.syncope.common.lib.to.AttrTO;
import org.apache.syncope.common.lib.to.BulkAction;
import org.apache.syncope.common.lib.to.BulkActionResult;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.to.ProvisioningResult;
import org.apache.syncope.common.lib.types.SchemaType;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.beans.AnyQuery;

public interface AnyService<TO extends AnyTO> extends JAXRSService {

    /**
     * Reads the list of attributes owned by the given any object for the given schema type.
     *
     * Note that for the UserService, GroupService and AnyObjectService subclasses, if the key parameter
     * looks like a UUID then it is interpreted as as key, otherwise as a (user)name.
     *
     * @param key any object key or name
     * @param schemaType schema type
     * @return list of attributes, owned by the given any object, for the given schema type
     */
    @GET
    @Path("{key}/{schemaType}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Set<AttrTO> read(@NotNull @PathParam("key") String key, @NotNull @PathParam("schemaType") SchemaType schemaType);

    /**
     * Reads the attribute, owned by the given any object, for the given schema type and schema.
     *
     * Note that for the UserService, GroupService and AnyObjectService subclasses, if the key parameter
     * looks like a UUID then it is interpreted as as key, otherwise as a (user)name.
     *
     * @param key any object key or name
     * @param schemaType schema type
     * @param schema schema
     * @return attribute, owned by the given any object, for the given schema type and schema
     */
    @GET
    @Path("{key}/{schemaType}/{schema}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    AttrTO read(
            @NotNull @PathParam("key") String key,
            @NotNull @PathParam("schemaType") SchemaType schemaType,
            @NotNull @PathParam("schema") String schema);

    /**
     * Reads the any object matching the provided key.
     *
     * @param key if value looks like a UUID then it is interpreted as key, otherwise as a (user)name
     * @return any object with matching key
     */
    @GET
    @Path("{key}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    TO read(@NotNull @PathParam("key") String key);

    /**
     * Returns a paged list of any objects matching the given query.
     *
     * @param anyQuery query conditions
     * @return paged list of any objects matching the given query
     */
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    PagedResult<TO> search(@BeanParam AnyQuery anyQuery);

    /**
     * Adds or replaces the attribute, owned by the given any object, for the given schema type and schema.
     *
     * @param key any object key or name
     * @param schemaType schema type
     * @param attrTO attribute
     * @return Response object featuring the updated any object attribute - as Entity
     */
    @PUT
    @Path("{key}/{schemaType}/{schema}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response update(
            @NotNull @PathParam("key") String key,
            @NotNull @PathParam("schemaType") SchemaType schemaType,
            @NotNull AttrTO attrTO);

    /**
     * Deletes the attribute, owned by the given any object, for the given schema type and schema.
     *
     * @param key any object key or name
     * @param schemaType schema type
     * @param schema schema
     * @return an empty response if operation was successful
     */
    @DELETE
    @Path("{key}/{schemaType}/{schema}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response delete(
            @NotNull @PathParam("key") String key,
            @NotNull @PathParam("schemaType") SchemaType schemaType,
            @NotNull @PathParam("schema") String schema);

    /**
     * Deletes any object matching provided key.
     *
     * @param key any object key or name
     * @return Response object featuring the deleted any object enriched with propagation status information
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
                description = "User, Group or Any Object successfully deleted enriched with propagation status "
                + "information, as Entity", content =
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
    @DELETE
    @Path("{key}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response delete(@NotNull @PathParam("key") String key);

    /**
     * Executes resource-related operations on given any object.
     *
     * @param patch external resources to be used for propagation-related operations
     * @return Response object featuring BulkActionResult as Entity
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
                description = "Bulk action result", content =
                @Content(schema =
                        @Schema(implementation = BulkActionResult.class)))
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
    @Path("{key}/deassociate/{action}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response deassociate(@NotNull DeassociationPatch patch);

    /**
     * Executes resource-related operations on given any object.
     *
     * @param patch external resources to be used for propagation-related operations
     * @return Response object featuring BulkActionResult as Entity
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
                description = "Bulk action result", content =
                @Content(schema =
                        @Schema(implementation = BulkActionResult.class)))
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
    @Path("{key}/associate/{action}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response associate(@NotNull AssociationPatch patch);

    /**
     * Executes the provided bulk action.
     *
     * @param bulkAction list of any object ids against which the bulk action will be performed.
     * @return Response object featuring BulkActionResult as Entity
     */
    @Parameter(name = RESTHeaders.PREFER, in = ParameterIn.HEADER,
            description = "Allows client to specify a preference for the result to be returned from the server",
            allowEmptyValue = true, schema =
            @Schema(defaultValue = "return-content", allowableValues = { "return-content", "return-no-content" }))
    @ApiResponses({
        @ApiResponse(responseCode = "200",
                description = "Bulk action result", content =
                @Content(schema =
                        @Schema(implementation = BulkActionResult.class)))
        , @ApiResponse(responseCode = "204",
                description = "No content if 'Prefer: return-no-content' was specified", headers =
                @Header(name = RESTHeaders.PREFERENCE_APPLIED, schema =
                        @Schema(type = "string"),
                        description = "Allows the server to inform the "
                        + "client about the fact that a specified preference was applied")) })
    @POST
    @Path("bulk")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    Response bulk(@NotNull BulkAction bulkAction);
}
