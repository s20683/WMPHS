package com.s20683.wmphs.gui2wmphs.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SimpleResponse {
    private boolean success;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorMessage;

    public SimpleResponse() {}
    @JsonCreator
    public SimpleResponse(
            @JsonProperty("success") boolean success,
            @JsonProperty("errorMessage") String errorMessage)
    {
        this.success = success;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "SimpleResponse{" +
                "success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
    public static SimpleResponse createOk(){
        return new SimpleResponse(true, "");
    }
    public static SimpleResponse createSimpleOk(){
        return new SimpleResponse(true, null);
    }
}