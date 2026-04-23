/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.smartcampus.resources;
/**
 *
 * @author User
 */

import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.Room;
import com.smartcampus.models.SensorReading;
import com.smartcampus.services.DataStorage;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    
    @POST
    public Response registerSensor(Sensor sensor) {
        // Validation: Ensure the parent Room exists
        if (!DataStorage.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room ID " + sensor.getRoomId() + " does not exist.");
        }

        // Save Sensor to Memory
        DataStorage.getSensors().put(sensor.getId(), sensor);

        // Update Room's list of sensors
        Room room = DataStorage.getRooms().get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

   
    @GET
    public List<Sensor> getAllSensors(
            @QueryParam("status") String status, 
            @QueryParam("type") String type) {
        
        List<Sensor> sensors = new ArrayList<>(DataStorage.getSensors().values());

        // Filter by Status (e.g., ?status=active)
        if (status != null && !status.isEmpty()) {
            sensors = sensors.stream()
                .filter(s -> s.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        }

        
        if (type != null && !type.isEmpty()) {
            sensors = sensors.stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
        }

        return sensors;
    }

    
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStorage.getSensors().get(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\": \"Sensor " + sensorId + " not found.\"}")
                           .build();
        }

        return Response.ok(sensor).build();
    }

    
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (!DataStorage.getSensors().containsKey(sensorId)) {
            throw new NotFoundException("Sensor not found");
        }
        return new SensorReadingResource(sensorId);
    }
}