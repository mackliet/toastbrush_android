package com.toastbrush.toastbrush_android;

import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.toastbrush.ToastbrushApplication;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ToastbrushWebAPI
{
    private static final String API_URL = "https://j5zdzphhdk.execute-api.us-west-1.amazonaws.com/toastbrush/";
    private static final Map<String, String> endpoints;
    static {
        Map<String, String> endpoint_map = new HashMap<>();
        endpoint_map.put("AddComment", API_URL + "addcomment");
        endpoint_map.put("AddUser", API_URL + "adduser");
        endpoint_map.put("EditComment", API_URL + "editcomment");
        endpoint_map.put("EditImageDescription", API_URL + "editimagedescription");
        endpoint_map.put("EditUserDescription", API_URL + "edituserdescription");
        endpoint_map.put("EditUserImage", API_URL + "edituserimage");
        endpoint_map.put("GetComments", API_URL + "getimage");
        endpoint_map.put("GetImage", API_URL + "getimage");
        endpoint_map.put("GetImageInfo", API_URL + "getimageinfo");
        endpoint_map.put("GetImagesByKeyword", API_URL + "getimagesbykeyword");
        endpoint_map.put("GetImagesByUser", API_URL + "getimagesbyuser");
        endpoint_map.put("GetUser", API_URL + "getuser");
        endpoint_map.put("SendImage", API_URL + "sendimage");
        endpoint_map.put("VoteComment", API_URL + "votecomment");
        endpoint_map.put("VoteImage", API_URL + "voteimage");
        endpoints = Collections.unmodifiableMap(endpoint_map);
    }

    public enum OrderValue
    {
        NEWEST(0),
        OLDEST(1),
        HIGH_SCORE(2),
        LOW_SCORE(3);
        private int value;
        OrderValue(int value)
        {
            this.value = value;
        }
        public int asInt()
        {
            return value;
        }
    }
    public enum VoteValue
    {
        DOWN_VOTE(-1),
        NO_VOTE(0),
        UP_VOTE(1);
        private int value;
        VoteValue(int value)
        {
            this.value = value;
        }
        public int asInt()
        {
            return value;
        }
    }

    private static final Response.ErrorListener m_error_callback = new Response.ErrorListener()
    {
        @Override
        public void onErrorResponse(VolleyError error)
        {
            Toast.makeText(ToastbrushApplication.getAppContext(),"Error processing request:\n" + error.getMessage(),Toast.LENGTH_LONG).show();
        }
    };


    private ToastbrushWebAPI()
    {

    }

    private static void post(final String endpoint, final String body, final Response.Listener<String> callback)
    {
        StringRequest postRequest = new StringRequest(Request.Method.POST, endpoint, callback, m_error_callback)
        {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                try {
                    return body == null ? null : body.getBytes("utf-8");
                }
                catch(UnsupportedEncodingException e){ return null; }
            }
        };
        ToastbrushApplication.getRequestQueue().add(postRequest);
    }

    public static void addComment(String file, String user, String comment, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("File", file);
            request.put("User", user);
            request.put("Comment", comment);
            post(endpoints.get("AddComment"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void addUser(String user, String email, String description, String encoded, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("User", user);
            request.put("Email", email);
            request.put("Description", description);
            request.put("Encoded", encoded);
            post(endpoints.get("AddUser"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void editComment(int id, String comment, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("ID", id);
            request.put("Comment", comment);
            post(endpoints.get("EditComment"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void editImageDescription(String file, String description, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("File", file);
            request.put("Description", description);
            post(endpoints.get("EditImageDescription"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void editUserDescription(String user, String encoded, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("User", user);
            request.put("Encoded", encoded);
            post(endpoints.get("EditUserDescription"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void editUserImage(String user, String encoded, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("User", user);
            request.put("Encoded", encoded);
            post(endpoints.get("EditUserImage"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void getComments(String file, OrderValue order, int limit, int offset, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("File", file);
            request.put("Order", order.asInt());
            request.put("Limit", limit);
            request.put("Offset", offset);
            post(endpoints.get("GetComments"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void getImage(String name, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("Name", name);
            post(endpoints.get("GetImage"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void getImageInfo(String file, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("File", file);
            post(endpoints.get("GetImageInfo"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void getImagesByKeyword(String keyword, OrderValue order, int limit, int offset, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("Keyword", keyword);
            request.put("Order", order.asInt());
            request.put("Limit", limit);
            request.put("Offset", offset);
            post(endpoints.get("GetImagesByKeyword"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void getImagesByUser(String user, OrderValue order, int limit, int offset, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("User", user);
            request.put("Order", order.asInt());
            request.put("Limit", limit);
            request.put("Offset", offset);
            post(endpoints.get("GetImagesByUser"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void getUser(String user, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("User", user);
            post(endpoints.get("GetUser"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void sendImage(String name, String user, String description, String encoded, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("Name", name);
            request.put("User", user);
            request.put("Description", description);
            request.put("Encoded", encoded);
            post(endpoints.get("SendImage"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void voteComment(int id, String user, VoteValue value, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("ID", id);
            request.put("User", user);
            request.put("Value", value.asInt());
            post(endpoints.get("VoteComment"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }
    public static void voteImage(String file, String user, VoteValue value, Response.Listener<String> callback)
    {
        JSONObject request = new JSONObject();
        try
        {
            request.put("File", file);
            request.put("User", user);
            request.put("Value", value);
            post(endpoints.get("VoteImage"), request.toString(), callback);
        }
        catch(Exception e)
        {

        }
    }

}
