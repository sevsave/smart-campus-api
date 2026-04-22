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
import com.smartcampus.exceptions.SensorUnavailableException;
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
        
        if (!DataStorage.getRooms().containsKey(sensor.getRoomId())) {
           throw new LinkedResourceNotFoundException("Room ID " + sensor.getRoomId() + " does not exist.");
        }

        DataStorage.getSensors().put(sensor.getId(), sensor);

        
        Room room = DataStorage.getRooms().get(sensor.getRoomId());
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }
    
  

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type,@QueryParam("status") String status) {
        if (type != null && !type.isEmpty()) {
            
            return DataStorage.getSensors().values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .filter(s -> (status == null || s.getStatus().equalsIgnoreCase(status)))
                    .collect(Collectors.toList());
        }
        return List.copyOf(DataStorage.getSensors().values());
    }

    
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (!DataStorage.getSensors().containsKey(sensorId)) {
            throw new NotFoundException("Sensor not found");
        }
        return new SensorReadingResource(sensorId);
    }
}