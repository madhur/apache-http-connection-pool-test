

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes=ApplicationConfig.class)
@SpringBootTest
public class ApplicationConfigTest
{
	@Autowired
	private HttpClient httpClient;

	@Autowired
	private ApplicationConfig applicationConfig;

	private String url = "https://www.google.com";

	long start = 0;

	@Before
	public void start() {
		start = System.currentTimeMillis();
	}

	@After
	public void end() {
		System.out.println(System.currentTimeMillis() - start);
	}

	@Test
	public void makeHttpRequestsWithoutClosingResponse() {
		for(int i=0;i<50;++i) {
			makeHttpRequestsWithoutClosingResponse(i);
		}
		System.out.println(createHttpInfo(applicationConfig.connectionManager));
	}

	@Test
	public void makeHttpRequestAndUseResponse() {
		for(int i=0;i<50;++i) {
			makeHttpRequestAndUseResponse(i);
		}
		System.out.println(createHttpInfo(applicationConfig.connectionManager));
	}

	@Test
	public void makeHttpRequestAndConsumeEntity() {
		for(int i=0;i<50;++i) {
			makeHttpRequestAndConsumeEntity(i);
		}
		System.out.println(createHttpInfo(applicationConfig.connectionManager));
	}

	@Test
	public void makeHttpRequestsAndCloseResponse() {
		for(int i=0;i<50;++i) {
			makeHttpRequestAndCloseResponse(i);
		}
		System.out.println(createHttpInfo(applicationConfig.connectionManager));
	}

	@Test
	public void makeHttpRequestAndCloseResponseAndConsumeEntity() {
		for(int i=0;i<50;++i) {
			makeHttpRequestAndCloseResponseAndConsumeEntity(i);
		}
		System.out.println(createHttpInfo(applicationConfig.connectionManager));
	}

	private void makeHttpRequestsWithoutClosingResponse(int i) {
		System.out.println("Executing request number: " + i);
		HttpGet httpGet = new HttpGet();
		httpGet.setURI(URI.create(url));
		try {
			HttpResponse response = httpClient.execute(httpGet);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println(createHttpInfo(applicationConfig.connectionManager));
			Assert.fail("Connection timeout at request number: " + i);
		}
	}

	private void makeHttpRequestAndUseResponse(int i) {
		System.out.println("Executing request number: " + i);
		HttpGet httpGet = new HttpGet();
		httpGet.setURI(URI.create(url));
		String responseStr = null;
		try {
			HttpResponse response = httpClient.execute(httpGet);
			responseStr = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println(createHttpInfo(applicationConfig.connectionManager));
			Assert.fail("Connection timeout at request number: " + i);
		}
	}

	private void makeHttpRequestAndConsumeEntity(int i) {
		System.out.println("Executing request number: " + i);
		HttpGet httpGet = new HttpGet();
		httpGet.setURI(URI.create(url));
		try {
			HttpResponse response = httpClient.execute(httpGet);
			EntityUtils.consumeQuietly(response.getEntity());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println(createHttpInfo(applicationConfig.connectionManager));
			Assert.fail("Connection timeout at request number: " + i);
		}
	}

	private void makeHttpRequestAndCloseResponse(int i) {
		System.out.println("Executing request number: " + i);
		HttpGet httpGet = new HttpGet();
		httpGet.setURI(URI.create(url));
		CloseableHttpResponse response = null;
		try {
			response = (CloseableHttpResponse) httpClient.execute(httpGet);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println(createHttpInfo(applicationConfig.connectionManager));
			Assert.fail("Connection timeout at request number: " + i);
		}
		finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void makeHttpRequestAndCloseResponseAndConsumeEntity(int i) {
		System.out.println("Executing request number: " + i);
		HttpGet httpGet = new HttpGet();
		httpGet.setURI(URI.create(url));
		CloseableHttpResponse response = null;
		try {
			response = (CloseableHttpResponse) httpClient.execute(httpGet);
			EntityUtils.consumeQuietly(response.getEntity());
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.out.println(createHttpInfo(applicationConfig.connectionManager));
			Assert.fail("Connection timeout at request number: " + i);
		}
		finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static String createHttpInfo(PoolingHttpClientConnectionManager connectionManager) {
		StringBuilder sb = new StringBuilder();
		sb.append("=========================").append("\n");
		sb.append("General Info:").append("\n");
		sb.append("-------------------------").append("\n");
		sb.append("MaxTotal: ").append(connectionManager.getMaxTotal()).append("\n");
		sb.append("DefaultMaxPerRoute: ").append(connectionManager.getDefaultMaxPerRoute()).append("\n");
		sb.append("ValidateAfterInactivity: ").append(connectionManager.getValidateAfterInactivity()).append("\n");
		sb.append("=========================").append("\n");

		PoolStats totalStats = connectionManager.getTotalStats();
		sb.append(createPoolStatsInfo("Total Stats", totalStats));

		Set<HttpRoute> routes = connectionManager.getRoutes();

		if (routes != null) {
			for (HttpRoute route : routes) {
				sb.append(createRouteInfo(connectionManager, route));
			}
		}

		return sb.toString();
	}

	private static String createRouteInfo(
			PoolingHttpClientConnectionManager connectionManager, HttpRoute route) {
		PoolStats routeStats = connectionManager.getStats(route);
		String info = createPoolStatsInfo(route.getTargetHost().toURI(), routeStats);
		return info;
	}

	private static String createPoolStatsInfo(String title, PoolStats poolStats) {
		StringBuilder sb = new StringBuilder();

		sb.append(title + ":").append("\n");
		sb.append("-------------------------").append("\n");

		if (poolStats != null) {
			sb.append("Available: ").append(poolStats.getAvailable()).append("\n");
			sb.append("Leased: ").append(poolStats.getLeased()).append("\n");
			sb.append("Max: ").append(poolStats.getMax()).append("\n");
			sb.append("Pending: ").append(poolStats.getPending()).append("\n");
		}

		sb.append("=========================").append("\n");

		return sb.toString();
	}
}
