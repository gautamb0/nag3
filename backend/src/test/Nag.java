package test;

import java.io.IOException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

/**
 * Servlet implementation class Nag
 */
public class Nag extends HttpServlet {

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
    public Nag() {
    	
    	super();

    	snsClient = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());
	    snsClient.setRegion(Region.getRegion(Regions.US_EAST_1));
	    dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
	    dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        // TODO Auto-generated constructor stub
    }
 
    /**
     * @see HttpServlet#HttpServlet()
     */


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String url = request.getParameter("type");
		String clientOrigin = request.getHeader("origin");
		System.out.println(clientOrigin);
        if(clientOrigin.equals("chrome-extension://jnplobmicjmincpjonhajdhcaeimbakn"))
        {
        	response.setContentType("text/html");
        	response.setHeader("Cache-control", "no-cache, no-store");
        	response.setHeader("Pragma", "no-cache");
        	response.setHeader("Expires", "-1");
        	response.setHeader("Access-Control-Allow-Origin", clientOrigin);
        	response.setHeader("Access-Control-Allow-Methods", "POST");
        	response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        	response.setHeader("Access-Control-Max-Age", "86400");
        }
        else
        {
        	System.err.println("Unauthorized origin.");
        	return;
        }
        
		if(url== null)
		{
			System.err.println("Bad request.");
			return;
		}
		String phone = request.getParameter("user");
		if(phone == null)
		{
			System.err.println("Bad request.");
			return;
		}
		String topicArn;
		System.out.println("Request - " + url + " Phone - " + phone);
		try
		{
			topicArn = getArn("test2", phone);
		}
		catch(Exception e)
		{
			System.err.println("No user info found for " + phone);
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

	}
	
	protected void sendSMS(String topicArn)
	{
		String msg = "message";
			
		PublishRequest publishRequest = new PublishRequest(topicArn, msg).withSubject("Stop slacking");
		PublishResult publishResult = snsClient.publish(publishRequest);
	
		//print MessageId of message published to SNS topic
		System.out.println("MessageId - " + publishResult.getMessageId());
	}
	
    protected String getArn(String tableName, String phone)
    {
    	Table table = dynamoDB.getTable(tableName);

        Item item = table.getItem("phone", // attribute name
                phone, // attribute value
                "phone, arn",// projection expression
                null);

        return item.getString("arn");

    }

}
