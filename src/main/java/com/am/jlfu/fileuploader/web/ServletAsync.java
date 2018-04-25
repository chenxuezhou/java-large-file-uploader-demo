package com.am.jlfu.fileuploader.web;


import com.am.jlfu.authorizer.Authorizer;
import com.am.jlfu.fileuploader.exception.UploadIsCurrentlyDisabled;
import com.am.jlfu.fileuploader.logic.UploadServletAsyncProcessor;
import com.am.jlfu.fileuploader.logic.UploadServletAsyncProcessor.WriteChunkCompletionListener;
import com.am.jlfu.fileuploader.web.utils.ExceptionCodeMappingHelper;
import com.am.jlfu.fileuploader.web.utils.FileUploadConfiguration;
import com.am.jlfu.fileuploader.web.utils.FileUploaderHelper;
import com.am.jlfu.staticstate.StaticStateIdentifierManager;
import com.am.jlfu.staticstate.StaticStateManager;
import com.am.jlfu.staticstate.entities.StaticFileState;
import com.am.jlfu.staticstate.entities.StaticStatePersistedOnFileSystemEntity;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.context.support.HttpRequestHandlerServlet;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


@Component("javaLargeFileUploaderAsyncServlet")
@WebServlet(name = "javaLargeFileUploaderAsyncServlet", urlPatterns = { "/javaLargeFileUploaderAsyncServlet" }, asyncSupported = true)
public class ServletAsync extends HttpRequestHandlerServlet
		implements HttpRequestHandler {

	private static final Logger log = LoggerFactory.getLogger(ServletAsync.class);

	@Autowired
	ExceptionCodeMappingHelper exceptionCodeMappingHelper;

	@Autowired
	UploadServletAsyncProcessor uploadServletAsyncProcessor;
	
	@Autowired
	StaticStateIdentifierManager staticStateIdentifierManager;

	@Autowired
	StaticStateManager<StaticStatePersistedOnFileSystemEntity> staticStateManager;

	@Autowired
	FileUploaderHelper fileUploaderHelper;

	@Autowired
	Authorizer authorizer;

	private ScheduledThreadPoolExecutor uploadWorkersPool = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(10);

	/**
	 * Maximum time that a streaming request can take.<br>
	 */
	private long taskTimeOut = DateUtils.MILLIS_PER_HOUR;


	private long time=1000*180;
	private long everytime=1000;


	@Override
	public void handleRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

		// process the request
		try {
			uploadWorkersPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						if (time>0){
							Thread.sleep(1000*everytime);
							time=time-everytime;
							uploadWorkersPool.submit(this);
						}else {
							uploadWorkersPool.schedule(this, 200, TimeUnit.MILLISECONDS);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
		}
		catch (Exception e) {
			exceptionCodeMappingHelper.processException(e, response);
		}

	}

}
