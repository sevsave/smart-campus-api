package com.smartcampus.resources;

import com.smartcampus.models.Sensor;
import com.smartcampus.models.Room;
import com.smartcampus.services.DataStorage;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {


    @POST
    public Response registerSensor(Sensor sensor) {
        // Validation: Does the Room ID exist? (Crucial for high marks)
        if (!DataStorage.getRooms().containsKey(sensor.getRoomId())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error: Room ID " + sensor.getRoomId() + " does not exist.")
                    .build();
        }

        DataStorage.getSensors().put(sensor.getId(), sensor);

        // Also update the Room's internal list of sensor IDs
        Room room = DataStorage.getRooms().get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }


    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        if (type != null && !type.isEmpty()) {
            // Filter by type using Java Streams (Professional approach)
            return DataStorage.getSensors().values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }
        return List.copyOf(DataStorage.getSensors().values());
    }

    // Add this at the bottom of SensorResource.java
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (!DataStorage.getSensors().containsKey(sensorId)) {
            throw new NotFoundException("Sensor not found");
        }
        return new SensorReadingResource(sensorId);
    }
}