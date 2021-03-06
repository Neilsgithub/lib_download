package download.imageLoader.core;

import com.litesuits.go.OverloadPolicy;
import com.litesuits.go.SchedulePolicy;
import com.litesuits.go.SmartExecutor;
import download.imageLoader.config.ImageConfig;
import download.imageLoader.listener.BackListener;
import download.imageLoader.listener.CustomDisplayMethod;
import download.imageLoader.request.BitmapRequest;
import download.imageLoader.util.ViewTaskUtil;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class ImageLoader {
	private SmartExecutor executor;
	private static class InstanceHoler {
		private static final ImageLoader instance = new ImageLoader();
	}
	public static ImageLoader getInstance() {
		return InstanceHoler.instance;
	}

	private RunningTasksManager mRunningTasksManager;
	private ImageConfig config = null;
	private final int threadCount = 3;

	public RunningTasksManager getmRunningTasksManager(){
		if (mRunningTasksManager == null)
			mRunningTasksManager = new RunningTasksManager(threadCount);
		return mRunningTasksManager;
	}
	public ImageConfig getConfig(){
		return config;
	}
	private ImageLoader() {
		mRunningTasksManager = new RunningTasksManager(threadCount);
		executor = new SmartExecutor(threadCount, 200);
		executor.setSchedulePolicy(SchedulePolicy.FirstInFistRun);
		executor.setOverloadPolicy(OverloadPolicy.DiscardOldTaskInQueue);
		config = new ImageConfig();
	}

	protected void setLoadingAndFailedId(Context context, int loadingId,
			int failedId) {
		config.setFailedIdAndLoadingId(context.getResources(), failedId,
				loadingId);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void loadImage(String path, View view,BackListener listener) {
		BitmapRequest request = new BitmapRequest(view, path,listener);
		loadImage(request);
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void loadImage(String path, View view,
							 CustomDisplayMethod customDisplayMethod) {
		BitmapRequest request = new BitmapRequest(view, path);
		request.customDisplayMethod = customDisplayMethod;
		loadImage(request);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void loadImage(String path, View view,int width,int height,
							 CustomDisplayMethod customDisplayMethod) {
		BitmapRequest request = new BitmapRequest(view, path);
		request.customDisplayMethod = customDisplayMethod;
		loadImage(request);
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void loadImage(String path, View view,int width,int height,BackListener listener) {
		BitmapRequest request = new BitmapRequest(view, path,listener);
		loadImage(request);
	}

	protected void preLoad(String path) {
		BitmapRequest request = new BitmapRequest(path);
		loadImage(request);
	}

	protected void loadImage(String path,View view) {
		BitmapRequest request = new BitmapRequest(view, path);
		loadImage(request);
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	protected void loadImage(BitmapRequest request) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			if (request.view == null || request.view.get() == null){

			}else {
				getInstance();
				config.cache.setDiskLruCache(request.view.get().getContext()
						.getApplicationContext());
				config.initDefault(request.view.get().getContext());
				config.cache.getMemoryCache(request);
				if (!request.checkIfNeedAsyncLoad()) {
					request.display();
				} else {
					ViewTaskUtil.cancelOldTask(executor, request.view.get());
					LoadTask task = new LoadTask(request, ImageLoader.this);
					request.displayLoading(config.getLoadingBm());
					request.view.get().setTag(request.path);
					executor.execute(task);
				}

			}
		} else {
			throw new RuntimeException("only run on ui thread");
		}
	}

}