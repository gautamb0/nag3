package test;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;


/**
 * Servlet implementation class S3Sample
 */
public class S3Sample extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private AmazonSNSClient snsClient;
    private Thread t;
    private volatile boolean threadSuspended;
    private final Object monitor = new Object();
    static DynamoDB dynamoDB;
    static SimpleDateFormat dateFormatter;
        /**
     * @see HttpServlet#HttpServlet()
     */
    public S3Sample() {
    	
    	super();
		//t = new Thread(new MessageLoop());
		//threadSuspended = true;
		//t.start();
    	snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());
	    snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
	    dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
	    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("S3Sample ").append(request.getContextPath()).append(" ");
/*		String url = request.getParameter("type");
		if(url== null)
		{
			System.err.println("Bad request.");
			return;
		}
		response.getWriter().append("url: ").append(url).append(" ");
		String email = request.getParameter("user");
		if(email == null)
		{
			System.err.println("Bad request.");
			return;
		}
		response.getWriter().append("email: ").append(email).append(" ");
		String topicArn;
		System.out.println("Request - " + url + " Email - " + email);
		try
		{
			topicArn = getArn("test", email);
		}
		catch(Exception e)
		{
			response.getWriter().append(e.getMessage());
			System.err.println("No user info found for " + email);
			return;
		}
		response.getWriter().append("topicArn: ").append(topicArn).append(" ");
		try
		{
			sendSMS(topicArn);
		}
		catch(Exception e)
		{
			System.err.println("Bad arn " + topicArn);
			return;
		}
		//System.out.println("Request - " + request.getParameter("type"));*/
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//sendSMS("arn:aws:sns:us-east-1:990888494507:000gautam000gmailcom");
		//return;
		String url = request.getParameter("type");
		if(url== null)
		{
			System.err.println("Bad request.");
			return;
		}
		String email = request.getParameter("user");
		if(email == null)
		{
			System.err.println("Bad request.");
			return;
		}
		String topicArn;
		System.out.println("Request - " + url + " Email - " + email);
		try
		{
			topicArn = getArn("test", email);
		}
		catch(Exception e)
		{
			System.err.println("No user info found for " + email);
			return;
		}
		try
		{
			sendSMS(topicArn);
		}
		catch(Exception e)
		{
			System.err.println("Bad arn " + topicArn);
			return;
		}
		response.getWriter().append("Served at: ").append(request.getContextPath());
		/*synchronized(monitor){
		if(request.getParameter("type").equals("bad"))
		{		
	        threadSuspended = false;
	        if(!threadSuspended)
	        {
	        	monitor.notify();
	        }
		}
		else
		{
			threadSuspended = true;
		}
		}*/
		
	}
	
	protected void sendSMS(String topicArn)
	{
		String msg = "message";
			
		PublishRequest publishRequest = new PublishRequest(topicArn, msg).withSubject("Stop slacking");
		PublishResult publishResult = snsClient.publish(publishRequest);
	
		//print MessageId of message published to SNS topic
		System.out.println("MessageId - " + publishResult.getMessageId());
	}
	
    protected String getArn(String tableName, String email)
    {
    	Table table = dynamoDB.getTable(tableName);

        Item item = table.getItem("email", // attribute name
                email, // attribute value
                "email, arn, ph",// projection expression
                null);

        return item.getString("arn");

    }
	
	private class MessageLoop implements Runnable
	{

		public void run()
		{
			while(true)
			{
				
				try
				{
					Thread.sleep(4000);
					if(threadSuspended)
					{
						synchronized(monitor)
						{
							while(threadSuspended)
								monitor.wait();

						}
					}
				}
				catch (InterruptedException e)
				{
					
				}
				
			}
		}
	
	}
	

}
