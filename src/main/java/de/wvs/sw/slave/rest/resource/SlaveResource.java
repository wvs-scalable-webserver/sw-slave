package de.wvs.sw.slave.rest.resource;

import com.google.gson.Gson;
import de.progme.hermes.server.http.Request;
import de.progme.hermes.server.http.annotation.Path;
import de.progme.hermes.server.http.annotation.Produces;
import de.progme.hermes.server.http.annotation.method.GET;
import de.progme.hermes.shared.ContentType;
import de.progme.hermes.shared.http.Response;
import de.wvs.sw.slave.rest.response.SlaveResponse;
import de.wvs.sw.slave.rest.response.SlaveStatsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Marvin Erkes on 04.02.2020.
 */
@Path("/slavee")
public class SlaveResource {

    private static Logger logger = LoggerFactory.getLogger(SlaveResource.class);
    private static Gson gson = new Gson();

    @GET
    @Path("/stats")
    @Produces(ContentType.APPLICATION_JSON)
    public Response stats(Request httpRequest) {

        return Response
                .ok()
                .content(gson.toJson(new SlaveStatsResponse(SlaveResponse.Status.OK, "OK"))).build();
    }
}
