/**
 * BabelFish
 * Copyright (C) 2019  Julia Gleichweit
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tuwien.babelfish.speech;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides interface to use the frengly translation web service REST api (http://www.frengly.com/)
 */
public class TranslationService {

    public static final String TAG = "TRANSL";
    private static final String SERVICE =  "http://frengly.com/frengly/data/translateREST";

    private static TranslationService instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private TranslationService(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    /**
     * Creates a new TranslationService with the associated context. If the service
     * was already created, subsequent calls do not change the context and the instance with
     * the original context is returned.
     *
     * @param context current activity context; can be null after first call
     * @return TranslationService instance
     */
    public static synchronized TranslationService getInstance(Context context) {
        if (instance == null) {
            instance = new TranslationService(context);
        }
        return instance;
    }


    /**
     * Returns an already existing or new Volley RequestQueue to perform network requests on
     * @return a started RequestQueue instance
     */
    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    /**
     * Add request to the Volley queue.
     *
     * @param req not null JSONObject
     * @param <T> type org.json.JSONObject
     */
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


    /**
     * Sends a JSONObject to the frengly service to obtain the translated text.
     * Responses or errors are sent to the passed listeners.
     *
     * @param translate text to be translated
     * @param from source language code
     * @param to target language code
     * @param responseListener class implementing Volley.Response.Listener
     * @param errorListener class implementing Volley.Response.ErrorListener
     */
    public void translate(String translate, String from, String to, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("src", from);
        jsonObject.put("dest", to);
        jsonObject.put("text", translate);
        jsonObject.put("email", "y180887@nwytg.net");
        jsonObject.put("password", "TUwienBabel");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, SERVICE, jsonObject, responseListener,errorListener);
        // Set the tag on the request.
        jsonObjectRequest.setTag(TAG);
        // Add a request (in this example, called stringRequest) to your RequestQueue.
        addToRequestQueue(jsonObjectRequest);

    }

    /**
     * Cancels all pending translation associated with TranslationService.TAG.
     */
    public void cancelRequests(){
        getRequestQueue().cancelAll(TAG);
    }
}
