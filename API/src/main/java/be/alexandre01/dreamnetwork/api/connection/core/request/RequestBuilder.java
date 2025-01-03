package be.alexandre01.dreamnetwork.api.connection.core.request;

import be.alexandre01.dreamnetwork.api.connection.core.communication.UniversalConnection;
import be.alexandre01.dreamnetwork.api.utils.messages.Message;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Data;
import lombok.Getter;
import lombok.Synchronized;

@Data
public class RequestBuilder {
    @Getter private static final Multimap<RequestInfo, RequestData> globalRequestData = ArrayListMultimap.create();
    protected Multimap<RequestInfo, RequestData> requestData;

    public RequestBuilder(){
        requestData = ArrayListMultimap.create();
    }

    public void addRequestBuilder(RequestBuilder... requestBuilders){
        for (RequestBuilder requestBuilder : requestBuilders) {
            requestData.putAll(requestBuilder.requestData);
        }
    }


    public static void addGlobalRequestData(RequestBuilder... requestBuilders){
        for (RequestBuilder requestBuilder : requestBuilders) {
            globalRequestData.putAll(requestBuilder.requestData);
        }
    }

    public interface RequestData {
        /**
         *
         * @param message
         * @param client
         * @param args
         * @return the {@linkplain Message message}
         */
        public Message write(Message message, UniversalConnection client, Object... args);

    }
}
