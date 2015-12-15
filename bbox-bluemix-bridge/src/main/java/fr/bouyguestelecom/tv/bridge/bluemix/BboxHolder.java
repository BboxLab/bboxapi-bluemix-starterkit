/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2015 BboxLab
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bouyguestelecom.tv.bridge.bluemix;

import android.content.Context;
import android.util.Log;

import fr.bouyguestelecom.tv.openapi.secondscreen.authenticate.IAuthCallback;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.Bbox;
import fr.bouyguestelecom.tv.openapi.secondscreen.bbox.BboxManager;

/**
 * @author Bertrand Martel
 */
public class BboxHolder {

    private static final String TAG = BboxHolder.class.getCanonicalName();

    public static BboxHolder mInstance = new BboxHolder();
    private Bbox mBbox;
    private BboxManager bboxManager = new BboxManager();

    /**
     * Singleton: private constructor. Instance must be retrieved with getInstance method
     */
    private BboxHolder() {
    }

    public BboxManager getBboxManager() {
        return bboxManager;
    }

    public void bboxSearch(final Context context, final IAuthCallback callback) {

        Log.i("BboxManager", "Start looking for Bbox");
        bboxManager.startLookingForBbox(context, new BboxManager.CallbackBboxFound() {
            @Override
            public void onResult(final Bbox bboxFound) {

                // When we find our Bbox, we stopped looking for other Bbox.
                bboxManager.stopLookingForBbox();

                // We save our Bbox.
                mBbox = bboxFound;

                Log.i(TAG, "Bbox found: " + mBbox.getIp());

                mBbox.authenticate(BuildConfig.BBOXAPI_APP_ID, BuildConfig.BBOXAPI_APP_SECRET, callback);
            }
        });

    }

    /**
     * set the current bbox
     *
     * @param ip bbox ip
     */
    public void setCustomBbox(String ip) {
        mBbox = new Bbox(ip);
    }

    /**
     * Do authentication.
     *
     * @param appId     application id.
     * @param appSecret application secret.
     * @param callback  callback when done.
     */
    public void authenticate(String appId, String appSecret, IAuthCallback callback) {
        if (mBbox != null) {
            mBbox.authenticate(appId, appSecret, callback);
        }
    }

    /**
     * Return the current bbox. null if not correctly initialized !
     *
     * @return the bbox.
     */
    public Bbox getBbox() throws BboxNotFoundException {
        if (mBbox == null) {
            throw new BboxNotFoundException();
        }
        return mBbox;
    }

    public static BboxHolder getInstance() {
        return mInstance;
    }

    public interface IBboxSearchCallback {
        public void onBboxFound();
    }
}
