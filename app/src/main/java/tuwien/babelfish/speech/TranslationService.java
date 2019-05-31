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
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.ImageLoader;
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

    public static synchronized TranslationService getInstance(Context context) {
        if (instance == null) {
            instance = new TranslationService(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


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

    public void cancelRequests(){
        getRequestQueue().cancelAll(TAG);
    }
}
