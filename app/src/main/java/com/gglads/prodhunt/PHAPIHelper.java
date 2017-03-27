package com.gglads.prodhunt;

import android.content.Context;
import android.content.Intent;

import com.gglads.prodhunt.Entities.Product;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.koushikdutta.ion.Ion.with;


public class PHAPIHelper {

    public static void authorize(Context context) throws InterruptedException, ExecutionException, TimeoutException {
        JsonObject json = new JsonObject();
        json.addProperty("client_id", Constants.API_KEY);
        json.addProperty("client_secret", Constants.API_SEC);
        json.addProperty("grant_type", "client_credentials");
        json.addProperty("code", Constants.API_TOK);

        Future fut = with(context)
                .load("https://api.producthunt.com/v1/oauth/token")
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if(result != null && result.has("access_token")) {
                            Constants.CLIENT_TOKEN = result.get("access_token").getAsString();
                        }
                    }
                });

        fut.get(5000, TimeUnit.MILLISECONDS);
    }

    public static void updateProducts(final Context context) {
        try {
            if (Constants.CLIENT_TOKEN.isEmpty())
                authorize(context);
            Calendar cal = Calendar.getInstance();
            final String cat = Prefs.getInstance().getCurrentCat();
            with(context)
                    .load(String.format("https://api.producthunt.com/v1/categories/%s/posts?day=%d-%02d-%02d", cat, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DATE)))
                    .setTimeout(2000)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Authorization", "Bearer " + Constants.CLIENT_TOKEN)
                    .setHeader("Host", "api.producthunt.com")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            Intent intent = null;
                            try {
                                if (result != null && result.has("posts")) {
                                    JsonObject json = null;
                                    Product product = null;
                                    Integer lasId = Prefs.getInstance().getLastPostID(cat);
                                    for (Iterator<JsonElement> jit = result.getAsJsonArray("posts").iterator(); jit.hasNext(); ) {
                                        json = (JsonObject) jit.next();
                                        product = new Product(json.get("id").getAsInt(), json.get("name").getAsString(), json.get("tagline").getAsString(), json.get("votes_count").getAsString());
                                        product.setThumb(json.getAsJsonObject("thumbnail").get("image_url").getAsString());
                                        product.setScreen(json.getAsJsonObject("screenshot_url").getAsJsonPrimitive("300px").getAsString());
                                        if (product.getID() > lasId) {
                                            lasId = product.getID();
                                            Prefs.getInstance().setLastPostID(cat, lasId);
                                        }
                                        intent = new Intent();
                                        intent.setAction(Constants.UPDATE_PRODUCT_ACTION);
                                        intent.putExtra("PRODUCT", product);
                                        context.sendBroadcast(intent);
                                    }
                                    intent = new Intent();
                                    intent.setAction(Constants.STOP_REFRESH_ACTION);
                                    context.sendBroadcast(intent);
                                }
                            } catch (Exception er) {
                                intent.setAction(Constants.STOP_REFRESH_ACTION);
                                context.sendBroadcast(intent);
                                intent = new Intent();
                                intent.setAction(Constants.ERROR_ACTION);
                                intent.putExtra("ERROR", er);
                                context.sendBroadcast(intent);
                            }
                        }
                    });
        }
        catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Constants.STOP_REFRESH_ACTION);
            context.sendBroadcast(intent);
            intent = new Intent();
            intent.setAction(Constants.ERROR_ACTION);
            intent.putExtra("ERROR", e);
            context.sendBroadcast(intent);
        }
    }


    public static void updateTimeProducts(final Context context) {
        try {
            if (Constants.CLIENT_TOKEN.isEmpty())
                authorize(context);
            Calendar cal = Calendar.getInstance();
            final String cat = Prefs.getInstance().getCurrentCat();
            with(context)
                    .load("https://api.producthunt.com/v1/posts/all?newer=" + Prefs.getInstance().getLastPostID(cat))
                    .setTimeout(2000)
                    .setHeader("Accept", "application/json")
                    .setHeader("Content-Type", "application/json")
                    .setHeader("Authorization", "Bearer " + Constants.CLIENT_TOKEN)
                    .setHeader("Host", "api.producthunt.com")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            Intent intent = null;
                            try {
                                if (result != null && result.has("posts")) {
                                    int n = 0;
                                    JsonObject json = null;
                                    Product product = null;
                                    intent = new Intent();
                                    ArrayList<String> keys = new ArrayList<>();
                                    for (Iterator<JsonElement> jit = result.getAsJsonArray("posts").iterator(); jit.hasNext(); )
                                    {
                                        json = (JsonObject) jit.next();
                                        if (!json.get("category_id").getAsString().equals("1"))
                                            continue;
                                        n++;
                                        if (n > 1)
                                            continue;
                                        product = new Product(json.get("id").getAsInt(), json.get("name").getAsString(), json.get("tagline").getAsString(), json.get("votes_count").getAsString());
                                        String tl = json.getAsJsonObject("thumbnail").get("image_url").getAsString();
                                        product.setThumb(tl);
                                        try {
                                            product.setThumb(Ion.with(context).load(tl).asBitmap().get(10000, TimeUnit.MILLISECONDS));
                                        }
                                        catch (Exception et) {
                                            // so sad((
                                        }
                                        product.setScreen(json.getAsJsonObject("screenshot_url").getAsJsonPrimitive("300px").getAsString());

                                    }
                                    intent.putExtra("PCOUNT", n);
                                    if (n > 1) {
                                        if (n == 1) {
                                            intent.putExtra("PNAME", product.getName());
                                            intent.putExtra("PDESC", product.getDesc());
                                            intent.putExtra("PVOTES", product.getUpvotes());
                                            intent.putExtra("PTHUMB", product.getThumb());
                                        }
                                        intent.setAction(Constants.TIME_NOTIFY_ACTION);
                                        context.sendBroadcast(intent);
                                    }
                                }
                            } catch (Exception er) {
                                intent.setAction(Constants.STOP_REFRESH_ACTION);
                                context.sendBroadcast(intent);
                                intent = new Intent();
                                intent.setAction(Constants.ERROR_ACTION);
                                intent.putExtra("ERROR", er);
                                context.sendBroadcast(intent);
                            }
                        }
                    });
        }
        catch (Exception e) {
            Intent intent = new Intent();
            intent.setAction(Constants.STOP_REFRESH_ACTION);
            context.sendBroadcast(intent);
            intent = new Intent();
            intent.setAction(Constants.ERROR_ACTION);
            intent.putExtra("ERROR", e);
            context.sendBroadcast(intent);
        }
    }
}
