package be.alexandre01.dreamnetwork.api.connection.core.communication;

import be.alexandre01.dreamnetwork.api.connection.core.request.RequestInfo;

import java.util.HashMap;

/*
 ↬   Made by Alexandre01Dev 😎
 ↬   done on 07/10/2023 at 14:04
*/
public abstract class IResponsesCollection {
    final HashMap<String, CoreResponse> responses = new HashMap<>();


    public void addResponse(String response, CoreResponse coreResponse){
        responses.put(response,coreResponse);
    }
    public void addResponse( CoreResponse coreResponse){
        responses.put(coreResponse.getClass().getSimpleName(),coreResponse);
    }
    public CoreResponse getResponses(Class<? extends CoreResponse> response){
        return responses.get(response.getSimpleName());
    }
    public CoreResponse getResponses(String response){
        return responses.get(response);
    }
}
